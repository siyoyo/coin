package util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;

public class Signature {

	public final static String SIGNATURE_ALGORITHM = "SHA256withRSA";
	private java.security.Signature signature;
	
	public Signature() {
		try {
			signature = java.security.Signature.getInstance(SIGNATURE_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	public byte[] sign(byte[] outputsInBytes, RSAPrivateCrtKey privateKey) throws InvalidKeyException, SignatureException {
		signature.initSign(privateKey, new SecureRandom());
		signature.update(outputsInBytes);
		return signature.sign();
	}
	
	public boolean verify(byte[] outputsInBytes, byte[] signedBytes, RSAPublicKey publicKey) throws InvalidKeyException, SignatureException {
		signature.initVerify(publicKey);
		signature.update(outputsInBytes);
		return signature.verify(signedBytes);
	}
	
}