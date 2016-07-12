package obj;

import java.security.PublicKey;

public class Output {
	
	private PublicKey recipientAddress;
	private int amount;
	
	public Output(PublicKey recipientAddress, int amount) {
		this.recipientAddress = recipientAddress;
		this.amount = amount;
	}
	
	public String toString() {
		return Integer.toString(amount) + "." + recipientAddress;	// TODO
	}
	
	public PublicKey recipientAddress() {
		return recipientAddress;
	}
	
	public int amount() {
		return amount;
	}

}
