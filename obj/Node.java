package obj;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.logging.Logger;

import util.MerkleTree;

public class Node {
	
	public final static String BLOCKCHAIN = "dat/blockchain.xml";
	public final static int REWARD = 50;
	public static String difficulty = "00ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
	
	private Logger logger = Logger.getLogger(Node.class.getName());
	private Wallet wallet;
	private ArrayList<Transaction> mempool;
	
	public Node() {
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
	
	public void mine() throws NoSuchAlgorithmException {
		makeBlock();		
	}
	
	/*
	 * Private methods
	 */
	private void findPeers() {
		
	}
	
	private void updateBlockchain() {
		
	}
	
	private void updateMempool(Transaction newTransaction) {
		mempool.add(newTransaction);
	}
	
	private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
		
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(512, new SecureRandom());
		
		return generator.generateKeyPair();	
	}
	
	private Block makeBlock() throws NoSuchAlgorithmException {
		
		Transaction mint = new Transaction();
		
		KeyPair newKeyPair = generateKeyPair();
		wallet.save(newKeyPair, REWARD);
		logger.info("Private key: " + newKeyPair.getPrivate());
		
		Output gold = new Output(newKeyPair.getPublic(), REWARD);
		logger.info("Public key: " + newKeyPair.getPublic());
		
		mint.addOutput(gold);
		mempool.add(mint);
		
		MerkleTree tree = new MerkleTree();
		String root = tree.getRoot(mempool);
		
		BlockHeader newHeader = new BlockHeader("previousHash", root);	// TODO
		String pow = newHeader.hash(difficulty);
		
		Block newBlock = new Block(newHeader, pow, mempool);
		
		return newBlock;
		
	}

}
