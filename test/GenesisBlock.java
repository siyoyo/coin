package test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import obj.Transaction.TransactionInputsLessThanOutputsException;
import p2p.NetworkNode;

public class GenesisBlock {
	
	public static void main (String[] args) {
		
		// Replace last header!
		
		try {
			NetworkNode node = new NetworkNode();
			node.initialiseExplorers("dat/blockchain.xml", "dat/utxo.xml");
			node.mine();
		} catch (ParserConfigurationException | SAXException | IOException | URISyntaxException e) {
			// NetworkNode exceptions
			e.printStackTrace();
		} catch (DOMException | TransactionInputsLessThanOutputsException | GeneralSecurityException | TransformerException e) {
			// mine() exceptions
			e.printStackTrace();
		}
	}
}
