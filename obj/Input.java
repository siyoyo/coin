package obj;

import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPrivateKey;

import util.Signature;

/**
 * <b> References </b>
 * <ul>
 * <li> <a href = "http://www.java2s.com/Tutorial/Java/0490__Security/RSASignatureGeneration.htm">Java Tutorial: RSA Signature Generation</a></li>
 * </ul>
 */
public class Input {
	
	private TransactionReference reference;
	private RSAPrivateKey senderPrivateKey;
	private int inputID;
	
	public Input(TransactionReference reference) {
		this.reference = reference;
	}
	
	public Input(TransactionReference reference, RSAPrivateKey privateKey) {
		this.reference = reference;
		this.senderPrivateKey = privateKey;
	}
	
	public TransactionReference reference() {
		return reference;
	}
	
	public String inputID() {
		return String.valueOf(inputID);
	}
	
	public void setInputID(int inputID) {
		this.inputID = inputID;
	}

	public byte[] sign(byte[] outputsInBytes) throws InputException {
		Signature signature = new Signature();
		
		byte[] signBytes;
		try {
			signBytes = signature.sign(outputsInBytes, senderPrivateKey);
		} catch (GeneralSecurityException e) {
			throw new InputException(e.getMessage());
		}
		return signBytes;
	}
	
	@SuppressWarnings("serial") // TODO
	public class InputException extends Exception {
		
		public InputException() {
			super();
		}
		
		public InputException(String msg) {
			super(msg);
		}
		
	}
}