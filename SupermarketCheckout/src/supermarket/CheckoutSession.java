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
		totalWeight = 0.0;
		deliveryFee = 0.0;
		for (Item item : yourItems.keySet()) {
			yourBill += pp.priceAfterDiscount(item) * yourItems.get(item);
			totalWeight += item.getWeight() * yourItems.get(item);
		}

		double subtotalBeforeDelivery = yourBill;

		String deliveryRequest = customer.getRequestDelivery();
			
		// Check if this is a new R10 request with time slot (format: "address|timeSlot")
		if (deliveryRequest.contains("|")) {
			// R10: Use DeliveryService with time slot
			String[] parts = deliveryRequest.split("\\|");
			String address = parts[0];
			String timeSlot = parts[1];
			
			DeliveryService ds = DeliveryService.getInstance();
			
			// Check weight limit first
			if (totalWeight > 50) {
				System.out.println("Delivery not supported: weight exceeds 50 Kg.");
				deliveryFee = 0.0;
			} else {
				// Calculate delivery fee using R10 rules
				deliveryFee = ds.calculateDeliveryFee(totalWeight, subtotalBeforeDelivery, customer, timeSlot);
				if (deliveryFee > 0) {
					System.out.printf("Delivery fee (R10): %.2f EUR for slot %s%n", deliveryFee, timeSlot);
				}
			}
		} else {
			// Legacy R8/R8b: No time slot (old format, just address)
			if(totalWeight <= 10) {
				deliveryFee = 15.0;
				System.out.println("Delivery fee (fixed): 15.00 EUR");
			}
			else if(totalWeight > 10 && totalWeight <= 50) {
				deliveryFee = 15.0 + (0.1 * yourBill);
				System.out.printf("Delivery fee (fixed + 10%%): %.2f EUR%n", deliveryFee);
			}
			else if(totalWeight > 50) {
				System.out.println("Delivery not supported for weights over 50 Kg.");
				deliveryFee = 0.0;
			}
		}
		// Apply customer plan discount to (item total + delivery fee)
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
			if (customer.getRequestDelivery() != null && !customer.getRequestDelivery().isEmpty()) {
				String deliveryRequest = customer.getRequestDelivery();
				
				if (deliveryRequest.contains("|")) {
					String[] parts = deliveryRequest.split("\\|");
					String timeSlot = parts[1];
					
					DeliveryService ds = DeliveryService.getInstance();
					ds.bookDelivery(totalWeight, customer, timeSlot);
					System.out.println("Delivery slot booked: " + timeSlot);
				}
				
				// Clear the delivery request after successful payment
				customer.setRequestDelivery(null);
			}
			
			yourItems.clear();
			yourBill = 0;
			totalWeight = 0;
			deliveryFee = 0;
		}
		return result;
	}

	public Customer getCustomer()      { return customer; }
	public double getYourBill()        { return yourBill; }
	public Stock getMyStock()          { return myStock; }
	public Map<Item, Integer> getCart() { return yourItems; }
	public double getTotalWeight()     { return totalWeight; }
}
