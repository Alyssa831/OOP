package supermarket;

import java.util.HashMap;
import java.util.Map;

/**
 * One customer's checkout in progress. Holds the cart, the running bill,
 * and references to system-level collaborators (Stock, PricingPolicy, POS).
 *
 * Lifetime is one customer's purchase — set up by CheckoutSystem.startCheckout,
 * cleared (or replaced) when pay() succeeds.
 */
public class CheckoutSession {
	private Customer customer;
	private Map<Item,Integer> yourItems = new HashMap<>();
	private double yourBill = 0.0;
	private double totalWeight = 0.0;
	private double deliveryFee = 0.0;
	private Stock myStock;
	private PricingPolicy pp;
	private POS pos;
	private PriceCalculator priceCalculator = new PriceCalculator();
	private double distance ;
	

	public CheckoutSession(Customer customer, Stock myStock, PricingPolicy pp, POS pos, double distance) {
		this.customer = customer;
		this.myStock = myStock;
		this.pp = pp;
		this.pos = pos;
		this.distance = distance;
	}

	public void scanItem(String name, int q) {
		yourItems.put(myStock.getNameItem().get(name), q);
	}

	/**
	 * Design decision (composition order): category pricing policy is applied
	 * first to each item, then the customer's plan discount is applied to the
	 * resulting subtotal. The brief is silent on order — this is the documented
	 * choice for the report.
	 */
	public void computeBill() {
		yourBill = 0.0;
		totalWeight = 0.0;
		deliveryFee = 0.0;
		for (Item item : yourItems.keySet()) {
			int q=yourItems.get(item); //quantity of this item in the cart
			double newPrice=priceCalculator.calculatePrice(item, q);
			yourBill += pp.priceAfterDiscount(item,newPrice) * q;
			totalWeight += item.getWeight() * q;
		}
		if(customer.getRequestDelivery() != null) {
			//deliveryFee is function of weights and distance (R7/R7b)
			// For example deliveries under 10
			// Kg and within 30Km distance may be charged a fixed amount (e.g. 15 Euros) whereas
			// delivery between 10 and 50 Kg should be charged a fixed amount plus a percentage of
			// the total price for the bought items. Deliveries of more than 50 Kg are not supported
			// and should be refused by the 
			
			if(totalWeight <= 10 && distance <= 30 && customer.getRequestDelivery() != null) {
				deliveryFee = 15.0;
			}
			else if(totalWeight > 10 && totalWeight <= 50 && customer.getRequestDelivery() != null) {
				deliveryFee = 15.0 + (0.1 * yourBill); // Example: fixed amount plus 10% of total price
			}
			else if (totalWeight > 50) {
				// R8: deliveries over 50 kg are refused. Aborting computeBill
				// signals the refusal to the caller (CLUI catches and prints).
				throw new IllegalStateException(
					"Delivery refused: weight " + totalWeight + " kg exceeds the 50 kg limit.");
			}
		}
		yourBill = customer.getDp().billAfterDiscount(yourBill, deliveryFee);
	}

	/**
	 * Run the payment. Inventory is decremented and the cart cleared ONLY on
	 * SUCCESS — failure outcomes leave state untouched so the cashier can retry.
	 */
	public PaymentOutcome pay(int cardnumber, int pin) {
		PaymentOutcome result = pos.process(cardnumber, pin, yourBill);
		if (result == PaymentOutcome.SUCCESS) {
			for (Item item : yourItems.keySet()) {
				myStock.sold(item, yourItems.get(item));
			}
			yourItems.clear();
			yourBill = 0;
		}
		return result;
	}

	public Customer getCustomer()      { return customer; }
	public double getYourBill()        { return yourBill; }
	public Stock getMyStock()          { return myStock; }
	public Map<Item, Integer> getCart() { return yourItems; }
}
