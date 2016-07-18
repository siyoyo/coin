package obj;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import util.Signature;

/**
 * <b> References </b>
 * <ul>
 * <li> <a href = "http://www.java2s.com/Tutorial/Java/0490__Security/RSASignatureGeneration.htm">Java Tutorial: RSA Signature Generation</a></li>
 * </ul>
 */
public class Input {
	
	private TransactionReference reference;
	private PrivateKey privateKey;
	
	public Input(TransactionReference reference, PrivateKey privateKey) {
		this.reference = reference;
		this.privateKey = privateKey;
	}
	
	public TransactionReference reference() {
		return reference;
	}

	public byte[] sign(byte[] outputsInBytes) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		
		Signature signature = new Signature();
		return signature.sign(outputsInBytes, privateKey);
		
	}
	
}