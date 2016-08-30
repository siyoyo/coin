package test;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;

import util.BaseConverter;
import util.WalletExplorer;

public class WalletRunTest {
	
	public static void main (String[] args) {
		
		try {
			
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(512, new SecureRandom());
			
			KeyPair keyPair = generator.generateKeyPair();
			
			WalletExplorer wallet = new WalletExplorer("dat/wallet.xml");
			wallet.save(keyPair, "50");
			
			byte[] encoded = keyPair.getPublic().getEncoded();
			String hex = BaseConverter.bytesDecToHex(encoded);
			RSAPrivateKey privateKey = wallet.privateKey(hex);
			
			Signature signature = Signature.getInstance("SHA256withRSA");
			String plaintext = "crypto";
			
			signature.initSign(privateKey);
			signature.update(plaintext.getBytes());
			byte[] signBytes = signature.sign();
			
			signature.initVerify(keyPair.getPublic());
			signature.update(plaintext.getBytes());
			System.out.println(signature.verify(signBytes));
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		
	}

}
