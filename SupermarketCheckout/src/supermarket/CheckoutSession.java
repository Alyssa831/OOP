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
	private Stock myStock;
	private PricingPolicy pp;
	private POS pos;

	public CheckoutSession(Customer customer, Stock myStock, PricingPolicy pp, POS pos) {
		this.customer = customer;
		this.myStock = myStock;
		this.pp = pp;
		this.pos = pos;
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
		for (Item item : yourItems.keySet()) {
			yourBill += pp.priceAfterDiscount(item) * yourItems.get(item);
		}
		yourBill = customer.getDp().billAfterDiscount(yourBill);
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
