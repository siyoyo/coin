package obj;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.ArrayList;
//import java.util.logging.Logger;

import obj.Transaction.TransactionInputsLessThanOutputsException;
import util.MerkleTree;
import util.RSA512;
import util.Signature.ValidationFailureException;

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
	public final static int REWARD = 50;
	public static String difficulty = "00ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
	
//	private Logger logger = Logger.getLogger(NetworkNode.class.getName());
	private Wallet wallet;
	private ArrayList<Transaction> mempool;
	
	public NetworkNode() {
		wallet = new Wallet();
		mempool = new ArrayList<Transaction>();
//		updateMempool();
	}
	
	public Wallet wallet() {
		return wallet;
	}
	
	public void connect() {
		findPeers();
		updateBlockchain();
	}
	
	public void mine() throws URISyntaxException, TransactionInputsLessThanOutputsException, GeneralSecurityException, ValidationFailureException {
		
		URI domain = NetworkNode.class.getClassLoader().getResource(BLOCKCHAIN).toURI();
		BlockExplorer explorer = new BlockExplorer(domain);
		
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
	
	private Block makeBlock(BlockExplorer explorer) throws URISyntaxException, TransactionInputsLessThanOutputsException, GeneralSecurityException, ValidationFailureException {
		
		ArrayList<Transaction> transactions = cloneMempool();
		
		Transaction mint = new Transaction();
		
		RSA512 rsa512 = new RSA512();
		KeyPair newKeyPair = rsa512.generateKeyPair();
		wallet.save(newKeyPair, REWARD);
		
		Output gold = new Output(newKeyPair.getPublic(), REWARD);
		
		mint.addOutput(gold);
		
		MerkleTree tree = new MerkleTree();
		String root = tree.getRoot(explorer, mint, transactions);
		ArrayList<Transaction> orderedTransactions = tree.orderedTransactions();
		
		BlockHeader newHeader = new BlockHeader(explorer.getLastBlockHeader(), root); 
		String pow = newHeader.hash(difficulty);
		
		Block newBlock = new Block(newHeader, pow, orderedTransactions);
		mempool.clear();
		
		return newBlock;
		
	}
		
	private ArrayList<Transaction> cloneMempool() {
		
		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
		for (int i = 0; i < mempool.size(); i++) transactions.add(mempool.get(i));
		
		return transactions;
	}
	
}