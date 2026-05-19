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
		// TODO Auto-generated method stub
		return item.getPrice()*catDis.get(item.getCategory());
	}
	@Override
	public double setCatDis(String cat, double dis) {
		// TODO Auto-generated method stub
		catDis.put(cat,dis);
		return 0;
	}

	
}
