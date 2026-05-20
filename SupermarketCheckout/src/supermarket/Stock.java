package supermarket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Stock implements Observable {
	private Map<String,Item> nameItem = new HashMap<>();
	private Map<Item,Integer> threshold;
	private boolean changed = false;

	// R9: multiple observers can subscribe (e.g., Manager AND Supplier).
	private List<Observer> observers = new ArrayList<>();

	public Map<String, Item> getNameItem() { return nameItem; }
	public void setNameItem(Map<String, Item> nameItem) { this.nameItem = nameItem; }

	// addItem <itemName> <categoryName> <unitPrice> <weight> <initialStock>
	public void addItem(String name, String category, double price, double weight, int initialStock) {
		nameItem.put(name, new Item(name, category, price, weight, initialStock));
	}

	// restock <itemName> <quantity>
	public void restock(String name, int quantity) {
		nameItem.get(name).setQuantity(quantity);
	}

	public void sold(Item item, int quantity) {
		item.setQuantity(item.getQuantity() - quantity);
		// NOTE (per user request): leave the threshold check logic alone.
		if (quantity <= threshold.get(item)) {
			this.changed = true;
			this.notifyObserver(item);
		}
	}

	// --- Observer pattern: subscribe/unsubscribe ---
	@Override
	public void addObserver(Observer o) {
		observers.add(o);
	}
	@Override
	public void removeObserver(Observer o) {
		observers.remove(o);
	}

	@Override
	public void notifyObserver(Item item) {
		if (this.changed && item.getQuantity() <= threshold.get(item)) {
			for (Observer o : observers) {
				o.update(item);
			}
			this.changed = false;
		}
	}
}
