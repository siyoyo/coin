package obj;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

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
	
	private URI domain;
	private Document doc;
	private NodeList blocks;
	private int height;
	
	public BlockExplorer(URI domain) {
		this.domain = domain;
		doc = parse(this.domain);
		doc.getDocumentElement().normalize();
		blocks = doc.getElementsByTagName("block");
		height = blocks.getLength();
	}
	
	public int height() {
		return height;
	}
	
	public String getLastBlockHeader() {
		String sHeight = String.valueOf(height);
		Node block = getBlockByHeight(sHeight);
		return getHeader(block);
	}
	
	public Node getBlockByHeight(String requestedHeight) {
		
		for (int i = 0; i < blocks.getLength(); i++) {
			
			Node block = blocks.item(i);
			Element element = (Element) block;
			String attribute = element.getAttributes().getNamedItem("height").getTextContent();	// Get height value
			
			if (attribute.compareTo(requestedHeight) == 0) return block;
		}
		
		return null;
	}
	
	public Node getBlockByHash(String pow) {

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
		
		String address = null;
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
					address = n.getTextContent();
				}			
			}
		}
		
		return address;
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
	
	public void extendBlockchain(Block newBlock, String difficulty) throws DOMException, NoSuchAlgorithmException, InvalidKeySpecException {
		
		doc = parse(domain);
		doc.getDocumentElement().normalize();
		
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
				
				Element greatgrandchild = doc.createElement("input");
				greatgrandchild.setAttribute("inputID", String.valueOf(j + 1));
				greatgrandchild.setTextContent(this.recipientAddress(reference));
				grandchild.appendChild(greatgrandchild);
				
			}
			
			ArrayList<Output> outputs = tx.outputs();
			
			for (int k = 0; k < outputs.size(); k++) {
				
				Output output = outputs.get(k);
				
				Element node = doc.createElement("output");
				node.setAttribute("outputID", String.valueOf(k + 1));
				
				Node element = doc.createElement("address");
				element.setTextContent(output.recipientAddress());
				node.appendChild(element);
				
				element = doc.createElement("amount");
				element.setTextContent(Integer.toString(output.amount()));
				node.appendChild(element);
				
				grandchild.appendChild(node);
				
			}
			
		}
		
		doc.getDocumentElement().appendChild(block);
		write(doc.getDocumentElement());
	}
	
	/*
	 * Private methods
	 */
	
	private String getHeader(Node block) {
		
		Node node = block.getFirstChild().getNextSibling();		// header node
		node = node.getFirstChild().getNextSibling();			// previousPoW node
		node = node.getNextSibling().getNextSibling();			// pow node
		
		return node.getTextContent();
	}
	
	private void write(Node node) {

	    DOMSource source = new DOMSource(node);
	    StreamResult result = new StreamResult(new File(domain));
	    
		try {
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty("indent", "yes");
			
			Document document = parse(domain);
			if (document.getDoctype() != null) {
			    String systemValue = (new File (document.getDoctype().getSystemId())).getName();
			    transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemValue);    
			}
			
			transformer.transform(source, result);
			
		} catch (TransformerConfigurationException tce) {
			tce.printStackTrace();
		} catch (TransformerException te) {
			te.printStackTrace();
		}
		
	}
	
	private Document parse(URI domain) {
		
		Document doc = null;
		
		try {
			
			File file = new File(domain);
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			
			doc = docBuilder.parse(file);
			
		} catch (SAXParseException spe) {
            
			// Error generated by the parser
            System.out.println("\n** Parsing error" + ", line " + spe.getLineNumber() + ", uri " + spe.getSystemId());
            System.out.println("  " + spe.getMessage() );
  
            // Use the contained exception, if any
            Exception x = spe;
            if (spe.getException() != null) x = spe.getException();
            x.printStackTrace();
            
        } catch (SAXException sxe) {
           
        	// Error generated by this application (or a parser-initialization error)
            Exception x = sxe;
            if (sxe.getException() != null) x = sxe.getException();
            x.printStackTrace();
            
        } catch (ParserConfigurationException pce) {
            
        	// Parser with specified options cannot be built 
            pce.printStackTrace();
            
        } catch (IOException ioe) {
            
        	// I/O error
            ioe.printStackTrace();
            
        }
		
		return doc;
	}

}