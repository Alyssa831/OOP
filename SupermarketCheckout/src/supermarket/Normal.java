package supermarket;

/**
 * Normal plan — no discount, no annual fee. The default plan for new customers.
 */
public class Normal implements DiscountPlan {
	@Override
	public double billAfterDiscount(double price) {
		return price;
	}
}
