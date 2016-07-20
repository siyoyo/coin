package obj;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
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

import util.XMLio;

public class UTXOExplorer {
	
	private String filename;
	private Document doc;
	private NodeList utxos;
	
	public UTXOExplorer(String filename) throws ParserConfigurationException, SAXException, IOException, URISyntaxException {
		this.filename = filename;
		doc = XMLio.parse(filename);
		doc.getDocumentElement().normalize();
		utxos = doc.getElementsByTagName("utxo");
	}

	public boolean valid(Input input) throws NoSuchAlgorithmException, InvalidKeySpecException, DOMException {
		
		TransactionReference reference = input.reference();
		
		for (int i = 0; i <utxos.getLength(); i++) {
			
			Node utxo = utxos.item(i);
			
			Node node = utxo.getFirstChild().getNextSibling();		// pow node
			String pow = node.getTextContent();
			
			node = node.getNextSibling().getNextSibling();			// txID node
			String txID = node.getTextContent();
			
			node = node.getNextSibling().getNextSibling();			// outputID node
			String outputID = node.getTextContent();
			
			if (pow.compareTo(reference.pow()) == 0 &
					txID.compareTo(reference.transactionID()) == 0 &
					outputID.compareTo(reference.outputID()) == 0) return true;
		}
		
		return false;
	}
	
	public void update(Block block) throws ParserConfigurationException, SAXException, IOException, TransformerException, URISyntaxException {
		
		ArrayList<Transaction> transactions = block.transactions();
		
		// Remove used UTXOs
		for (int i = 0; i < transactions.size(); i++) {
			ArrayList<Input> inputs = transactions.get(i).inputs();
			
			for (int j = 0; j < inputs.size(); j++) {
				TransactionReference reference = inputs.get(j).reference();
				
				for (int k = 0; k < utxos.getLength(); k++) {
					
					Node utxo = utxos.item(k);
					
					Node node = utxo.getFirstChild().getNextSibling();		// pow node
					String pow = node.getTextContent();
					
					node = node.getNextSibling().getNextSibling();			// txID node
					String txID = node.getTextContent();
					
					node = node.getNextSibling().getNextSibling();			// outputID node
					String outputID = node.getTextContent();
					
					if (pow.compareTo(reference.pow()) == 0 &
							txID.compareTo(reference.transactionID()) == 0 &
							outputID.compareTo(reference.outputID()) == 0) 
						doc.getDocumentElement().removeChild(utxo);
				}
			}
		}
		
		// Add new UTXOs
		for (int i = 0; i < transactions.size(); i++) {
			
			ArrayList<Output> outputs = transactions.get(i).outputs();
			
			for (int j = 0; j < outputs.size(); j++) {
				
				Node utxo = doc.createElement("utxo");
				
				Element element = doc.createElement("pow");
				element.setTextContent(block.pow());
				utxo.appendChild(element);
				
				element = doc.createElement("txID");
				element.setTextContent(String.valueOf(i + 1));		// 1-base so need to shift up
				utxo.appendChild(element);
				
				element = doc.createElement("outputID");
				element.setTextContent(String.valueOf(j + 1));		// 1-base so need to shift up
				utxo.appendChild(element);
				
				doc.getDocumentElement().appendChild(utxo);
			}
		}
		
		XMLio.write(filename, doc, doc.getDocumentElement());
	}
	
}