package supermarket;

import java.util.HashMap;
import java.util.Map;

public class Stock implements Observable{
	private Map<String,Item> nameItem=new HashMap<>();
	
	public Map<String, Item> getNameItem() {
		return nameItem;
	}
	public void setNameItem(Map<String, Item> nameItem) {
		this.nameItem = nameItem;
	}
	
	//addItem <itemName> <categoryName> <unitPrice> <weight> <initialStock>
	public void addItem(String name, String category, double price, double weight, int initialStock) {
		nameItem.put(name,new Item(name, category, price, weight, initialStock));
	}
	//restock <itemName> <quantity>
	public void restock(String name, int quantity) {
		nameItem.get(name).setQuantity(quantity);
	}
	
	private Map<Item,Integer> threshold;
	private boolean changed=false;
	private Observer ob;
	public void sold(Item item, int quantity) {
		item.setQuantity(item.getQuantity()-quantity);
		if(quantity<=threshold.get(item)) {
			this.changed = true;
			this.notifyObserver(item);
		}
	}
	@Override
	public void notifyObserver(Item item) {
		if (this.changed) {
			ob.update(item.getQuantity());
			this.changed = false;
		}
	}
	
}
