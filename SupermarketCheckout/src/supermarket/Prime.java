package supermarket;

public class Prime implements DiscountPlan {
	
	double discount=0.2;
	public Prime() {
		// TODO Auto-generated constructor stub
	}
	//prime which yields a 20% discount on the bought items whenever the total price of bought items
	//is greater than or equal to 50 Euros

	@Override
	public double billAfterDiscount(double price) {
		// 20% off when the subtotal is >= 50€, no discount otherwise (R5)
		if (price >= 50) return price * (1 - discount);
		return price;
	}

}
