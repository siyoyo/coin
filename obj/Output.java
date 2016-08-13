package obj;

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
	
	private RSAPublicKey publicKey;
	private String publicKeyString;
	private String outputAddress;
	private String amount;
	private int outputID;
	
	public Output(RSAPublicKey recipientPublicKey, String amount) {
		
		this.publicKey = recipientPublicKey;
		byte[] encodedPublicKey = recipientPublicKey.getEncoded(); 
		this.publicKeyString = BaseConverter.bytesDecToHex(encodedPublicKey);
		
		this.amount = amount;
		
		SHA256 sha256 = new SHA256();
		outputAddress = sha256.hashString(publicKeyString);
	}
	
	public String toString() {
		return amount + " to " + outputAddress;
	}
	
	public RSAPublicKey publicKey() {
		return publicKey;
	}
	
	public String recipientPublicKeyString() {
		return publicKeyString;
	}
	
	public String outputAddress() {
		return outputAddress;
	}
	
	public String amount() {
		return amount;
	}
	
	public String outputID() {
		return String.valueOf(outputID);
	}
	
	public void setOutputID(int outputID) {
		this.outputID = outputID;
	}

}