package obj;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import obj.Transaction.TransactionInputsLessThanOutputsException;
import util.MerkleTree;
import util.RSA512;

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
	public final static String PEERS = "dat/peers.xml";
	
	public final static String REWARD = "50";
	public static String difficulty = "00ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
	
	public int port = 9999;
	private Socket client;
	private PrintWriter writer;
//	private ArrayList<Socket> peers;
	
	private BlockExplorer blockExplorer;
	private UTXOExplorer utxoExplorer;
	private Wallet wallet;
	private ArrayList<Transaction> mempool;
	
	public NetworkNode() throws ParserConfigurationException, SAXException, IOException, URISyntaxException, DOMException, TransactionInputsLessThanOutputsException, GeneralSecurityException, TransformerException {
		
		blockExplorer = new BlockExplorer(BLOCKCHAIN);
		utxoExplorer = new UTXOExplorer(UTXO);
		wallet = new Wallet();
		
//		peers = new ArrayList<Socket>();
		mempool = new ArrayList<Transaction>();
		
		ServerSocket server = new ServerSocket(port);
		client = server.accept();
		System.out.println("Connection received from " + client.getInetAddress().getHostName());
		writer = new PrintWriter(client.getOutputStream(), true);
		
//		findPeers();
		
	}
	
	public PrintWriter toClient() {
		return writer;
	}
	
//	public void updatePeerlist(Socket socket) {
//		peers.add(socket);
//	}
	
	public void updateMempool(Transaction transaction) {
		mempool.add(transaction);
	}

	public void mine() throws URISyntaxException, TransactionInputsLessThanOutputsException, GeneralSecurityException, ParserConfigurationException, SAXException, IOException, DOMException, TransformerException {
		Block newBlock = makeBlock(blockExplorer);
		broadcastBlock(newBlock);
		blockExplorer.extendBlockchain(newBlock, difficulty);
	}
		
	/*
	 * Private methods
	 */
//	private void findPeers() throws ParserConfigurationException, SAXException, IOException, URISyntaxException {
//		
//		Document doc = XMLio.parse(PEERS);
//		doc.getDocumentElement().normalize();
//		NodeList peerlist = doc.getElementsByTagName("peer");
//		
//		for (int i = 0; i < peerlist.getLength(); i++) {
//			String hostname = peerlist.item(i).getTextContent();
//			Socket peer = new Socket(hostname, port);
//			peers.add(peer);
//		}
//	}
	
	private void broadcastBlock(Block block) {
		writer.println(block.toString());
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
			
			System.out.println("Finalising transaction " + (i + 1) + " of " + transactions.size() + " ...");
			
			Transaction tx = transactions.get(i);
			tx.finalise(blockExplorer);

			// Check input against UTXO list and verify signatures
			boolean validTransaction = tx.validate(i, blockExplorer, utxoExplorer);
			
			// For each input
			ArrayList<Input> inputs = tx.inputs();
			for (int j = 0; j < inputs.size(); j++) {
				
				// Check for double-spending in same transaction or group of transactions
				TransactionReference reference = inputs.get(j).reference();
				boolean seen = seen(references, reference);
				System.out.println("Input " + (i + 1) + "." + j + " has been seen previously in this group of transactions: " + seen);
				if (!seen) references.add(reference);
				
				if (!validTransaction || seen) {
					transactions.remove(i);
					System.out.println("Input " + (i + 1) + "." + j + " is invalid and has been removed");
				}
				else System.out.println("Input " + (i + 1) + "." + j + " is valid");
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