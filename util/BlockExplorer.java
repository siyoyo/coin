package util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import obj.Block;
import obj.Input;
import obj.Output;
import obj.Transaction;
import obj.TransactionReference;

/**
 * The BlockExplorer class has superpowers like the ability to time travel
 * (but only into the past) and x-ray vision to see inside Merkle trees to
 * decipher ancient transactions.
 * <br><br>
 * <b>References</b>
 * <ul>
 * <li><a href="http://www.w3schools.com/xml/dom_nodes.asp">XML DOM Nodes</a></li>
 * </ul>
 */
public class BlockExplorer {
	
	private String filename;
	private Document doc;
	
	public BlockExplorer(String filename) throws ParserConfigurationException, SAXException, IOException, URISyntaxException {
		this.filename = filename;
		doc = XMLio.parse(filename);
		doc.getDocumentElement().normalize();
	}
	
	public String getLastBlockHeader() {
		
		NodeList blocks = doc.getElementsByTagName("block");
		String height = String.valueOf(blocks.getLength());
		Node block = getBlockByHeight(height);
		System.out.println(block.getFirstChild().getNodeName());
		return getHeader(block);
	}
	
	public Node getBlockByHeight(String requestedHeight) {
		
		NodeList blocks = doc.getElementsByTagName("block");
		for (int i = 0; i < blocks.getLength(); i++) {
			Node block = blocks.item(i);
			Element element = (Element) block;
			String attribute = element.getAttributes().getNamedItem("height").getTextContent();	// Get height value
			
			if (attribute.compareTo(requestedHeight) == 0) return block;
		}
		
		return null;
	}
	
	public Node getBlockByHash(String pow) {
		NodeList blocks = doc.getElementsByTagName("block");
		for (int i = 0; i < blocks.getLength(); i++) {
			Node block = blocks.item(i);
			Node node = block.getFirstChild().getNextSibling();	// header node
			node = node.getFirstChild().getNextSibling();		// previousPoW node
			node = node.getNextSibling().getNextSibling();		// pow node
			
			if (node.getTextContent().compareTo(pow) == 0) return block;
		}
		
		return null;
	}
	
	public String recipientAddress(TransactionReference reference) {
		
		Element node = (Element) getBlockByHash(reference.pow());
		NodeList transactions = node.getElementsByTagName("transaction");
		
		for (int i = 0; i < transactions.getLength(); i++) {
			
			Element transaction = (Element) transactions.item(i);
			NodeList outputs = transaction.getElementsByTagName("output");
			
			for (int j = 0; j < outputs.getLength(); j++) {
				
				Element output = (Element) outputs.item(j);
				
				if (transaction.getAttribute("txID").compareTo(reference.transactionID()) == 0 &
						output.getAttribute("outputID").compareTo(reference.outputID()) == 0) {
					
					Node n = output.getFirstChild().getNextSibling();	// address node
					return n.getTextContent();
				}			
			}
		}
		
		return null;
	}
	
	public RSAPublicKey recipientPublicKey(TransactionReference reference) throws NoSuchAlgorithmException, InvalidKeySpecException, DOMException {
		
		String address = recipientAddress(reference);
		return RSA512.decodePublicKey(address);
	}
	
	public String transactionAmount(TransactionReference reference) {
		
		String amount = null;
		Element node = (Element) getBlockByHash(reference.pow());
		NodeList transactions = node.getElementsByTagName("transaction");
		
		for (int i = 0; i < transactions.getLength(); i++) {
			
			Element transaction = (Element) transactions.item(i);
			NodeList outputs = transaction.getElementsByTagName("output");
			
			for (int j = 0; j < outputs.getLength(); j++) {
				
				Element output = (Element) outputs.item(j);
				
				if (transaction.getAttribute("txID").compareTo(reference.transactionID()) == 0 &
						output.getAttribute("outputID").compareTo(reference.outputID()) == 0) {
					Node n = output.getFirstChild().getNextSibling();	// address node
					n = n.getNextSibling().getNextSibling();			// amount node
					amount = n.getTextContent();
				}
			}
		}
		
		return amount;
	}
	
	public NodeList previousPoWs() {
		return doc.getElementsByTagName("previousPoW");
	}
	
	public void extendBlockchain(Block newBlock, String difficulty) throws DOMException, NoSuchAlgorithmException, InvalidKeySpecException, ParserConfigurationException, SAXException, IOException, TransformerException, URISyntaxException  {
		
		doc.getDocumentElement().normalize();
		
		NodeList blocks = doc.getElementsByTagName("block");
		int height = blocks.getLength();
		
		Element block = doc.createElement("block");
		block.setAttribute("height", String.valueOf(++height));
		
		block.appendChild(doc.createElement("header"));
		block.appendChild(doc.createElement("body"));
		
		// Block header
		Node child = block.getFirstChild();
		
		Element grandchild = doc.createElement("previousPoW");
		grandchild.setTextContent(newBlock.header().previousPoW());
		child.appendChild(grandchild);
		
		grandchild = doc.createElement("pow");
		grandchild.setTextContent(newBlock.pow());
		child.appendChild(grandchild);
		
		grandchild = doc.createElement("merkleRoot");
		grandchild.setTextContent(newBlock.header().merkleRoot());
		child.appendChild(grandchild);
		
		grandchild = doc.createElement("nonce");
		grandchild.setTextContent(Integer.toString(newBlock.header().nonce()));
		child.appendChild(grandchild);
		
		grandchild = doc.createElement("difficulty");
		grandchild.setTextContent(difficulty);
		child.appendChild(grandchild);
		
		// Block body
		child = block.getLastChild();
		
		ArrayList<Transaction> transactions = newBlock.transactions();
		
		for (int i = 0; i < transactions.size(); i++) {
			
			Transaction tx = transactions.get(i);
			ArrayList<Input> inputs = tx.inputs();
			
			grandchild = doc.createElement("transaction");
			grandchild.setAttribute("txID", String.valueOf(i + 1));
			child.appendChild(grandchild);
			
			for (int j = 0; j < inputs.size(); j++) {
				
				TransactionReference reference = inputs.get(j).reference();
				byte[] encodedPublicKey = this.recipientPublicKey(reference).getEncoded();
				String encodedPublicKeyInHex = BaseConverter.bytesDecToHex(encodedPublicKey);
				
				Element greatgrandchild = doc.createElement("input");
				greatgrandchild.setAttribute("inputID", String.valueOf(j + 1));
				greatgrandchild.setTextContent(encodedPublicKeyInHex);
				grandchild.appendChild(greatgrandchild);
				
			}
			
			ArrayList<Output> outputs = tx.outputs();
			
			for (int k = 0; k < outputs.size(); k++) {
				
				Output output = outputs.get(k);
				
				Element el = doc.createElement("output");
				el.setAttribute("outputID", String.valueOf(k + 1));
				
				Node n = doc.createElement("address");
				byte[] encoded = output.recipientPublicKey().getEncoded();
				n.setTextContent(BaseConverter.bytesDecToHex(encoded));
				el.appendChild(n);
				
				n = doc.createElement("amount");
				n.setTextContent(output.amount());
				el.appendChild(n);
				
				grandchild.appendChild(el);
				
			}
			
		}
		
		doc.getDocumentElement().appendChild(block);
		XMLio.write(filename, doc, doc.getDocumentElement());
	}
	
	/*
	 * Private methods
	 */
	
	private String getHeader(Node block) {
		
		Node node = block.getFirstChild().getNextSibling();		// header node
		System.out.println("node1: " + node.getNodeName());
		node = node.getFirstChild().getNextSibling();			// previousPoW node
		System.out.println("node2: " + node.getNodeName());
		node = node.getNextSibling().getNextSibling();			// pow node
		System.out.println("node3: " + node.getNodeName());
		return node.getTextContent();
	}
	
}