package util;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WalletExplorer {
	
	public final static String KEY_ALGORITHM = "RSA";
	
	private String filename;
	private Document doc;
	
	public WalletExplorer(String filename) {
		this.filename = filename;
		doc = XMLio.parse(this.filename);
		doc.getDocumentElement().normalize();
	}
	
	/**
	 * Updates the balance of the provided address, if the address already exists.
	 * Creates a new wallet entry, otherwise.
	 * @param keyPair key pair
	 * @param amount amount
	 */
	public void save(KeyPair keyPair, String amount) {
		
		// Check if this key entry already exists
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		byte[] encodedPublicKey = publicKey.getEncoded();
		String publicKeyAsString = BaseConverter.bytesDecToHex(encodedPublicKey);
		
		SHA256 sha256 = new SHA256();
		String address = sha256.hashString(publicKeyAsString);
		
		NodeList addressNodes = doc.getElementsByTagName("address");
		Node keyNode, addressNode, publicNode, privateNode, amountNode;
		Element keyElement = null;
		
		int sum = 0;
		
		for (int i = 0; i < addressNodes.getLength(); i++) {
			
			addressNode = addressNodes.item(i);
			
			if (addressNode.getTextContent().compareTo(address) == 0) {
				
				keyElement = (Element) addressNode.getParentNode();
				amountNode = keyElement.getElementsByTagName("amount").item(0);
				sum = Integer.valueOf(amountNode.getTextContent()) + Integer.valueOf(amount);
				amountNode.setTextContent(String.valueOf(sum));
				return;
			}
		}
		
		// Create new wallet entry if new address
		keyNode = doc.createElement("key");
		
		// Store address
		addressNode = doc.createElement("address");
		addressNode.setTextContent(address);
		keyNode.appendChild(addressNode);
		
		// Store public key
		publicNode = doc.createElement("public");
		publicNode.setTextContent(publicKeyAsString);
		keyNode.appendChild(publicNode);
		
		// Store private key
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		byte[] encodedPrivateKey = privateKey.getEncoded();
		String privateKeyAsString = BaseConverter.bytesDecToHex(encodedPrivateKey);
		
		privateNode = doc.createElement("private");
		privateNode.setTextContent(privateKeyAsString);
		keyNode.appendChild(privateNode);
		
		// Store balance
		amountNode = doc.createElement("amount");
		amountNode.setTextContent(amount);
		keyNode.appendChild(amountNode);
		
		doc.getDocumentElement().appendChild(keyNode);
		XMLio.write(filename, doc, doc.getDocumentElement());
	}
	
	
	
	public RSAPublicKey publicKey(String address) {
		
		Element keyElement = (Element) getKeyNode(address);
		Node publicNode = keyElement.getElementsByTagName("public").item(0);
		
		String publicKeyAsString = publicNode.getTextContent();
		
		byte[] encodedPublicKey = BaseConverter.stringHexToDec(publicKeyAsString); 
		X509EncodedKeySpec encodedPublicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
		
		KeyFactory factory;
		RSAPublicKey publicKey = null;
		try {
			factory = KeyFactory.getInstance(KEY_ALGORITHM);
			publicKey = (RSAPublicKey) factory.generatePublic(encodedPublicKeySpec);
		}  catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		
		return publicKey;
		
	}
	
	/**
	 * Returns the private key matching the given address.
	 * @param address hash of the public key string
	 * @return RSA private key
	 */
	public RSAPrivateKey privateKey(String address) {
		
		Element keyElement = (Element) getKeyNode(address);
		Node privateNode = keyElement.getElementsByTagName("private").item(0);
		
		String privateKeyAsString = privateNode.getTextContent();
		
		byte[] encodedPrivateKey = BaseConverter.stringHexToDec(privateKeyAsString); 
		PKCS8EncodedKeySpec encodedPrivateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
		
		KeyFactory factory;
		RSAPrivateKey privateKey = null;
		try {
			factory = KeyFactory.getInstance(KEY_ALGORITHM);
			privateKey = (RSAPrivateKey) factory.generatePrivate(encodedPrivateKeySpec);
		}  catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		
		return privateKey;
	}
	
	public void updateBalance(String address, int amount) {
		
		Element keyNode = (Element) getKeyNode(address);
		Node amountNode = keyNode.getElementsByTagName("amount").item(0);
		
		String balance = amountNode.getTextContent();
		int updatedBalance = Integer.valueOf(balance) + amount;
		amountNode.setTextContent(String.valueOf(updatedBalance));
		
		XMLio.write(filename, doc, doc.getDocumentElement());
	}
	
	/* -----------------------------------------------------------------
	 * 							Private methods
	 * -----------------------------------------------------------------
	 */
	private Node getKeyNode(String address) {
		
		NodeList addressNodes = doc.getElementsByTagName("address");
		Node addressNode, keyNode = null;
		
		for (int i = 0; i < addressNodes.getLength(); i++) {
			addressNode = addressNodes.item(i);
			
			if (addressNode.getTextContent().compareTo(address) == 0) {
				keyNode = addressNode.getParentNode();
				return keyNode;
			}
		}
		return keyNode;
	}
	
}