package test;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class SignatureRunTest {
	
	public static void main (String[] args) {
		
		try {
			
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(512, new SecureRandom());
			KeyPair keyPair = generator.generateKeyPair();
			
			RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
			RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey) keyPair.getPrivate();
			
			byte[] encodedPublicKey = publicKey.getEncoded();
			byte[] encodedPrivateKey = privateKey.getEncoded();
			
			System.out.println("PUBLIC");
			System.out.println(publicKey.getFormat());
			System.out.println("length: " + encodedPublicKey.length);
			for (int i = 0; i < encodedPublicKey.length; i++) {
				System.out.print(encodedPublicKey[i] + " ");
			}
			
			System.out.println("--------------------------");
			
			System.out.println("PRIVATE");
			System.out.println(privateKey.getFormat());
			System.out.println("length: " + encodedPrivateKey.length);
			for (int i = 0; i < encodedPrivateKey.length; i++) {
				System.out.print(encodedPrivateKey[i] + " ");
			}
			
			System.out.println("--------------------------");
			
			KeyFactory factory = KeyFactory.getInstance("RSA");
			
			X509EncodedKeySpec encodedPublicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
			RSAPublicKey reconstructedPublicKey = (RSAPublicKey) factory.generatePublic(encodedPublicKeySpec);
			
			PKCS8EncodedKeySpec encodedPrivateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
			RSAPrivateCrtKey reconstructedPrivateKey = (RSAPrivateCrtKey) factory.generatePrivate(encodedPrivateKeySpec);
			
			byte[] plaintext = "hello world!".getBytes();
			
			Signature signature = Signature.getInstance("SHA256withRSA");
			
			signature.initSign(privateKey, new SecureRandom());
			signature.update(plaintext);
			byte[] signedBytes = signature.sign();
			
			signature.initVerify(reconstructedPublicKey);
			signature.update(plaintext);
			System.out.println("test1: " + signature.verify(signedBytes));
			
			signature.initSign(reconstructedPrivateKey, new SecureRandom());
			signature.update(plaintext);
			signedBytes = signature.sign();
			
			signature.initVerify(publicKey);
			signature.update(plaintext);
			System.out.println("test2: " + signature.verify(signedBytes));
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}