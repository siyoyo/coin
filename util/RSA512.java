package util;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class RSA512 {
	
	private final static String KEY_ALGORITHM = "RSA";
	
	public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
		
		KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
		generator.initialize(512, new SecureRandom());
		
		return generator.generateKeyPair();	
	}
	
	public static RSAPublicKey decodePublicKey(String address) throws NoSuchAlgorithmException, InvalidKeySpecException {
		
		byte[] encoded = BaseConverter.stringHexToDec(address);
		X509EncodedKeySpec encodedPublicKeySpec = new X509EncodedKeySpec(encoded);
		KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
		RSAPublicKey publicKey = (RSAPublicKey) factory.generatePublic(encodedPublicKeySpec);
		
		return publicKey;
	}

}
