package obj;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

public class Wallet {
	
	public final static String WALLET = "dat/wallet.xml"; 
	private Hashtable<KeyPair, Integer> keyPairs;
	
	public Wallet() {
		keyPairs = new Hashtable<KeyPair, Integer>();
	}
	
	public void save(KeyPair keyPair, int amount) {
		
		if (keyPairs.containsKey(keyPair)) {
			int newTotal = keyPairs.get(keyPair).intValue() + amount;
			keyPairs.put(keyPair, new Integer(newTotal));
		} else keyPairs.put(keyPair, new Integer(amount));
		
	}
	
	public String getBalances() {
		return balances();
	}
	
	/*
	 * Private methods
	 */
	private String balances() {
		
		StringBuilder str = new StringBuilder();
		Iterator<Entry<KeyPair, Integer>> itr = keyPairs.entrySet().iterator();
		
		while (itr.hasNext()) {
			Entry<KeyPair, Integer> entry = itr.next();
			PublicKey publicKey = (PublicKey) entry.getKey().getPublic();
			int balance = entry.getValue().intValue();
			str.append(publicKey + "\t" + balance + "\n");
		}
		
		return str.toString();
	}

}
