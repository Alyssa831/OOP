package supermarket;

import java.util.HashMap;
import java.util.Map;

/**
 * The long-lived coordinator. Owns Bank, TAS, POS, Stock, the active
 * PricingPolicy, the registry of discount plans, and the registries of
 * customers / cashiers / managers. Also holds the current CheckoutSession
 * (0..1 — null when nobody is at the till).
 */
public class CheckoutSystem {
	private Map<String, Customer> customers = new HashMap<>();
	private Map<String, Cashier> cashiers   = new HashMap<>();
	private Map<String, Manager> managers   = new HashMap<>();

	// Available plans for subscribeToPlan (extensible — register new ones here).
	private Map<String, DiscountPlan> plans = new HashMap<>();

	private CheckoutSession session;
	// Start with an empty CatDis so computeBill works even before any
	// setCategoryDiscount has been called (defaults to no category discounts).
	private PricingPolicy pp = new CatDis();
	private Stock myStock = new Stock();
	private double revenue = 0;
	private Bank bank;
	private TAS tas;
	private POS pos;

	public CheckoutSystem() { }

	public CheckoutSystem(Bank bank, TAS tas) {
		this.bank = bank;
		this.tas = tas;
		this.pos = new POS(bank, tas);

		// Register the default plan implementations (R5). To add a new plan,
		// implement DiscountPlan and call registerPlan(...) — no edit needed
		// elsewhere (R5b).
		registerPlan("normal", new Normal());
		registerPlan("prime", new Prime());
		registerPlan("platinum", new Platinum());
	}

	// ---- Plan registry (extensibility for R5b) ----

	public void registerPlan(String name, DiscountPlan plan) {
		plans.put(name.toLowerCase(), plan);
	}

	public DiscountPlan getPlan(String name) {
		return plans.get(name.toLowerCase());
	}

	// ---- Category pricing (R6/R6b) ----

	public void setCatDis(String cat, double dis) {
		if (pp == null) pp = new CatDis();
		pp.setCatDis(cat, dis);
	}

	// ---- Registration (manager actions) ----

	public void registerCustomer(String firstname, String surname, String username,
	                             String address, int password, Card autoCard) {
		Customer c = new Customer(firstname, surname, username, address, password);
		c.setCard(autoCard);
		customers.put(username, c);
	}

	public void registerCashier(String firstname, String surname, String username, int password) {
		cashiers.put(username, new Cashier(firstname, surname, username, password));
	}

	public void registerManager(String username, int password) {
		managers.put(username, new Manager(username, password));
	}

	// ---- Checkout session lifecycle ----

	public void startCheckout(String customerUsername) {
		Customer customer = customers.get(customerUsername);
		session = new CheckoutSession(customer, myStock, pp, pos);
	}

	public void endSession() { session = null; }

	// ---- Revenue ----

	public void addRevenue(double amount) { this.revenue += amount; }
	public double getRevenue()            { return revenue; }

	// ---- Getters ----

	public Map<String, Customer> getCustomers() { return customers; }
	public Map<String, Cashier>  getCashiers()  { return cashiers; }
	public Map<String, Manager>  getManagers()  { return managers; }
	public CheckoutSession       getSession()   { return session; }
	public Stock                 getMyStock()   { return myStock; }
	public POS                   getPos()       { return pos; }
	public Bank                  getBank()      { return bank; }
	public TAS                   getTas()       { return tas; }
	public PricingPolicy         getPp()        { return pp; }
}
