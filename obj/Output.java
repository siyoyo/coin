package obj;

import java.security.interfaces.RSAPublicKey;

/**
 * <b>References</b>
 * <ul>
 * <li> <a href="http://www.javamex.com/tutorials/cryptography/rsa_encryption.shtml">RSA Encryption in Java</a></li>
 * </ul>
 */
public class Output {
	
	private RSAPublicKey recipientPublicKey;
	private String amount;
	
	public Output(RSAPublicKey recipientAddress, String amount) {
		this.recipientPublicKey = recipientAddress;
		this.amount = amount;
	}
	
	public String toString() {
		return amount + " to " + recipientPublicKey.getModulus().toString(16);
	}
	
	public RSAPublicKey recipientAddress() {
		return recipientPublicKey;
	}
	
	public String amount() {
		return amount;
	}

}