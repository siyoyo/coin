package obj;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import util.BaseConverter;
import util.SHA256;
import util.XMLio;

public class Wallet {
	
	public final static String WALLET = "dat/wallet.xml";
	public final static String KEY_ALGORITHM = "RSA";
	public Document doc;
	
	public Wallet() {
		doc = XMLio.parse(WALLET);
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
		RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey) keyPair.getPrivate();
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
		XMLio.write(WALLET, doc, doc.getDocumentElement());
	}
	
	/**
	 * Returns the private key matching the given address.
	 * @param address hash of the public key string
	 * @return RSA private key
	 */
	public RSAPrivateCrtKey privateKey(String address) {
		
		Element keyElement = (Element) getKeyNode(address);
		Node privateNode = keyElement.getElementsByTagName("private").item(0);
		
		String privateKeyAsString = privateNode.getTextContent();
		
		byte[] encodedPrivateKey = BaseConverter.stringHexToDec(privateKeyAsString); 
		PKCS8EncodedKeySpec encodedPrivateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
		
		KeyFactory factory;
		RSAPrivateCrtKey privateKey = null;
		try {
			factory = KeyFactory.getInstance(KEY_ALGORITHM);
			privateKey = (RSAPrivateCrtKey) factory.generatePrivate(encodedPrivateKeySpec);
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
		
		XMLio.write(WALLET, doc, doc.getDocumentElement());
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