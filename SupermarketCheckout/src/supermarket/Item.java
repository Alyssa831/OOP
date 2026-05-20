package supermarket;

public class Item{
	private String name;
	private String category;
	private double price;
	private double weight;
	private int quantity;
	
	public String getName() {
		return name;
	}

	public String getCategory() {
		return category;
	}

	public double getPrice() {
		return price;
	}

	public double getWeight() {
		return weight;
	}
	
	public Item(String name, String category, double price, double weight, int initialStock) {
		super();
		this.name = name;
		this.category = category;
		this.price = price;
		this.weight = weight;
		quantity = initialStock;
		
	}
	public int getQuantity() {
		return quantity;
	}
	
	public void setQuantity(int quantity) {
		this.quantity = quantity;
		
	}
	
}	
