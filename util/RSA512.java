package util;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

public class RSA512 {
	
	private final static String ALGORITHM = "RSA";
	private BigInteger exponent = new BigInteger("65537", 10);
	
	public KeyPair generateKeyPair() throws NoSuchAlgorithmException {
		
		KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
		generator.initialize(512, new SecureRandom());
		
		return generator.generateKeyPair();	
	}

	public PublicKey retrievePublicKey(String recipientAddress) throws NoSuchAlgorithmException, InvalidKeySpecException {
		
		BigInteger address = new BigInteger(recipientAddress, 16);
		RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(address, exponent);
		KeyFactory factory = KeyFactory.getInstance("RSA");
		
		return factory.generatePublic(publicKeySpec);
	}
}
