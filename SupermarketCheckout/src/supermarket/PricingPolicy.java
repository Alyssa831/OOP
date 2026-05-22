package supermarket;

public interface PricingPolicy {
	
	public double priceAfterDiscount(Item item, double price);
	public void setCatDis(String cat, double dis);
}
