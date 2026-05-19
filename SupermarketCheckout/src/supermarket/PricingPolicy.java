package supermarket;

public interface PricingPolicy {
	
	public double priceAfterDiscount(Item item);
	public double setCatDis(String cat, double dis);
}
