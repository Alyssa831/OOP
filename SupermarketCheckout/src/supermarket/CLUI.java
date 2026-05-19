package supermarket;
import java.util.Scanner;


import java.util.Arrays;

public class CLUI {
	static Bank bank=new Bank();
	static TAS tas=new TAS();
	static CheckoutSystem myCheckoutSystem=new CheckoutSystem(bank,tas);
	static int user=0;
	static boolean checkoutstarted=false;
    public static void main(String[] args) {
    	
    	
        Scanner scanner = new Scanner(System.in);
        String input;
        
        System.out.println("This is a  Command Line Interface");
        System.out.println("Available commands:");
        System.out.println("  HELP - Show this help message");
        System.out.println("  login <username> <password>");
        System.out.println("  setup <>");
        System.out.println("  STOP - Exit the program");
        System.out.println("Enter your commands below:");
        
        
        
        while (true) {
            System.out.print("> ");
            input = scanner.nextLine().trim();
            
            if (input.equalsIgnoreCase("STOP")) {
                System.out.println("Exiting the program...");
                	user=0;
                break;
            }
            
            if (input.isEmpty()) {
                continue;
            }
            
            
            //user = 0 no login
            //user = 1 manager
            //user = 2 cashier
            //user = 3 customer
            
            // Split input into command and arguments
            String[] parts = input.split("\\s+");
            String command = parts[0].toUpperCase();
            String[] arguments = Arrays.copyOfRange(parts, 1, parts.length);
            switch (command) {
	      
	            case "LOGIN":
	                handleLogin(arguments); // anyone (when logged out)
	                break;
	            case "SETUP":
	                handleSetup();          // anyone
	                break;
	            case "LOGOUT":
	               // handleLogout();         // any logged-in user
	                break;
	            case "ADDITEM":
	                handleAddItem(arguments);
	                break;
	            case "SETCATEGORYDISCOUNT":
	                //handleSetCategoryDiscount(arguments);
	                break;
	            case "SCANITEM":
	                //handleScanItem(arguments);
	                break;
	            case "SIMULATEPAYMENT":
	                handleSimulatePayment(arguments);
	                break;
	            case "STARTCHECKOUT":
	                handleCheckout(arguments);
	                break;
	            default:
                    System.out.println("Unknown command: " + command);
                    System.out.println("Type HELP for available commands");
            }
        }
        
        scanner.close();
    }
    
    private static void handleLogin(String[] args) {
    	if (user != 0) { 
            System.out.println("You are already logged on.");
            return;
        }
    	 
    	if (args.length != 2) {
            System.out.println("Usage: login <username> <password>");
            return;
        }
    	else {
    		 int password=Integer.parseInt(args[1]);
    		if(args[0].equals("ceo")) {
    			if(password==12345) {
    				handleManager();
    			}
    			else {
    				System.out.println("Wrong password.");
    			}
    		}
    		if(myCheckoutSystem.getCustomers().containsKey(args[0])){
    			if(password==myCheckoutSystem.getCustomers().get(args[0]).getPassword()) {
    				handleManager();
    			}
    			else {
    				System.out.println("Wrong password.");
    			}
    		}
    		
    		
    	}
    }
    private static void handleManager() {
    	user=1;
    	System.out.println("Manager logged in.");
    	System.out.println("Available commands:");
    	System.out.println("  addItem <itemName> <categoryName> <unitPrice> <weight> <initialStock>");
        System.out.println("  setCategoryDiscount <categoryName> <discountPercent>");
        System.out.println("  registerCashier <firstName> <lastName> <username> <password>");
        System.out.println("  setCategoryDiscount <categoryName> <discountPercent>");
        System.out.println("  registerCustomer <firstName> <lastName> <username> <address> <password>");
        System.out.println("  logout <>");
    }
    private static void handleSetup() {
    	//default catalogue with the three mandatory item categories of R2b, one cashier, one
    	//customer, default discount plans prime/platinum and pre-registered test bank cards
    	//discount plans are not set at runtime. already built as classes
    	bank.addCardPin(1234,0000);
    	tas.addCardInfo(1234,new Info(0,true));
    	
    	//myCheckoutSystem.manager.put("ceo",new Manager());
    	myCheckoutSystem.getCustomers().put("default customer",new Customer());
    	//myCheckoutSystem.cashiers.put("default cashier",new Cashier());
    	
    }
    /**
     * simulatePayment arms a one-shot override on the POS so the next
     * pay call returns the given outcome — without manipulating bank state.
     * It does not perform any payment side effects itself; pay() does.
     */
    private static void handleSimulatePayment(String[] args) {
    	if (user != 2) {  // cashier only
            System.out.println("Permission denied: simulatePayment requires cashier login.");
            return;
        }
        if (args.length != 1) {
            System.out.println("Usage: simulatePayment <SUCCESS|PIN_WRONG|INSUFFICIENT_FUNDS|AUTH_DENIED>");
            return;
        }
        try {
            PaymentOutcome outcome = PaymentOutcome.valueOf(args[0]);
            myCheckoutSystem.getPos().simulateNext(outcome);
            System.out.println("Next pay outcome forced to: " + outcome);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid outcome. Use one of: SUCCESS, PIN_WRONG, INSUFFICIENT_FUNDS, AUTH_DENIED");
        }
    }
    private static void handleCheckout(String[] args) {
    	if (checkoutstarted==true) {  
            System.out.println(" already in a checkout session.");
            return;
        }if (user != 2) {  // cashier only
            System.out.println("Permission denied: addItem requires cashier login.");
            return;
        }
        if (args.length != 1) {
            System.out.println("Usage: ");
            return;
        }
    	else {
    		myCheckoutSystem.startCheckout(args[0]);
    	}
	}
    private static void handleAddItem(String[] args) {
    	if (user != 1) {  // manager only
            System.out.println("Permission denied: addItem requires manager login.");
            return;
        }
        if (args.length != 5) {
            System.out.println("Usage: addItem <itemName> <categoryName> <unitPrice> <weight> <initialStock>");
            return;
        }
        else {
        	Stock myStock=myCheckoutSystem.getMyStock();
        	myStock.addItem(args[0], args[1], Double.parseDouble(args[2]),Double.parseDouble(args[3]), Integer.parseInt(args[2])) ;
        	myCheckoutSystem.setMyStock(myStock);
        	System.out.println("Successfully added item.");
        	handleManager();
        }
        
    }
}