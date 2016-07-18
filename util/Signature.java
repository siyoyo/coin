package util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import obj.BlockExplorer;
import obj.Input;
import obj.Transaction;
import obj.TransactionReference;

public class Signature {

	private final static String ALGORITHM = "SHA256withRSA";
	private java.security.Signature signature;
	
	public Signature() throws NoSuchAlgorithmException {
		signature = java.security.Signature.getInstance(ALGORITHM);
	}
	
	
	public byte[] sign(byte[] bytes, PrivateKey privateKey) throws InvalidKeyException, SignatureException {
		
		signature.initSign(privateKey, new SecureRandom());
		signature.update(bytes);
		
		return signature.sign();
	}
	
	public boolean validate(BlockExplorer explorer, ArrayList<Transaction> transactions) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException, ValidationFailureException {
		
		boolean passedValidation = false;
		
		RSA512 rsa512 = new RSA512();
		
		// For each transaction
		for (int i = 0; i < transactions.size(); i++) {
			
			Transaction tx = transactions.get(i);
			byte[][] signatures = tx.signatures();
			ArrayList<Input> inputs = tx.inputs();
			byte[] outputsInBytes = tx.getOutputsInBytes();
			
			// For each input private key
			for (int j = 0; j < inputs.size(); j++) {
				
				TransactionReference reference = inputs.get(j).reference();
				String recipientAddress = explorer.recipientAddress(reference);
				
				signature.initVerify(rsa512.retrievePublicKey(recipientAddress));
				signature.update(outputsInBytes);
				
				passedValidation = signature.verify(signatures[j]);
				if (passedValidation != true) {
					throw new ValidationFailureException();
				}
			}		
		}
		
		return passedValidation;
	}
	
	@SuppressWarnings("serial")	// TODO Serialize
	public class ValidationFailureException extends Exception {
		
		public ValidationFailureException() {
			super();
		}
		
		public ValidationFailureException(String msg) {
			super(msg);
		}
		
	}
	
}