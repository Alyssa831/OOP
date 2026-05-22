package supermarket;

/**
 * Platinum plan — 30% discount on the bill regardless of total (R5).
 */
public class Platinum implements DiscountPlan {
	private double discount = 0.3;

	@Override
	public double billAfterDiscount(double price, double deliveryfee) {
		// Item discount only (R5): 30% off the items, regardless of total.
		// The delivery fee's plan discount (R8b: platinum delivery is free) is
		// applied by DeliveryService, so we just ADD the (already-zeroed) fee.
		return price * (1 - discount) + deliveryfee;
	}
}
