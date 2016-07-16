package obj;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
//import java.util.logging.Logger;

import util.MerkleTree;

/**
 * <b>References</b>
 * <ul>
 * <li><a href="https://docs.oracle.com/javase/tutorial/jaxp/xslt/writingDom.html">Oracle Java Tutorials:
 * Writing Out a DOM as an XML File</a></li>
 * </ul>
 */
public class NetworkNode {
	
	public final static String ALGORITHM = "RSA";
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
	
	public void mine() throws NoSuchAlgorithmException, URISyntaxException, InvalidKeySpecException {
		
		Block newBlock = makeBlock();
		
		URI domain = NetworkNode.class.getClassLoader().getResource(BLOCKCHAIN).toURI();
		BlockExplorer explorer = new BlockExplorer(domain);
		explorer.extendBlockchain(newBlock, difficulty);
		
	}
	
	/*
	 * Private methods
	 */
	private void findPeers() {
		
	}
	
	private void updateBlockchain() {
		
	}
	
//	private void updateMempool(Transaction newTransaction) {
//		mempool.add(newTransaction);
//	}
	
	private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
		
		KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
		generator.initialize(512, new SecureRandom());
		
		return generator.generateKeyPair();	
	}
	
	private Block makeBlock() throws NoSuchAlgorithmException, InvalidKeySpecException, URISyntaxException {
		
		ArrayList<Transaction> transactions = cloneMempool();
		
		Transaction mint = new Transaction();
		
		KeyPair newKeyPair = generateKeyPair();
		wallet.save(newKeyPair, REWARD);
		
		Output gold = new Output(newKeyPair.getPublic(), REWARD);
		
		mint.addOutput(gold);
		transactions.add(mint);
		
		MerkleTree tree = new MerkleTree();
		String root = tree.getRoot(transactions);	// TODO mint transaction should be either first or last
		ArrayList<Transaction> orderedTransactions = tree.orderedTransactions();
		
		URI domain = NetworkNode.class.getClassLoader().getResource(BLOCKCHAIN).toURI();
		BlockExplorer explorer = new BlockExplorer(domain);
		
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