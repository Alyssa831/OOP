package supermarket;

/**
 * A customer of the supermarket.
 * Per §2.3: firstname, surname, numerical ID, discount plan.
 * Plus address (from registerCustomer CLUI) and an auto-generated test Card.
 */
public class Customer extends User {
	private int numericalID;
	private String firstname;
	private String surname;
	private String address;
	private DiscountPlan dp;
	private Card card;
	private static int idCounter = 0;
	private String requestDelivery=null;
	private String requestedTimeSlot = null;

	public Customer(String firstname, String surname, String username,
	                String address, int password) {
		super(username, password);
		this.numericalID = ++idCounter;
		this.firstname = firstname;
		this.surname = surname;
		this.address = address;
		this.dp = new Normal();
	}

	public int getNumericalID()      { return numericalID; }
	public String getFirstname()     { return firstname; }
	public String getSurname()       { return surname; }
	public String getAddress()       { return address; }
	public DiscountPlan getDp()      { return dp; }
	public Card getCard()            { return card; }
	public String getRequestDelivery() { return requestDelivery; }

	public void setRequestDelivery(String requestDelivery) { this.requestDelivery = requestDelivery; }
	public void setDp(DiscountPlan dp) { this.dp = dp; }
	public void setCard(Card card)     { this.card = card; }
	public String getRequestedTimeSlot() { return requestedTimeSlot; }

	public void clearDeliveryRequest() {
        this.requestDelivery = null;
        this.requestedTimeSlot = null;
    }
}
