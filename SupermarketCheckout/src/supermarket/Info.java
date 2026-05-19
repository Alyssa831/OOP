package supermarket;

public class Info {
	private double balance;
	private boolean auth=true;
	public boolean isAuth() {
		// TODO Auto-generated method stub
		return auth;
	}
	public Info(double balance, boolean auth) {
		super();
		this.balance = balance;
		this.auth = auth;
	}
	public double getBalance() {
		return balance;
	}
	public void setBalance(double balance) {
		this.balance = balance;
	}
	public void setAuth(boolean auth) {
		this.auth = auth;
	}
	
}
