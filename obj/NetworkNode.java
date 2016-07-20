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
	public final static String REWARD = "50";
	public static String difficulty = "00ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
	
//	private Logger logger = Logger.getLogger(NetworkNode.class.getName());
	private BlockExplorer blockExplorer;
	private UTXOExplorer utxoExplorer;
	private Wallet wallet;
	private ArrayList<Transaction> mempool;
	
	public NetworkNode() throws ParserConfigurationException, SAXException, IOException, URISyntaxException {
		blockExplorer = new BlockExplorer(BLOCKCHAIN);
		utxoExplorer = new UTXOExplorer(UTXO);
		wallet = new Wallet();
		mempool = new ArrayList<Transaction>();
//		updateMempool();
	}
	
	public void connect() { // TODO
		findPeers();
		updateBlockchain();
	}
	
	public void mine() throws URISyntaxException, TransactionInputsLessThanOutputsException, GeneralSecurityException, ParserConfigurationException, SAXException, IOException, DOMException, TransformerException {
		Block newBlock = makeBlock(blockExplorer);
		blockExplorer.extendBlockchain(newBlock, difficulty);
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

		// Validate transactions
		ArrayList<Transaction> transactions = cloneMempool();
		transactions = finalise(transactions);
		
		// Create mint transaction
		KeyPair newKeyPair = RSA512.generateKeyPair();
		RSAPublicKey publicKey = (RSAPublicKey) newKeyPair.getPublic();
		
		int totalReward = Integer.parseInt(totalFees(transactions)) + Integer.parseInt(REWARD);
		Output gold = new Output(publicKey, String.valueOf(totalReward));
		
		Transaction mint = new Transaction();
		mint.addOutput(gold);
		
		// Only include transactions which are valid
		MerkleTree tree = new MerkleTree();
		String root = tree.getRoot(explorer, mint, transactions);
		
		// Genesis block: previousPoW = b1769976a749b969f3dd57ac31b302805a84665848600e57c756b1fad44a12d7
		BlockHeader newHeader = new BlockHeader(explorer.getLastBlockHeader(), root);
		String pow = newHeader.hash(difficulty);
		Block newBlock = new Block(newHeader, pow, tree.orderedTransactions());
		
		// Update UTXO list
		utxoExplorer.update(newBlock);
		
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
		
		ArrayList<TransactionReference> references = new ArrayList<TransactionReference>();
		
		// For each transaction
		for (int i = 0; i < transactions.size(); i++) {
			
			Transaction tx = transactions.get(i);
			byte[][] signatures = tx.finalise(blockExplorer);
			byte[] outputsInBytes = tx.getOutputsInBytes();
			
			ArrayList<Input> inputs = tx.inputs();
			
			// For each input
			for (int j = 0; j < inputs.size(); j++) {
				
				// Check UTXO list
				Input input = inputs.get(j);
				boolean validUTXO = utxoExplorer.valid(input);
				
				// Check signature
				TransactionReference reference = inputs.get(j).reference();
				RSAPublicKey publicKey = blockExplorer.recipientPublicKey(reference);
				
				Signature signature = new Signature();
				boolean validSignature = signature.verify(outputsInBytes, signatures[j], publicKey); 
				
				// Check for double-spending in same transaction or group of transactions
				boolean seen = seen(references, reference);
				if (!seen) references.add(reference);
				
				if (!validUTXO || !validSignature || seen) transactions.remove(i);
			}
		}
		
		return transactions;
	}
	
	private String totalFees(ArrayList<Transaction> finalisedTransactions) {
		
		int fees = 0;
		
		for (int i = 0; i < finalisedTransactions.size(); i++) {
			Transaction tx = finalisedTransactions.get(i);
			fees += Integer.parseInt(tx.transactionFee());
		}
		
		return String.valueOf(fees);
	}
	
	private boolean seen(ArrayList<TransactionReference> references, TransactionReference newReference) {
		
		for (int i = 0; i < references.size(); i++) {
			
			TransactionReference reference = references.get(i);
			
			if (newReference.pow().compareTo(reference.pow()) == 0 &
					newReference.transactionID().compareTo(reference.transactionID()) == 0 &
					newReference.outputID().compareTo(reference.outputID()) == 0) return true;
		}
		
		return false;
	}

}