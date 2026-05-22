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
	/**
	 * Compute the total bill including delivery fee with R10 dynamic pricing
	 */
	public void computeBill() {
		yourBill = 0.0;
		totalWeight = 0.0;
		deliveryFee = 0.0;
		
		// Calculate item totals
		for (Item item : yourItems.keySet()) {
			int q = yourItems.get(item);
			double newPrice = priceCalculator.calculatePrice(item, q);
			yourBill += pp.priceAfterDiscount(item, newPrice) * q;
			totalWeight += item.getWeight() * q;
		}
		
		double subtotalBeforeDelivery = yourBill;
		
		// Calculate delivery fee if requested
		if (customer.getRequestDelivery() != null) {
			String assignedSlot = customer.getAssignedTimeSlot();
			
			// If no slot assigned yet, use DeliveryService to get one
			if (assignedSlot == null) {
				DeliveryService ds = DeliveryService.getInstance();
				assignedSlot = ds.requestDelivery(customer, customer.getRequestDelivery(), totalWeight);
				if (assignedSlot != null) {
					customer.setAssignedTimeSlot(assignedSlot);
				}
			}
			
			// Calculate delivery fee with the assigned slot
			if (assignedSlot != null) {
				DeliveryService ds = DeliveryService.getInstance();
				
				if (totalWeight > 50) {
					throw new IllegalStateException(
						"Delivery refused: weight " + totalWeight + " kg exceeds the 50 kg limit.");
				}
				
				deliveryFee = ds.calculateDeliveryFee(totalWeight, subtotalBeforeDelivery, customer, assignedSlot);
				System.out.printf("Delivery fee: %.2f EUR%n", deliveryFee);
			} else {
				// Fallback to legacy R8 logic if no slot available
				if (totalWeight <= 10 && distance <= 30) {
					deliveryFee = 15.0;
				} else if (totalWeight > 10 && totalWeight <= 50) {
					deliveryFee = 15.0 + (0.1 * yourBill);
				} else if (totalWeight > 50) {
					throw new IllegalStateException("Delivery refused: weight exceeds 50kg");
				}
			}
		}
		
		// Apply customer plan discount to (item total + delivery fee)
		yourBill = customer.getDp().billAfterDiscount(yourBill, deliveryFee);
		
		// Clear delivery request for next purchase (but keep assigned slot until payment)
		// We'll clear the request, but keep assigned slot for booking during pay()
	}

	/**
	 * Run the payment. Inventory is decremented and the cart cleared ONLY on
	 * SUCCESS — failure outcomes leave state untouched so the cashier can retry.
	 * Also books the delivery slot if delivery was requested.
	 */
	public PaymentOutcome pay(int cardnumber, int pin) {
		PaymentOutcome result = pos.process(cardnumber, pin, yourBill);
		if (result == PaymentOutcome.SUCCESS) {
			for (Item item : yourItems.keySet()) {
				myStock.sold(item, yourItems.get(item));
			}
			
			// Book the delivery slot if delivery was requested
			if (customer.getRequestDelivery() != null && customer.getAssignedTimeSlot() != null) {
				DeliveryService ds = DeliveryService.getInstance();
				ds.bookDelivery(customer, totalWeight);
				System.out.println("📦 Delivery slot booked successfully!");
			}
			
			yourItems.clear();
			yourBill = 0;
			totalWeight = 0;
			deliveryFee = 0;
			
			// Clear delivery request and assigned slot
			customer.setRequestDelivery(null);
			customer.clearDeliveryRequest();
		}
		return result;
	}

	public Customer getCustomer()      { return customer; }
	public double getYourBill()        { return yourBill; }
	public Stock getMyStock()          { return myStock; }
	public Map<Item, Integer> getCart() { return yourItems; }
}