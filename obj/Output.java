package obj;

import java.security.interfaces.RSAPublicKey;

import util.BaseConverter;

/**
 * <b>References</b>
 * <ul>
 * <li> <a href="http://www.javamex.com/tutorials/cryptography/rsa_encryption.shtml">RSA Encryption in Java</a></li>
 * </ul>
 */
public class Output {
	
	private RSAPublicKey recipientPublicKey;
	private String recipientAddress;
	private String amount;
	
	public Output(RSAPublicKey recipientPublicKey, String amount) {
		this.recipientPublicKey = recipientPublicKey;
		byte[] encodedPublicKey = recipientPublicKey.getEncoded(); 
		this.recipientAddress = BaseConverter.bytesDecToHex(encodedPublicKey);
		this.amount = amount;
	}
	
	public String toString() {
		return amount + " to " + recipientPublicKey.getModulus().toString(16);
	}
	
	public RSAPublicKey recipientPublicKey() {
		return recipientPublicKey;
	}
	
	public String recipientAddress() {
		return recipientAddress;
	}
	
	public String amount() {
		return amount;
	}

}