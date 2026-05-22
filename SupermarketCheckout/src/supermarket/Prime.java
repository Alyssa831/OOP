package supermarket;

public class Prime implements DiscountPlan {
	
	double discount=0.2;
	public Prime() {
		// TODO Auto-generated constructor stub
	}
	//prime which yields a 20% discount on the bought items whenever the total price of bought items
	//is greater than or equal to 50 Euros

	@Override
	public double billAfterDiscount(double price, double deliveryfee) {
		// Item discount only (R5): 20% off the items when the subtotal is >= 50€.
		// The delivery fee's plan discount (R8b: prime pays 50%) is applied by
		// DeliveryService, so here we just ADD the already-discounted delivery fee.
		double items = (price >= 50) ? price * (1 - discount) : price;
		return items + deliveryfee;
	}

}
