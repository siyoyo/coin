package obj;

import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;

import util.BaseConverter;
import util.SHA256;

/**
 * <b>References</b>
 * <ul>
 * <li> <a href="http://www.javamex.com/tutorials/cryptography/rsa_encryption.shtml">RSA Encryption in Java</a></li>
 * </ul>
 */
public class Output {
	
	private RSAPublicKey recipientPublicKey;
	private String recipientPublicKeyString;
	private String recipientAddress;
	private String amount;
	
	public Output(RSAPublicKey recipientPublicKey, String amount) {
		
		this.recipientPublicKey = recipientPublicKey;
		byte[] encodedPublicKey = recipientPublicKey.getEncoded(); 
		this.recipientPublicKeyString = BaseConverter.bytesDecToHex(encodedPublicKey);
		this.amount = amount;
		
		try {
			SHA256 sha256 = new SHA256();
			recipientAddress = sha256.hashString(recipientPublicKeyString);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
	}
	
	public String toString() {
		return amount + " to " + recipientAddress;
	}
	
	public RSAPublicKey recipientPublicKey() {
		return recipientPublicKey;
	}
	
	public String recipientPublicKeyString() {
		return recipientPublicKeyString;
	}
	
	public String recipientAddress() {
		return recipientAddress;
	}
	
	public String amount() {
		return amount;
	}

}