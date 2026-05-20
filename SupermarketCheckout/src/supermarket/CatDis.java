package supermarket;

import java.util.HashMap;
import java.util.Map;

public class CatDis implements PricingPolicy {
	private Map<String,Double> catDis=new HashMap<>();
	public CatDis() {
		// TODO Auto-generated constructor stub
	}
	@Override
	public double priceAfterDiscount(Item item) {
		// If the category has no policy registered, default to no discount
		// (multiplier 1.0) — avoids NullPointerException on freshly added items.
		double multiplier = catDis.getOrDefault(item.getCategory(), 1.0);
		return item.getPrice() * multiplier;
	}
	@Override
	public double setCatDis(String cat, double dis) {
		// TODO Auto-generated method stub
		catDis.put(cat,dis);
		return 0;
	}

	
}
