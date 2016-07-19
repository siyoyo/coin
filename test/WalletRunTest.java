package test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import obj.Wallet;
import util.BaseConverter;

public class WalletRunTest {
	
	public static void main (String[] args) {
		
		try {
			
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(512, new SecureRandom());
			
			KeyPair keyPair = generator.generateKeyPair();
			
			Wallet wallet = new Wallet();
			wallet.save(keyPair, "50");
			
			byte[] encoded = keyPair.getPublic().getEncoded();
			String hex = BaseConverter.bytesDecToHex(encoded);
			RSAPrivateCrtKey privateKey = wallet.privateKey(hex);
			
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
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		
	}

}
