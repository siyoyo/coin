package obj;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import util.BaseConverter;
import util.XMLio;

public class Wallet {
	
	public final static String WALLET = "dat/wallet.xml";
	public final static String KEY_ALGORITHM = "RSA";
	public Document doc;
	
	public Wallet() throws ParserConfigurationException, SAXException, IOException, URISyntaxException {
		doc = XMLio.parse(WALLET);
		doc.getDocumentElement().normalize();
	}
	
	public void save(KeyPair keyPair, String amount) throws NoSuchAlgorithmException, TransformerException, ParserConfigurationException, SAXException, IOException, URISyntaxException {
		
		// Check if this key entry already exists
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		byte[] encoded = publicKey.getEncoded();
		
		String value = BaseConverter.bytesDecToHex(encoded);
		
		NodeList keys = doc.getElementsByTagName("key");
		
		for (int i = 0; i < keys.getLength(); i++) {
			
			Node node = keys.item(i);
			node = node.getFirstChild();						// public node
			
			if (node.getTextContent().compareTo(value) == 0) {	// if public key is a match
				System.out.println("ugh");
				node = node.getNextSibling().getNextSibling();	// private node
				node = node.getNextSibling().getNextSibling();	// balance node
				int sum = Integer.parseInt(node.getTextContent()) + Integer.parseInt(amount);
				node.setTextContent(String.valueOf(sum));		// update balance
				
				return;
			}
		}
		
		Element key = doc.createElement("key");
		
		// Store public key
		Element element = doc.createElement("public");
		element.setTextContent(value);
		key.appendChild(element);
		
		// Store private key
		RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey) keyPair.getPrivate();
		encoded = privateKey.getEncoded();
		value = BaseConverter.bytesDecToHex(encoded);
		
		element = doc.createElement("private");
		element.setTextContent(value);
		key.appendChild(element);
		
		// Store balance
		element = doc.createElement("balance");
		element.setTextContent(amount);
		key.appendChild(element);
		
		doc.getDocumentElement().appendChild(key);
		XMLio.write(WALLET, doc, doc.getDocumentElement());
	}
	
	public RSAPrivateCrtKey privateKey(String encodedPublicKeyInHex) throws NoSuchAlgorithmException, InvalidKeySpecException {
		
		NodeList publicKeys = doc.getElementsByTagName("public");
		
		for (int i = 0; i < publicKeys.getLength(); i++) {
			
			Node pubNode = publicKeys.item(i);
			
			if (pubNode.getTextContent().compareTo(encodedPublicKeyInHex) == 0) {
				
				Node priNode = pubNode.getNextSibling().getNextSibling();
				String hex = priNode.getTextContent();
				
				byte[] encoded = BaseConverter.stringHexToDec(hex);
				PKCS8EncodedKeySpec encodedPrivateKeySpec = new PKCS8EncodedKeySpec(encoded);
				
				KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
				RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey) factory.generatePrivate(encodedPrivateKeySpec);
				
				return privateKey;
			}
		}
		
		return null;
	}
	
}