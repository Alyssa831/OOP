package supermarket;

/**
 * Normal plan — no discount, no annual fee. The default plan for new customers.
 */
public class Normal implements CustomerPlan {
	@Override
	public double billAfterDiscount(double price) {
		return price;
	}

	public int getAnnualFee() {
		return 0;
	}
}
