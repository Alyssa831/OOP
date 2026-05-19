package supermarket;

import java.util.HashMap;
import java.util.Map;

public class TAS {
	private Map<Integer,Info> card_info = new HashMap<>();
	public boolean isAuth(int cardNumber) {
		return card_info.get(cardNumber).isAuth();
	}
	public boolean checkBalance(int cardNumber, double bill) {
		if(card_info.get(cardNumber).getBalance()<bill) return false;
		return true;
	}
	public void setBalance(int cardNumber, double bill) {
		card_info.get(cardNumber).setBalance(card_info.get(cardNumber).getBalance()-bill);
	}
	public void addCardInfo(Integer cardNumber, Info info) {
		card_info.put(cardNumber,info);
	}
}
