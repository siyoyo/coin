package obj;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

/**
 * <b>References</b>
 * <ul>
 * <li> <a href="http://www.javamex.com/tutorials/cryptography/rsa_encryption.shtml">RSA Encryption in Java</a></li>
 * </ul>
 */
public class Output {
	
	public final static String ALGORITHM = "RSA";
	private PublicKey recipientAddress;
	private int amount;
	
	public Output(PublicKey recipientAddress, int amount) {
		this.recipientAddress = recipientAddress;
		this.amount = amount;
	}
	
	public String toString() {
		return Integer.toString(amount) + "." + recipientAddress;	// TODO
	}
	
	public String recipientAddress() throws NoSuchAlgorithmException, InvalidKeySpecException {
		
		KeyFactory factory = KeyFactory.getInstance(ALGORITHM);
		RSAPublicKeySpec publicKey = factory.getKeySpec(recipientAddress, RSAPublicKeySpec.class);
		String address = new String (publicKey.getModulus().toString(16));
		return address;
		
	}
	
	public int amount() {
		return amount;
	}

}
