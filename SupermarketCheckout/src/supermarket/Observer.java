package supermarket;

/**
 * Observer interface for the low-stock notification (R9).
 * Receives the Item that crossed the threshold so the observer
 * knows WHAT to restock, not just that something is low.
 */
public interface Observer {
	
	void update(Item item);
}
