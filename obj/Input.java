package obj;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateCrtKey;

import util.Signature;

/**
 * <b> References </b>
 * <ul>
 * <li> <a href = "http://www.java2s.com/Tutorial/Java/0490__Security/RSASignatureGeneration.htm">Java Tutorial: RSA Signature Generation</a></li>
 * </ul>
 */
public class Input {
	
	private TransactionReference reference;
	private RSAPrivateCrtKey senderPrivateKey;
	
	public Input(TransactionReference reference, RSAPrivateCrtKey privateKey) {
		this.reference = reference;
		this.senderPrivateKey = privateKey;
	}
	
	public TransactionReference reference() {
		return reference;
	}

	public byte[] sign(byte[] outputsInBytes) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		
		Signature signature = new Signature();
		return signature.sign(outputsInBytes, senderPrivateKey);
		
	}
	
}