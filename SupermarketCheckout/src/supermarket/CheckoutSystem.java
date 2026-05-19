package supermarket;

import java.util.HashMap;
import java.util.Map;

public class CheckoutSystem {
	//cash register (which concerns processinsg of the bought items and computation of the total bill)
	//the customer (the individual that is willing to buy items from the supermarket) 
	//the bank payment system (which concerns the management of the bank transaction to pay the bill)
	private Map<String,Customer> customers=new HashMap<>();
	public Map<String, Customer> getCustomers() {
		return customers;
	}
	private CashRegister session;
	private Map<String,Cashier> cashiers=new HashMap<>();
	private Map<String,Manager> manager=new HashMap<>();
	private PricingPolicy pp;
	private Stock myStock=new Stock();
	private double revenue=0;
	private Bank bank;
	private TAS tas;
	public Stock getMyStock() {
		return myStock;
	}
	public void setMyStock(Stock myStock) {
		this.myStock = myStock;
	}
	private POS pos;
	
	public CheckoutSystem() {
		// TODO Auto-generated constructor stub
	}
	public CheckoutSystem(Bank bank, TAS tas) {
		super();
		this.bank = bank;
		this.tas = tas;
		this.pos=new POS(bank,tas);
	}
	public void setCatDis(String cat, double dis) {
		pp=new CatDis();
		pp.setCatDis(cat,dis);
	}
	public void startCheckout(String name) {
		Customer customer=customers.get(name);
		session=new CashRegister(customer, myStock, pp,pos);
	}
	public CashRegister getSession() {
		return session;
	}

	public POS getPos() {
		return pos;
	}

}
