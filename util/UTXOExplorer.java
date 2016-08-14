package util;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import obj.Block;
import obj.Input;
import obj.Output;
import obj.Transaction;
import obj.TransactionReference;

public class UTXOExplorer {
	
	private String filename;
	private Document doc;
	
	public UTXOExplorer(String filename) {
		this.filename = filename;
		doc = XMLio.parse(filename);
		doc.getDocumentElement().normalize();
	}

	/**
	 * Checks the input against the UTXO list.
	 * @param input input
	 * @return true if the input is found in the UTXO list; false otherwise
	 * @throws UTXOException 
	 */
	public boolean valid(Input input) throws UTXOException {
		Node utxoNode = getUTXONode(input.reference());
		if (utxoNode != null) return true;
		else throw new UTXOException("Input not found in UTXO list");
	}
	
	/**
	 * Updates the file containing the list of unspent transaction outputs by removing
	 * spent inputs and creating new unspent outputs.
	 * <b>This method should only be called on a finalized block</b> as it assumes all
	 * arrays are 1-based.
	 * @param finalisedBlock finalized block
	 */
	public void update(Block finalisedBlock) {
		
		ArrayList<Transaction> transactions = finalisedBlock.transactions();
		int numberOfTransactions = transactions.size();
		int numberOfInputs, numberOfOutputs;
		
		Transaction transaction;
		ArrayList<Input> inputs;
		ArrayList<Output> outputs;
		
		TransactionReference reference;
		Input input;
		Output output;
		
		Node utxoNode, powNode, txIDNode, outputIDNode;
		
		for (int i = 0; i < numberOfTransactions; i++) {
			
			transaction = transactions.get(i);
			
			// Remove used UTXOs i.e. spent inputs
			inputs = transaction.inputs();
			numberOfInputs = inputs.size();
			
			for (int j = 0; j < numberOfInputs; j++) {
				input = inputs.get(j);
				reference = input.reference();
				utxoNode = getUTXONode(reference);
				doc.removeChild(utxoNode);
			}
		
			// Create new UTXOs from outputs
			outputs = transaction.outputs();
			numberOfOutputs = outputs.size();
			
			for (int k = 0; k < numberOfOutputs; k++) {
				output = outputs.get(k);
				utxoNode = doc.createElement("utxo");
				
				powNode = doc.createElement("pow");
				powNode.setTextContent(finalisedBlock.pow());
				utxoNode.appendChild(powNode);
				
				txIDNode = doc.createElement("txID");
				txIDNode.setTextContent(transaction.txID());
				utxoNode.appendChild(txIDNode);
				
				outputIDNode = doc.createElement("outputID");
				outputIDNode.setTextContent(output.outputID());
				utxoNode.appendChild(outputIDNode);
				
				doc.getDocumentElement().appendChild(utxoNode);
			}
		}
	}
	
	public void rebuildUTXOList(BlockExplorer blockExplorer) {
		
		doc.getDocumentElement().normalize();
		
		// Clear all UTXO entries
		NodeList utxoNodes = doc.getElementsByTagName("utxo");
		for (int i = 0; i < utxoNodes.getLength(); i++) doc.getDocumentElement().removeChild(utxoNodes.item(i));
		
		// Rebuild UTXO entries
		Block block;
		int blockchainHeight = blockExplorer.getBlockchainHeight();
		for (int height = 1; height <= blockchainHeight; height++) {
			block = blockExplorer.getBlock(String.valueOf(height));
			update(block);
		}
		
		// Write to file
		updateUTXOFile();
	}
	
	/**
	 * Writes the current state of the document object to file.
	 */
	public void updateUTXOFile() {
		XMLio.write(filename, doc, doc.getDocumentElement());
	}
	
	@SuppressWarnings("serial")
	public class UTXOException extends Exception {
		
		public UTXOException() {
			super();
		}
		
		public UTXOException(String msg) {
			super(msg);
		}
	}
	
	/* -----------------------------------------------------------------
	 * 							Private methods
	 * -----------------------------------------------------------------
	 */
	
	/*
	 * Returns the UXTO node that matches the given transaction reference.
	 */
	private Node getUTXONode(TransactionReference reference) {
		
		NodeList utxoNodes = doc.getElementsByTagName("utxo");
		Node utxoNode, powNode, txIDNode, outputIDNode;
		
		String pow, txID, outputID;
		
		for (int i = 0; i < utxoNodes.getLength(); i++) {
			
			utxoNode = utxoNodes.item(i);

			// Check proof-of-work
			powNode = getDescendantNode(utxoNode, "pow");
			pow = powNode.getTextContent();
			if (pow.compareTo(reference.pow()) == 0) {

				// Check transaction ID
				txIDNode = getDescendantNode(utxoNode, "txID");
				txID = txIDNode.getTextContent();
				if (txID.compareTo(reference.transactionID()) == 0) {
					
					// Check output ID
					outputIDNode = getDescendantNode(utxoNode, "outputID");
					outputID = outputIDNode.getTextContent();
					if (outputID.compareTo(reference.outputID()) == 0) return utxoNode;
				}
			}
		}
		return null;
	}
	
	/*
	 * Returns the specified descendant node of the parent node.  Assumes there is only one
	 * descendant node of its kind.
	 */
	private Node getDescendantNode(Node parentNode, String tag) {
		Element parentElement = (Element) parentNode;
		NodeList nodes = parentElement.getElementsByTagName(tag);
		return nodes.item(0);
	}
	
}