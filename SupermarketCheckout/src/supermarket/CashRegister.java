package supermarket;

import java.util.HashMap;
import java.util.Map;

/**
 * One customer's checkout in progress. Holds the cart and the running bill.
 *
 * Note on naming: this class is really a "checkout session" — its lifetime
 * is one customer's purchase. The name CashRegister is kept for now but a
 * future rename to CheckoutSession would be more accurate.
 */
public class CashRegister {
	private Customer customer;
	private Map<Item,Integer> yourItems=new HashMap<>();
	private double yourBill=0.0;
	private Stock myStock;
	private PricingPolicy pp;
	private POS pos;

	public CashRegister(Customer customer, Stock myStock, PricingPolicy pp, POS pos) {
		super();
		this.customer = customer;
		this.myStock = myStock;
		this.pp = pp;
		this.pos = pos;
	}

	public void scanItem(String name, int q) {
		yourItems.put(myStock.getNameItem().get(name), q);
	}

	/**
	 * Design decision (composition order): category pricing policy is applied
	 * first to each item, then the customer's plan discount is applied to the
	 * resulting subtotal. See R5/R6 — the brief is silent on order, this is
	 * the choice documented in the report.
	 */
	public void computeBill() {
		yourBill = 0.0;
		for (Item item : yourItems.keySet()) {
			yourBill += pp.priceAfterDiscount(item);
		}
		yourBill = customer.getDp().billAfterDiscount(yourBill);
	}

	/**
	 * Run the payment. Tell-Don't-Ask: we ask POS for one outcome,
	 * then act on it. Inventory is decremented and the cart cleared
	 * ONLY on SUCCESS — every other outcome leaves state untouched
	 * so the cashier can retry or correct.
	 */
	public PaymentOutcome pay(int cardnumber, int pin) {
		PaymentOutcome result = pos.process(cardnumber, pin, yourBill);
		if (result == PaymentOutcome.SUCCESS) {
			for (Item item : yourItems.keySet()) {
				myStock.sold(item, yourItems.get(item));
			}
			yourItems.clear();
			yourBill = 0;
		}
		return result;
	}

	public double getYourBill() {
		return yourBill;
	}

	public Stock getMyStock() {
		return myStock;
	}
}
