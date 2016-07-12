package obj;

import java.security.PublicKey;

public class Reward {
	
	private int amount;
	private PublicKey rewardAddress;
	
	public Reward(int amount, PublicKey rewardAddress) {
		this.amount = amount;
		this.rewardAddress = rewardAddress;
	}
	
	public int amount() {
		return amount;
	}
	
	public PublicKey rewardAddress() {
		return rewardAddress;
	}

}
