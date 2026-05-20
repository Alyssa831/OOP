package supermarket;

public class Cashier extends User {
	private String firstname;
	private String surname;

	public Cashier(String firstname, String surname, String username, int password) {
		super(username, password);
		this.firstname = firstname;
		this.surname = surname;
	}

	public String getFirstname() { return firstname; }
	public String getSurname()   { return surname; }
}
