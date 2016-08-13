package util;

import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import obj.Block;
import obj.BlockHeader;
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
	
	public BlockExplorer(String filename) {
		this.filename = filename;
		doc = XMLio.parse(filename);
		doc.getDocumentElement().normalize();
	}
	
	/**
	 * Returns the current height of the blockchain.
	 * @return current height
	 */
	public int getBlockchainHeight() {
		return getBlocks().getLength();
	}
	
	/**
	 * Returns the proof-of-work hash string of the last block.
	 * @return proof-of-work hash string
	 */
	public String getLastPoW() {
		String height = String.valueOf(getBlockchainHeight());
		Node block = getBlockNodeByHeight(height);
		return getPoW(block);
	}
	
	/**
	 * Returns the block node that corresponds to the specified height on the blockchain.
	 * @param height height
	 * @return block node
	 */
	public Node getBlockNodeByHeight(String height) {
		NodeList heights = doc.getElementsByTagName("height");
		Node blockNode = getBlockNodeFromList(heights, height);
		return blockNode;
	}
	
	/**
	 * Returns the block node that is uniquely identified by the proof-of-work hash provided.
	 * @param pow proof-of-work hash string
	 * @return block node
	 */
	public Node getBlockNodeByHash(String pow) {
		NodeList pows = doc.getElementsByTagName("pow");
		Node blockNode = getBlockNodeFromList(pows, pow);
		return blockNode;
	}
	
	/**
	 * Returns the output address that is uniquely identified by the transaction reference.
	 * @param reference transaction reference
	 * @return output address
	 */
	public String outputAddress(TransactionReference reference) {
		Node outputNode = getOutputNodeByReference(reference);
		Node outputAddressNode = getDescendantNode(outputNode, "outputAddress");
		return outputAddressNode.getTextContent();
	}
	
	/**
	 * Returns the public key that is uniquely identified by the transaction reference.
	 * @param reference transaction reference
	 * @return public key
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws DOMException
	 */
	public RSAPublicKey publicKey(TransactionReference reference) {
		String publicKeyString = publicKeyString(reference);
		return RSA512.decodePublicKey(publicKeyString);
	}
	
	/**
	 * Returns the transaction amount that is uniquely identified by the transaction reference.
	 * @param reference transaction reference
	 * @return transaction amount
	 */
	public String transactionAmount(TransactionReference reference) {
		Node outputNode = getOutputNodeByReference(reference);
		Node amountNode = getDescendantNode(outputNode, "amount");
		return amountNode.getTextContent();
	}
	
	/**
	 * Returns the block at the specified height of the blockchain.
	 * @param requestedHeight height
	 * @return block
	 */
	public Block getBlock(String requestedHeight) {
		
		Element blockElement = (Element) getBlockNodeByHeight(requestedHeight);
		
		// Block header
		Node previousPoWNode = getDescendantNode(blockElement, "previousPoW");
		String previousPoW = previousPoWNode.getTextContent();
		
		Node merkleRootNode = getDescendantNode(blockElement, "merkleRoot");
		String merkleRoot = merkleRootNode.getTextContent();
		
		Node nonceNode = getDescendantNode(blockElement, "nonce");
		int nonce = Integer.valueOf(nonceNode.getTextContent());
		
		Node difficultyNode = getDescendantNode(blockElement, "difficulty");
		String difficulty = difficultyNode.getTextContent();
		
		BlockHeader blockHeader = new BlockHeader(previousPoW, merkleRoot);
		blockHeader.setNonce(nonce);
		blockHeader.setDifficulty(difficulty);
		
		// Block body
		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
		Transaction transaction;
		Element transactionElement;
		
		NodeList txNodes = blockElement.getElementsByTagName("transaction");
		int numberOfTransactions = txNodes.getLength();
		Node txIDNode;
		
		NodeList inputNodes, outputNodes;
		int numberOfInputs, numberOfOutputs;
		
		Node inputNode, refPoWNode, refTxNode, refOutNode, inputIDNode;
		Node outputNode, publicKeyNode, amountNode, outputIDNode;
		
		Input input;
		TransactionReference reference;
		Output output;
		RSAPublicKey publicKey;
		
		// Transactions
		for (int i = 0; i < numberOfTransactions; i++) {
			
			transactionElement = (Element) txNodes.item(i);
			transaction = new Transaction();
			
			// Transaction ID
			txIDNode = getDescendantNode(transactionElement, "txID");
			transaction.setTxID(Integer.valueOf(txIDNode.getTextContent()));
			
			// Inputs
			inputNodes = transactionElement.getElementsByTagName("input");
			numberOfInputs = inputNodes.getLength();
			
			for (int j = 0; j < numberOfInputs; j++) {
				
				inputNode = inputNodes.item(j);
				
				// Transaction reference
				refPoWNode = getDescendantNode(inputNode, "refPoW");
				refTxNode = getDescendantNode(inputNode, "refTx");
				refOutNode = getDescendantNode(inputNode, "refOut");
				
				reference = new TransactionReference(refPoWNode.getTextContent(), refTxNode.getTextContent(), refOutNode.getTextContent());
				
				// Input
				input = new Input(reference);
				
				// Input ID
				inputIDNode = getDescendantNode(inputNode, "inputID");
				input.setInputID(Integer.valueOf(inputIDNode.getTextContent()));
				
				transaction.inputs().add(input);
			}
			
			// Outputs
			outputNodes = transactionElement.getElementsByTagName("output");
			numberOfOutputs = outputNodes.getLength();
			
			for (int k = 0; k < numberOfOutputs; k++) {
				
				outputNode = outputNodes.item(k);
				
				// Public key
				publicKeyNode = getDescendantNode(outputNode, "publicKey");
				publicKey = RSA512.decodePublicKey(publicKeyNode.getTextContent());
				
				// Amount
				amountNode = getDescendantNode(outputNode, "amount");
				
				// Output
				output = new Output(publicKey, amountNode.getTextContent());
				
				// Output ID
				outputIDNode = getDescendantNode(outputNode, "outputID");
				output.setOutputID(Integer.valueOf(outputIDNode.getTextContent()));
				
				transaction.outputs().add(output);
				}
			transactions.add(transaction);
			}
		
		// Proof-of-work
		Node powNode = getDescendantNode(blockElement, "pow");
		String pow = powNode.getTextContent();
		
		// Block
		Block block = new Block(blockHeader, pow, transactions);
		
		// Block height
		Node heightNode = getDescendantNode(blockElement, "height");
		block.setHeight(Integer.valueOf(heightNode.getTextContent()));
		
		return block;
	}
	
	/**
	 * Writes the new block to the blockchain
	 * @param block
	 */
	public void extendBlockchain(Block block) {
		
		doc.getDocumentElement().normalize();
		
		Node blockNode = doc.createElement("block");
		
		// Block height
		Node heightNode = doc.createElement("height");
		heightNode.setTextContent(block.height());
		blockNode.appendChild(heightNode);
		
		// Block header
		Node headerNode = doc.createElement("header");
		
		Node headerChildNode = doc.createElement("previousPoW");
		headerChildNode.setTextContent(block.header().previousPoW());
		headerNode.appendChild(headerChildNode);
		
		headerChildNode = doc.createElement("pow");
		headerChildNode.setTextContent(block.pow());
		headerNode.appendChild(headerChildNode);
		
		headerChildNode = doc.createElement("merkleRoot");
		headerChildNode.setTextContent(block.header().merkleRoot());
		headerNode.appendChild(headerChildNode);
		
		headerChildNode = doc.createElement("nonce");
		headerChildNode.setTextContent(Integer.toString(block.header().nonce()));
		headerNode.appendChild(headerChildNode);
		
		headerChildNode = doc.createElement("difficulty");
		headerChildNode.setTextContent(block.header().difficulty());
		headerNode.appendChild(headerChildNode);
		
		blockNode.appendChild(headerNode);
		
		// Block body
		Node bodyNode = doc.createElement("body");
		Node txNode, txIDNode, inputNode, outputNode, node;
		Input input;
		TransactionReference reference;
		Output output;
		
		// Transactions
		ArrayList<Transaction> transactions = block.transactions();
		for (int i = 0; i < transactions.size(); i++) {
			
			Transaction tx = transactions.get(i);
			txNode = doc.createElement("transaction");
			
			// Transaction ID
			txIDNode = doc.createElement("txID");
			txIDNode.setTextContent(tx.txID());
			txNode.appendChild(txIDNode);
			
			// Inputs
			ArrayList<Input> inputs = tx.inputs();
			for (int j = 0; j < inputs.size(); j++) {
				
				input = inputs.get(j); 
				reference = input.reference();
				inputNode = doc.createElement("input");
				
				// Input ID
				node = doc.createElement("inputID");
				node.setTextContent(input.inputID());
				inputNode.appendChild(node);
				
				// Input Address
				node = doc.createElement("inputAddress");
				node.setTextContent(outputAddress(reference));
				inputNode.appendChild(node);
				
				// Input Reference PoW
				node = doc.createElement("refPoW");
				node.setTextContent(reference.pow());
				inputNode.appendChild(node);
				
				// Input Reference TransactionID
				node = doc.createElement("refTx");
				node.setTextContent(reference.transactionID());
				inputNode.appendChild(node);
				
				// Input Reference OutputID
				node = doc.createElement("refOut");
				node.setTextContent(reference.outputID());
				inputNode.appendChild(node);
				
				txNode.appendChild(inputNode);
			}
			
			// Outputs
			ArrayList<Output> outputs = tx.outputs();
			for (int k = 0; k < outputs.size(); k++) {
				
				output = outputs.get(k);
				outputNode = doc.createElement("output");
				
				// Output ID
				node = doc.createElement("outputID");
				node.setTextContent(output.outputID());
				outputNode.appendChild(node);
				
				// Output Address
				node = doc.createElement("outputAddress");
				node.setTextContent(output.outputAddress());
				outputNode.appendChild(node);
				
				// Public Key
				node = doc.createElement("publicKey");
				node.setTextContent(output.recipientPublicKeyString());
				outputNode.appendChild(node);
				
				// Amount
				node = doc.createElement("amount");
				node.setTextContent(output.amount());
				outputNode.appendChild(node);
				
				txNode.appendChild(outputNode);
			}
			
			bodyNode.appendChild(txNode);
		}
		
		blockNode.appendChild(bodyNode);
		doc.getDocumentElement().appendChild(blockNode);
		
		XMLio.write(filename, doc, doc.getDocumentElement());
		System.out.println("Added new block " + block.pow());
	}
	
	/**
	 * Computes the longest consensus between two chains.
	 * @param chain1 blockchain file 1
	 * @param chain2 blockchain file 2
	 * @return longest consensus between the two chains
	 */
	public static int longestMatch(String chain1, String chain2) {
		
		Document doc1 = XMLio.parse(chain1);
		Document doc2 = XMLio.parse(chain2);
		
		NodeList heightNodes1 = doc1.getElementsByTagName("height");
		NodeList heightNodes2 = doc2.getElementsByTagName("height");
		
		int length1 = heightNodes1.getLength();
		int length2 = heightNodes2.getLength();
		
		System.out.println("Height of " + chain1 + ": " + length1);
		System.out.println("Height of " + chain2 + ": " + length2);
		
		int longestPotential = Math.min(length1, length2);
		int longestMatch = 0;
		
		Node heightNode1, heightNode2, powNode1, powNode2;
		Element blockElement1, blockElement2;
		String height1, height2, pow1, pow2;
		
		for (int index1 = 0; index1 < longestPotential; index1++) {
			for (int index2 = 0; index2 < longestPotential; index2++) {
				
				heightNode1 = heightNodes1.item(index1);
				height1 = heightNode1.getTextContent();
				
				heightNode2 = heightNodes2.item(index2);
				height2 = heightNode2.getTextContent();
				
				if (height1.compareTo(height2) == 0) {
					
					blockElement1 = (Element) heightNode1.getParentNode();
					blockElement2 = (Element) heightNode2.getParentNode();
					
					powNode1 = blockElement1.getElementsByTagName("pow").item(0);
					powNode2 = blockElement2.getElementsByTagName("pow").item(0);
					
					pow1 = powNode1.getTextContent();
					pow2 = powNode2.getTextContent();
					
					if (pow1.compareTo(pow2) == 0) longestMatch++;
					else break;
				}
			}
		}
		return longestMatch;
	}
	
	/* -----------------------------------------------------------------
	 * 							Private methods
	 * -----------------------------------------------------------------
	 */
	
	/*
	 * Returns a list containing all the block nodes.
	 */
	private NodeList getBlocks() {
		doc.getDocumentElement().normalize();
		return doc.getElementsByTagName("block");
	}
	
	/*
	 * Returns the proof-of-work hash string of the given block node.  Assumes
	 * there is only one proof-of-work node.
	 */
	private String getPoW(Node blockNode) {
		Element blockElement = (Element) blockNode;
		NodeList pows = blockElement.getElementsByTagName("pow");
		return pows.item(0).getTextContent();
	}
	
	/*
	 * Returns as a string the public key that is uniquely identified by the
	 * transaction reference.
	 */
	private String publicKeyString(TransactionReference reference) {
		Node outputNode = getOutputNodeByReference(reference);
		Node publicKeyNode = getDescendantNode(outputNode, "publicKey");
		return publicKeyNode.getTextContent();
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
	
	/*
	 * Returns the output node that is uniquely identified by the transaction reference. 
	 */
	private Node getOutputNodeByReference(TransactionReference reference) {
		
		NodeList pows = doc.getElementsByTagName("pow");
		Node powNode = getNodeFromList(pows, reference.pow());
		
		Element blockElement = (Element) getBlockNode(powNode);
		
		NodeList txIDs = blockElement.getElementsByTagName("txID");
		Node txIDNode = getNodeFromList(txIDs, reference.transactionID());
		
		Element txElement = (Element) txIDNode.getParentNode();
		
		NodeList outputIDs = txElement.getElementsByTagName("outputID");
		Node outputIDNode = getNodeFromList(outputIDs, reference.outputID());
		
		Node outputNode = outputIDNode.getParentNode();
		
		return outputNode;
	}
	
	/*
	 * Returns the node from the given list that matches the search term.
	 */
	private Node getNodeFromList(NodeList nodes, String searchTerm) {
		
		Node node;
		
		for (int i = 0; i < nodes.getLength(); i++) {
			node = nodes.item(i);
			if (node.getTextContent().compareTo(searchTerm) == 0) return node;
		}
		
		return null;
	}
	
	/*
	 * Returns the block parent node of the given child node. 
	 */
	private Node getBlockNode(Node childNode) {
		Node blockNode = childNode;
		while (blockNode.getNodeName().compareTo("block") != 0) blockNode = blockNode.getParentNode(); 
		return blockNode;
	}
	
	/*
	 * Returns the block parent node of the node from the given list that matches the search term.
	 */
	private Node getBlockNodeFromList(NodeList nodes, String searchTerm) {
		Node node = getNodeFromList(nodes, searchTerm);
		Node blockNode = getBlockNode(node);
		return blockNode;
	}
	
}