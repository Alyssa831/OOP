package supermarket;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;

/**
 * Command-Line User Interface for the Supermarket Checkout System.
 *
 * State shared across all commands lives in static fields:
 *   system      — the long-lived coordinator (Bank, TAS, POS, Stock, registries)
 *   currentUser — the User object currently logged in (null when nobody is)
 *   currentRole — the role enum, kept in sync with currentUser's type
 *
 * Role checks use {@link Role}; the old int-codes (0/1/2/3) have been retired.
 * The "checkoutstarted" boolean has been retired too — system.getSession()!=null
 * is the single source of truth.
 */
public class CLUI {
	private static Bank bank = new Bank();
	private static TAS tas = new TAS();
	private static CheckoutSystem system = new CheckoutSystem(bank, tas);
	private static User currentUser = null;
	private static Role currentRole = Role.NONE;

	public static void main(String[] args) {
		// "my_supermarket.ini" semantics: apply standard setup at startup.
		applyDefaultSetup();

		System.out.println("Supermarket Checkout System CLUI. Type 'help' for commands.");
		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.print("> ");
			if (!scanner.hasNextLine()) break;
			String input = scanner.nextLine().trim();
			if (input.equalsIgnoreCase("stop") || input.equalsIgnoreCase("exit")) {
				System.out.println("Exiting.");
				break;
			}
			if (input.isEmpty() || input.startsWith("#")) continue;
			dispatchCommand(input);
		}
		scanner.close();
	}

	/**
	 * Parse and dispatch one command line. Extracted so runTest can reuse it.
	 */
	private static void dispatchCommand(String input) {
		String[] parts = input.split("\\s+");
		String command = parts[0].toLowerCase();
		String[] argv = Arrays.copyOfRange(parts, 1, parts.length);

		switch (command) {
			case "help":                 handleHelp(); break;
			case "login":                handleLogin(argv); break;
			case "logout":               handleLogout(); break;
			case "setup":                handleSetup(); break;
			case "registercashier":      handleRegisterCashier(argv); break;
			case "registercustomer":     handleRegisterCustomer(argv); break;
			case "additem":              handleAddItem(argv); break;
			case "restock":              handleRestock(argv); break;
			case "setcategorydiscount":  handleSetCategoryDiscount(argv); break;
			case "subscribetoplan":      handleSubscribeToPlan(argv); break;
			case "startcheckout":        handleStartCheckout(argv); break;
			case "scanitem":             handleScanItem(argv); break;
			case "computebill":          handleComputeBill(); break;
			case "requestdelivery":      handleRequestDelivery(argv); break;
			case "pay":                  handlePay(argv); break;
			case "simulatepayment":      handleSimulatePayment(argv); break;
			case "showinventory":        handleShowInventory(); break;
			case "showrevenue":          handleShowRevenue(); break;
			case "runtest":              handleRunTest(argv); break;
			case "requestDelivery":      handleRequestDelivery(argv); break; // allow camelCase too
			default:
				System.out.println("Unknown command: " + command + ". Type 'help'.");
		}
	}

	// ============== Authentication ==============

	private static void handleLogin(String[] args) {
		if (currentRole != Role.NONE) {
			System.out.println("Already logged in as " + currentUser.getUsername() + ". Logout first.");
			return;
		}
		if (args.length != 2) {
			System.out.println("Usage: login <username> <password>");
			return;
		}
		String username = args[0];
		int password;
		try { password = Integer.parseInt(args[1]); }
		catch (NumberFormatException e) { System.out.println("Password must be an integer."); return; }

		// Look up the user across all three registries.
		User u = system.getManagers().get(username);
		if (u == null) u = system.getCashiers().get(username);
		if (u == null) u = system.getCustomers().get(username);
		if (u == null) {
			System.out.println("Unknown user: " + username);
			return;
		}
		if (u.getPassword() != password) {
			System.out.println("Wrong password.");
			return;
		}

		currentUser = u;
		if (u instanceof Manager)       currentRole = Role.MANAGER;
		else if (u instanceof Cashier)  currentRole = Role.CASHIER;
		else if (u instanceof Customer) currentRole = Role.CUSTOMER;

		System.out.println("Logged in as " + currentRole + ": " + username);
	}

	private static void handleLogout() {
		if (currentRole == Role.NONE) {
			System.out.println("Nobody is logged in.");
			return;
		}
		System.out.println("Logged out " + currentUser.getUsername());
		currentUser = null;
		currentRole = Role.NONE;
	}

	// ============== Setup / Bootstrap ==============

	private static void handleSetup() {
		applyDefaultSetup();
		System.out.println("Setup applied: default catalogue, plans, ceo, and test cards.");
	}

	/**
	 * Load standard defaults: ceo manager, one cashier, one customer,
	 * the three mandatory categories (R2b), and a couple of test bank cards.
	 * Idempotent — running it again refreshes the same defaults.
	 */
	private static void applyDefaultSetup() {
		// Manager assumed to exist per the brief.
		system.registerManager("ceo", 123456789);

		// Default cashier and customer.
		system.registerCashier("Default", "Cashier", "cashier", 1111);

		Card customerCard = new Card(1234, 0000);
		bank.addCard(customerCard);
		tas.addCardInfo(1234, new Info(500.0, true));
		system.registerCustomer("Default", "Customer", "customer", "1 Default Street", 2222, customerCard);
		
		// Set addresses and distances for delivery (R7/R8/R8b).
		system.setAddressDistance("rue de rivoli, 75001 paris", 1.3); // 1st arrondissement
		system.setAddressDistance("rue de louvre, 75001 paris", 0.4);
		system.setAddressDistance("rue de belleville, 75019 paris", 4.5); // 19th arrondissement
		system.setAddressDistance("place d'armes, 78000 versailles", 19.84); // outside of paris
		system.setAddressDistance("boulevard de Parc, 77700 Coupvray", 39.58); // >30km away

		// Three mandatory categories (R2b) — one starter item per category.
		Stock s = system.getMyStock();
		if (!s.getNameItem().containsKey("apple"))   s.addItem("apple",   "fruit-and-vegetables", 1.20, 0.2, 50);
		if (!s.getNameItem().containsKey("yogurt"))  s.addItem("yogurt",  "dairy",                2.50, 0.5, 30);
		if (!s.getNameItem().containsKey("chicken")) s.addItem("chicken", "meat",                 7.00, 1.0, 20);
		//set thresholds for notifications (R9)
		s.addThreshold("apple", 10);
		s.addThreshold("yogurt", 5);
		s.addThreshold("chicken", 5);
		s.addObserver(new Supplier());
	}

	// ============== Manager commands ==============

	private static void handleRegisterCashier(String[] args) {
		if (!requireRole(Role.MANAGER, "registerCashier")) return;
		if (args.length != 4) {
			System.out.println("Usage: registerCashier <firstName> <lastName> <username> <password>");
			return;
		}
		try {
			int pw = Integer.parseInt(args[3]);
			system.registerCashier(args[0], args[1], args[2], pw);
			System.out.println("Cashier registered: " + args[2]);
		} catch (NumberFormatException e) {
			System.out.println("Password must be an integer.");
		}
	}

	private static void handleRegisterCustomer(String[] args) {
		if (!requireRole(Role.MANAGER, "registerCustomer")) return;
		// Two accepted forms:
		//   (a) plain, single-token address (per the brief):
		//       registerCustomer <firstName> <lastName> <username> <address> <password>
		//   (b) bracketed address that may contain spaces:
		//       registerCustomer <firstName> <lastName> <username> [<address>] <password>
		String joined = String.join(" ", args);
		int open  = joined.indexOf('[');
		int close = joined.indexOf(']');

		String firstName, lastName, username, address;
		String passwordToken;

		if (open >= 0 && close > open) {
			// Form (b): bracketed address
			address = joined.substring(open + 1, close).trim();
			String[] before = joined.substring(0, open).trim().split("\\s+");
			String[] after  = joined.substring(close + 1).trim().split("\\s+");
			if (before.length != 3 || after.length < 1 || after[0].isEmpty()) {
				printRegisterCustomerUsage();
				return;
			}
			firstName = before[0]; lastName = before[1]; username = before[2];
			passwordToken = after[0];
		} else if (args.length == 5) {
			// Form (a): plain, single-token address
			firstName = args[0]; lastName = args[1]; username = args[2];
			address = args[3]; passwordToken = args[4];
		} else {
			printRegisterCustomerUsage();
			return;
		}

		try {
			int pw = Integer.parseInt(passwordToken);
			// Auto-generate a test card for this customer.
			int cardNumber = 1000 + system.getCustomers().size() + 1;
			Card card = new Card(cardNumber, 0);
			bank.addCard(card);
			tas.addCardInfo(cardNumber, new Info(500.0, true));
			system.registerCustomer(firstName, lastName, username, address, pw, card);
			System.out.println("Customer registered: " + username + " (card " + cardNumber + ", PIN 0000)");
		} catch (NumberFormatException e) {
			System.out.println("Password must be an integer.");
		}
	}

	private static void printRegisterCustomerUsage() {
		System.out.println("Usage: registerCustomer <firstName> <lastName> <username> <address> <password>");
		System.out.println("  For an address with spaces, wrap it in square brackets, e.g.:");
		System.out.println("  registerCustomer Alice Martin alice [12 rue de la paix, 75001 paris] 4444");
	}

	private static void handleAddItem(String[] args) {
		if (!requireRole(Role.MANAGER, "addItem")) return;
		if (args.length != 5) {
			System.out.println("Usage: addItem <itemName> <categoryName> <unitPrice> <weight> <initialStock>");
			return;
		}
		try {
			String name = args[0];
			String category = args[1];
			double price = Double.parseDouble(args[2]);
			double weight = Double.parseDouble(args[3]);
			int stock = Integer.parseInt(args[4]);
			system.getMyStock().addItem(name, category, price, weight, stock);
			system.getMyStock().addThreshold(name, 10); // default threshold for new items
			System.out.println("Added " + name + " (" + category + ", " + price + "€).");
			System.out.println("Set default threshold for " + name + " to 10 units.");
		} catch (NumberFormatException e) {
			System.out.println("Invalid number in arguments.");
		}
	}

	private static void handleRestock(String[] args) {
		if (!requireRole(Role.MANAGER, "restock")) return;
		if (args.length != 2) {
			System.out.println("Usage: restock <itemName> <quantity>");
			return;
		}
		try {
			int qty = Integer.parseInt(args[1]);
			Map<String, Item> items = system.getMyStock().getNameItem();
			if (!items.containsKey(args[0])) { System.out.println("Unknown item: " + args[0]); return; }
			system.getMyStock().restock(args[0], qty);
			System.out.println("Restocked " + args[0] + " to " + qty);
		} catch (NumberFormatException e) {
			System.out.println("Quantity must be an integer.");
		}
	}

	private static void handleSetCategoryDiscount(String[] args) {
		if (!requireRole(Role.MANAGER, "setCategoryDiscount")) return;
		if (args.length != 2) {
			System.out.println("Usage: setCategoryDiscount <categoryName> <discountPercent>");
			return;
		}
		try {
			double pct = Double.parseDouble(args[1]);
			// Convert "10" (= 10%) into a multiplier (0.9) for the price.
			double multiplier = 1.0 - (pct / 100.0);
			system.setCatDis(args[0], multiplier);
			System.out.println("Category discount: " + args[0] + " → " + pct + "% off");
		} catch (NumberFormatException e) {
			System.out.println("Discount percent must be a number.");
		}
	}

	private static void handleShowInventory() {
		if (!requireRole(Role.MANAGER, "showInventory")) return;
		System.out.println("Inventory:");
		for (Item item : system.getMyStock().getNameItem().values()) {
			System.out.printf("  %s (%s): %.2f€  weight=%.2fkg  qty=%d%n",
				item.getName(), item.getCategory(), item.getPrice(),
				item.getWeight(), item.getQuantity());
		}
	}

	private static void handleShowRevenue() {
		if (!requireRole(Role.MANAGER, "showRevenue")) return;
		System.out.printf("Total revenue: %.2f€%n", system.getRevenue());
	}

	// ============== Customer commands ==============

	private static void handleSubscribeToPlan(String[] args) {
		if (!requireRole(Role.CUSTOMER, "subscribeToPlan")) return;
		if (args.length != 1) {
			System.out.println("Usage: subscribeToPlan <planName>");
			return;
		}
		CustomerPlan plan = system.getPlan(args[0]);
		if (plan == null) {
			System.out.println("Unknown plan: " + args[0]);
			return;
		}
		Customer customer = (Customer) currentUser;
		double annualFee = plan.getAnnualFee();
		// Check if customer is already on this plan (optional)
		if (customer.getDp().getClass().equals(plan.getClass())) {
			System.out.println("You are already subscribed to " + args[0]);
			return;
		}
		
		// Charge the annual fee if applicable
		if (annualFee > 0) {
			Card customerCard = customer.getCard();
			if (customerCard == null) {
				System.out.println("No card on file. Cannot charge annual fee.");
				return;
			}
			
			// Use the POS to charge the fee
			PaymentOutcome outcome = system.getPos().process(
				customerCard.getCardNumber(), 
				customerCard.getPin(), 
				annualFee
			);
			
			if (outcome != PaymentOutcome.SUCCESS) {
				System.out.println("Annual fee payment failed: " + outcome);
				System.out.println("Subscription cancelled.");
				return;
			}
			
			System.out.printf("Annual fee of %.2f EUR charged successfully.%n", annualFee);
		}
		customer.setDp(plan);
		System.out.println(currentUser.getUsername() + " subscribed to " + args[0]);
	}

	private static void handleRequestDelivery(String[] args) {
		if (!requireRole(Role.CUSTOMER, "requestDelivery")) return;

		// Address may contain spaces, so wrap it in square brackets:
		//   requestDelivery [<address>]
		// With no arguments, the customer's own registered address is used.
		// The distance is derived from the postal code by DeliveryService (R10),
		// so there is no fixed address book to validate against.
		String address;
		String joined = String.join(" ", args).trim();
		int open  = joined.indexOf('[');
		int close = joined.indexOf(']');
		if (open >= 0 && close > open) {
			address = joined.substring(open + 1, close).trim();
		} else if (joined.isEmpty()) {
			address = ((Customer) currentUser).getAddress();
		} else {
			address = joined; // accept a bare address too
		}

		if (address == null || address.isEmpty()) {
			System.out.println("Usage: requestDelivery [<address>]  (omit the address to use your registered one)");
			return;
		}

		// Delivery is a PENDING request applied to the next checkout (R7), so it
		// must be set BEFORE a checkout session is open.
		if (system.getSession() != null) {
			System.out.println("Cannot request delivery during an active checkout. Request it before startCheckout.");
			return;
		}

		((Customer) currentUser).setRequestDelivery(address);
		System.out.println("Delivery requested to: " + address);
		System.out.println("   The system will assign the best available time slot at checkout.");
	}

	// ============== Cashier commands ==============

	private static void handleStartCheckout(String[] args) {
		if (!requireRole(Role.CASHIER, "startCheckout")) return;
		if (system.getSession() != null) {
			System.out.println("A checkout is already in progress.");
			return;
		}
		if (args.length != 1) {
			System.out.println("Usage: startCheckout <customerUsername>");
			return;
		}
		if (!system.getCustomers().containsKey(args[0])) {
			System.out.println("Unknown customer: " + args[0]);
			return;
		}
		system.startCheckout(args[0]);
		System.out.println("Checkout started for " + args[0]);
	}

	private static void handleScanItem(String[] args) {
		if (!requireRole(Role.CASHIER, "scanItem")) return;
		if (system.getSession() == null) {
			System.out.println("No active checkout. Use startCheckout first.");
			return;
		}
		if (args.length != 2) {
			System.out.println("Usage: scanItem <itemName> <quantity>");
			return;
		}
		try {
			String name = args[0];
			int qty = Integer.parseInt(args[1]);
			if (!system.getMyStock().getNameItem().containsKey(name)) {
				System.out.println("Unknown item: " + name);
				return;
			}
			system.getSession().scanItem(name, qty);
			System.out.println("Scanned " + qty + " x " + name);
		} catch (NumberFormatException e) {
			System.out.println("Quantity must be an integer.");
		}
	}

	private static void handleComputeBill() {
		if (!requireRole(Role.CASHIER, "computeBill")) return;
		if (system.getSession() == null) {
			System.out.println("No active checkout.");
			return;
		}
		try {
			system.getSession().computeBill();
			System.out.printf("Total bill: %.2f€%n", system.getSession().getYourBill());
		} catch (IllegalStateException e) {
			// Raised e.g. when delivery weight exceeds the 50 kg limit (R8).
			System.out.println("Cannot compute bill: " + e.getMessage());
		}
	}

	private static void handlePay(String[] args) {
		if (!requireRole(Role.CASHIER, "pay")) return;
		if (system.getSession() == null) {
			System.out.println("No active checkout.");
			return;
		}
		if (args.length != 2) {
			System.out.println("Usage: pay <cardNumber> <pin>");
			return;
		}
		try {
			int cardnumber = Integer.parseInt(args[0]);
			int pin = Integer.parseInt(args[1]);
			double billBefore = system.getSession().getYourBill();
			PaymentOutcome outcome = system.getSession().pay(cardnumber, pin);
			System.out.println("Payment outcome: " + outcome);
			if (outcome == PaymentOutcome.SUCCESS) {
				system.addRevenue(billBefore);
				system.endSession();
				System.out.printf("Receipt printed. Total charged: %.2f€%n", billBefore);
			}
		} catch (NumberFormatException e) {
			System.out.println("Card number and PIN must be integers.");
		}
	}

	private static void handleSimulatePayment(String[] args) {
		if (!requireRole(Role.CASHIER, "simulatePayment")) return;
		if (args.length != 1) {
			System.out.println("Usage: simulatePayment <SUCCESS|PIN_WRONG|INSUFFICIENT_FUNDS|AUTH_DENIED>");
			return;
		}
		try {
			PaymentOutcome outcome = PaymentOutcome.valueOf(args[0]);
			system.getPos().simulateNext(outcome);
			System.out.println("Next pay outcome forced to: " + outcome);
		} catch (IllegalArgumentException e) {
			System.out.println("Invalid outcome. Use one of: SUCCESS, PIN_WRONG, INSUFFICIENT_FUNDS, AUTH_DENIED");
		}
	}

	// ============== Generic commands (no login required) ==============

	private static void handleRunTest(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: runTest <testScenario-file>");
			return;
		}
		String name = args[0];
		// Try several candidate locations so runTest works no matter what the
		// working directory is (project root, src, bin, or an absolute path).
		String[] candidates = {
			name,
			"src/supermarket/" + name,
			"bin/supermarket/" + name,
			System.getProperty("user.dir") + java.io.File.separator + name
		};
		java.io.File found = null;
		for (String path : candidates) {
			java.io.File f = new java.io.File(path);
			if (f.exists()) { found = f; break; }
		}
		if (found == null) {
			System.out.println("Could not find test file '" + name + "'.");
			System.out.println("Working directory is: " + System.getProperty("user.dir"));
			System.out.println("Put the file there (or in src/supermarket/), or pass a full path.");
			return;
		}
		System.out.println("Running test file: " + found.getAbsolutePath());
		try (BufferedReader br = new BufferedReader(new FileReader(found))) {
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#")) continue;
				System.out.println("> " + line);
				dispatchCommand(line);
			}
		} catch (IOException e) {
			System.out.println("Could not read test file: " + e.getMessage());
		}
	}

	private static void handleHelp() {
		System.out.println("Available commands:");
		System.out.println("  help");
		System.out.println("  login <username> <password>");
		System.out.println("  logout");
		System.out.println("  setup");
		System.out.println("  registerCashier <firstName> <lastName> <username> <password>      (manager)");
		System.out.println("  registerCustomer <firstName> <lastName> <username> <address> <password>  (manager)");
		System.out.println("  addItem <itemName> <categoryName> <unitPrice> <weight> <initialStock>    (manager)");
		System.out.println("  restock <itemName> <quantity>                                            (manager)");
		System.out.println("  setCategoryDiscount <categoryName> <discountPercent>                     (manager)");
		System.out.println("  subscribeToPlan <planName>                                               (customer)");
		System.out.println("  startCheckout <customerUsername>                                         (cashier)");
		System.out.println("  scanItem <itemName> <quantity>                                           (cashier)");
		System.out.println("  computeBill                                                              (cashier)");
		System.out.println("  pay <cardNumber> <pin>                                                   (cashier)");
		System.out.println("  simulatePayment <SUCCESS|PIN_WRONG|INSUFFICIENT_FUNDS|AUTH_DENIED>       (cashier)");
		System.out.println("  requestDelivery <address>                                                (customer)");
		System.out.println("  showInventory                                                            (manager)");
		System.out.println("  showRevenue                                                              (manager)");
		System.out.println("  runTest <testScenario-file>");
		System.out.println("  stop");
	}

	// ============== Helpers ==============

	/**
	 * Checks that the current role matches the required one. Prints a message
	 * and returns false if not. Centralised so every handler can do one-line
	 * authorisation: if (!requireRole(Role.MANAGER, "addItem")) return;
	 */
	private static boolean requireRole(Role required, String command) {
		if (currentRole != required) {
			System.out.println("Permission denied: " + command + " requires " + required + " login.");
			return false;
		}
		return true;
	}
}