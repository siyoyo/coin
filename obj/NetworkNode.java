package obj;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
//import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import obj.Transaction.TransactionInputsLessThanOutputsException;
import util.MerkleTree;
import util.RSA512;
import util.Signature;

/**
 * <b>References</b>
 * <ul>
 * <li><a href="https://docs.oracle.com/javase/tutorial/jaxp/xslt/writingDom.html">Oracle Java Tutorials:
 * Writing Out a DOM as an XML File</a></li>
 * </ul>
 */
public class NetworkNode {
	
	public final static String BLOCKCHAIN = "dat/blockchain.xml";
	public final static String UTXO = "dat/utxo.xml";
	public final static String KEY_ALGORITHM = "RSA";
	public final static String SIGNATURE_ALGORITHM = "SHA256withRSA";
	public final static String REWARD = "50";
	public static String difficulty = "00ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
	
//	private Logger logger = Logger.getLogger(NetworkNode.class.getName());
	private BlockExplorer explorer;
	private Wallet wallet;
	private ArrayList<Transaction> mempool;
	
	public NetworkNode() throws ParserConfigurationException, SAXException, IOException, URISyntaxException {
		explorer = new BlockExplorer(BLOCKCHAIN);
		wallet = new Wallet();
		mempool = new ArrayList<Transaction>();
//		updateMempool();
	}
	
	public void connect() { // TODO
		findPeers();
		updateBlockchain();
	}
	
	public void mine() throws URISyntaxException, TransactionInputsLessThanOutputsException, GeneralSecurityException, ParserConfigurationException, SAXException, IOException, DOMException, TransformerException {
		
		Block newBlock = makeBlock(explorer);
		explorer.extendBlockchain(newBlock, difficulty);
		
	}
	
	/*
	 * Private methods
	 */
	private void findPeers() {
		
	}
	
	private void updateBlockchain() {
		
	}
	
	public void updateMempool(Transaction newTransaction) {
		mempool.add(newTransaction);
	}
	
	private Block makeBlock(BlockExplorer explorer) throws URISyntaxException, TransactionInputsLessThanOutputsException, GeneralSecurityException, TransformerException, ParserConfigurationException, SAXException, IOException {
		
		// Create mint transaction
		KeyPair newKeyPair = RSA512.generateKeyPair();
		RSAPublicKey publicKey = (RSAPublicKey) newKeyPair.getPublic();
		
		Output gold = new Output(publicKey, REWARD);
		
		Transaction mint = new Transaction();
		mint.addOutput(gold);
		
		// Validate transactions
		ArrayList<Transaction> transactions = cloneMempool();
		transactions = finalise(transactions);
		
		// Only include transactions which are valid
		MerkleTree tree = new MerkleTree();
		String root = tree.getRoot(explorer, mint, transactions);
		
		BlockHeader newHeader = new BlockHeader(explorer.getLastBlockHeader(), root);
		String pow = newHeader.hash(difficulty);
		
		Block newBlock = new Block(newHeader, pow, tree.orderedTransactions());
		wallet.save(newKeyPair, REWARD);
		mempool.clear();
		
		return newBlock;
		
	}
			
	private ArrayList<Transaction> cloneMempool() {
		
		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
		for (int i = 0; i < mempool.size(); i++) transactions.add(mempool.get(i));
		
		return transactions;
	}
	
	private ArrayList<Transaction> finalise(ArrayList<Transaction> transactions) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, TransactionInputsLessThanOutputsException, InvalidKeySpecException, DOMException {
		
		// For each transaction
		for (int i = 0; i < transactions.size(); i++) {
			
			Transaction tx = transactions.get(i);
			byte[][] signatures = tx.finalise(explorer);
			byte[] outputsInBytes = tx.getOutputsInBytes();
			
			ArrayList<Input> inputs = tx.inputs();
			
			// For each input
			for (int j = 0; j < inputs.size(); j++) {
				
				TransactionReference reference = inputs.get(j).reference();
				RSAPublicKey publicKey = explorer.recipientPublicKey(reference);
				
				Signature signature = new Signature();
				boolean valid = signature.verify(outputsInBytes, signatures[j], publicKey); 
				
				if (!valid) transactions.remove(i);
			}		
		}
		
		return transactions;
	}

}