package supermarket;

/**
 * Platinum plan — 30% discount on the bill regardless of total (R5).
 */
public class Platinum implements DiscountPlan {
	private double discount = 0.3;

	@Override
	public double billAfterDiscount(double price, double deliveryfee) {
		return price * (1 - discount);
	}
}
