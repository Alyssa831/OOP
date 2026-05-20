package supermarket;

/**
 * Supplier observes Stock and is notified when an item drops below its
 * configured threshold (R9). For now it just prints a restock alert.
 */
public class Supplier implements Observer {
	@Override
	public void update(Item item) {
		System.out.println("[Supplier] Low stock alert: " + item.getCategory()
			+ " — please restock this item.");
	}
}
