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
		
		doc.getDocumentElement().normalize();
		
		NodeList blocks = doc.getElementsByTagName("block");
		String height = String.valueOf(blocks.getLength());
		Node block = getBlockByHeight(height);
		
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
		
		NodeList pows = doc.getElementsByTagName("pow");
		
		for (int i = 0; i < pows.getLength(); i++) {
			Node node = pows.item(i);
			if (node.getTextContent().compareTo(pow) == 0) return node.getParentNode().getParentNode();
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
	
	public String recipientPublicKeyString (TransactionReference reference) {
		
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
					n = n.getNextSibling().getNextSibling();			// publickey node
					return n.getTextContent();
				}			
			}
		}
		
		return null;
	}
	
	public RSAPublicKey recipientPublicKey(TransactionReference reference) throws NoSuchAlgorithmException, InvalidKeySpecException, DOMException {
		String address = recipientPublicKeyString(reference);
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
					n = n.getNextSibling().getNextSibling();			// publickey node
					n = n.getNextSibling().getNextSibling();			// amount node
					amount = n.getTextContent();
				}
			}
		}
		
		return amount;
	}
	
	public void extendBlockchain(Block newBlock, String difficulty) throws DOMException, NoSuchAlgorithmException, InvalidKeySpecException, ParserConfigurationException, SAXException, IOException, TransformerException, URISyntaxException  {
		
		doc.getDocumentElement().normalize();
		
		NodeList blocks = doc.getElementsByTagName("block");
		int height = blocks.getLength();
		
		Element block = doc.createElement("block");
		block.setAttribute("height", String.valueOf(++height));
		
		block.appendChild(doc.createElement("header"));
		
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
		block.appendChild(doc.createElement("body"));
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
				String address = this.recipientAddress(reference);
				
				Element greatgrandchild = doc.createElement("input");
				greatgrandchild.setAttribute("inputID", String.valueOf(j + 1));
				greatgrandchild.setTextContent(address);
				grandchild.appendChild(greatgrandchild);
				
			}
			
			ArrayList<Output> outputs = tx.outputs();
			
			for (int k = 0; k < outputs.size(); k++) {
				
				Output output = outputs.get(k);
				
				Element el = doc.createElement("output");
				el.setAttribute("outputID", String.valueOf(k + 1));
				
				Node n = doc.createElement("address");
				n.setTextContent(output.recipientAddress());
				el.appendChild(n);
				
				n = doc.createElement("publicKey");
				n.setTextContent(output.recipientPublicKeyString());
				el.appendChild(n);
				
				n = doc.createElement("amount");
				n.setTextContent(output.amount());
				el.appendChild(n);
				
				grandchild.appendChild(el);
				
			}
			
		}
		
		doc.getDocumentElement().appendChild(block);
		XMLio.write(filename, doc, doc.getDocumentElement());
		System.out.println("Added new block " + newBlock.pow());
	}
	
	public static int longestMatch(String chain1, String chain2) throws ParserConfigurationException, SAXException, IOException, URISyntaxException {
		
		Document doc1 = XMLio.parse(chain1);
		Document doc2 = XMLio.parse(chain2);
		
		NodeList blocks1 = doc1.getElementsByTagName("block");
		NodeList blocks2 = doc2.getElementsByTagName("block");
		
		System.out.println("Height of " + chain1 + ": " + blocks1.getLength());
		System.out.println("Height of " + chain2 + ": " + blocks2.getLength());
		
		int longestPotential = Math.min(blocks1.getLength(), blocks2.getLength());
		int longestMatch = 0;
		
		Node block1, block2, pow1, pow2;
		NodeList nodes1, nodes2;
		String hash1 = "hash1", hash2 = "hash2";
		
		for (int i = 0; i < longestPotential; i++) {
			
			block1 = blocks1.item(i);
			block2 = blocks2.item(i);
			
			nodes1 = block1.getFirstChild().getNextSibling().getChildNodes();
			nodes2 = block2.getFirstChild().getNextSibling().getChildNodes();
			
			for (int j = 0; j < nodes1.getLength(); j++) {
				pow1 = nodes1.item(j);
				if (pow1.getNodeName().compareTo("pow") == 0) hash1 = pow1.getTextContent();
			}
			
			for (int k = 0; k < nodes2.getLength(); k++) {
				pow2 = nodes2.item(k);
				if (pow2.getNodeName().compareTo("pow") == 0) hash2 = pow2.getTextContent();
			}
			
			if (hash1.compareTo(hash2) == 0) longestMatch++;
			else break;
		}
		
		return longestMatch;
	}
	
	/*
	 * Private methods
	 */
	
	private String getHeader(Node block) {
		
		NodeList children = block.getChildNodes();
		Node node;
		
		for (int i = 0; i < children.getLength(); i++) {
			
			node = children.item(i);
			if (node.getNodeName().compareTo("header") == 0) {
				
				NodeList grandchildren = node.getChildNodes();
				for (int j = 0; j < grandchildren.getLength(); j++) {
					
					node = grandchildren.item(j);
					if (node.getNodeName().compareTo("pow") == 0) return node.getTextContent();
				}
			}
		}
		return null;
	}
	
}