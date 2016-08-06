package p2p;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import obj.Block;
import obj.BlockHeader;
import obj.Input;
import obj.Output;
import obj.Transaction;
import obj.TransactionReference;
import obj.Wallet;
import obj.Transaction.TransactionInputsLessThanOutputsException;
import util.BaseConverter;
import util.BlockExplorer;
import util.MerkleTree;
import util.RSA512;
import util.UTXOExplorer;
import util.XMLio;

/**
 * <b>References</b>
 * <ul>
 * <li><a href="https://docs.oracle.com/javase/tutorial/jaxp/xslt/writingDom.html">Oracle Java Tutorials:
 * Writing Out a DOM as an XML File</a></li>
 * <li><a href="http://stackoverflow.com/questions/363681/generating-random-integers-in-a-specific-range">
 * Generating random integers in a specific range</a></li>
 * </ul>
 */
public class NetworkNode {
	
	public final static String BLOCKCHAIN = "dat/blockchain";
	public final static String UTXO = "dat/utxo";
	public final static String PEERS = "dat/peers.xml";
	public final static String EXT = ".xml";
	
	public final static String REWARD = "50";
	public static String difficulty = "00008fffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
	
	private ArrayList<Peer> peers;
	private ArrayList<PrintWriter> toPeers;
	
	private BlockExplorer blockExplorer;
	private UTXOExplorer utxoExplorer;
	private Wallet wallet;
	private ArrayList<Transaction> mempool;
	
	public NetworkNode() throws ParserConfigurationException, SAXException, IOException, URISyntaxException  {
		wallet = new Wallet();
		peers = new ArrayList<Peer>();
		toPeers = new ArrayList<PrintWriter>();
		mempool = new ArrayList<Transaction>();
	}
	
	public static void main (String[] args) {
		
		int port = Integer.parseInt(args[0]);
		NetworkNode node;
		
		try {

			node = new NetworkNode();
			
			// Start server
			ServerSocket server = new ServerSocket(port);
			String hostname = server.getInetAddress().getHostName();
			System.out.println("I am " + hostname + " " + port);
			
			SocketListener socketListener = node.new SocketListener(server);
			socketListener.start();
			
			// Initialise explorers
			node.initialiseExplorers(BLOCKCHAIN + "_" + hostname + "_" + port + EXT, UTXO + "_" + hostname + "_" + port + EXT);
			
			// Find peers
			for (int i = 0; i < 3; i++) node.findPeer(hostname, port);
			
			for (int i = 0; i < node.peers.size(); i++) {
				Peer peer = node.peers.get(i);
				System.out.println("peer " + i + ": " + peer.hostname() + " " + peer.port());
			}
			
			// Start listening for new blocks
			BlockListener listener = node.new BlockListener();
			listener.start();
			
			// Start mining
			Miner miner = node.new Miner();
			miner.start();
			
		} catch (IOException e) {
			// ServerSocket exception
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// NetworkNode exception
			e.printStackTrace();
		} catch (SAXException e) {
			// NetworkNode exception
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// NetworkNode exception
			e.printStackTrace();
		} 	
	}
	
	public void updateMempool(Transaction transaction) {
		mempool.add(transaction);
	}
	
	public void initialiseExplorers(String blockchainFilename, String utxoFilename) throws ParserConfigurationException, SAXException, IOException, URISyntaxException {
		blockExplorer = new BlockExplorer(blockchainFilename);
		utxoExplorer = new UTXOExplorer(utxoFilename);
	}

	public void mine() throws URISyntaxException, TransactionInputsLessThanOutputsException, GeneralSecurityException, ParserConfigurationException, SAXException, IOException, DOMException, TransformerException {
		Block block = makeBlock(blockExplorer);
		if (isNewBlock(block)) {
			blockExplorer.extendBlockchain(block, difficulty);
			broadcastBlock(block);
		}
	}
	
	class SocketListener extends Thread {
		
		private ServerSocket server;
		
		public SocketListener(ServerSocket server) {
			super("Socket listener");
			this.server = server;
		}
		
		public void run() {
		
			while (true) {
				try {
					Socket socket = server.accept();
					connect(socket);
					System.out.println("Connection received from " + socket.getInetAddress().getHostName() + " " + socket.getPort());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	class BlockListener extends Thread {
		
		public BlockListener() {
			super("Block listener");
		}
		
		public void run() {
			
			String incoming;
			
			while (true) {
				
				for (int i = 0; i < peers.size(); i++) {
					
					Peer peer = peers.get(i);
					BufferedReader reader = peer.reader();
					
					try {
						
						incoming = reader.readLine();
						
						if (incoming != null) {
							
							System.out.println("Receiving block from " + peer.hostname() + " " + peer.port());
							Block block = readBlock(incoming);
							
							if (isNewBlock(block)) {
								blockExplorer.extendBlockchain(block, block.header().difficulty());
								broadcastBlock(block);
							}
						}
					} catch (IOException e) {
						// BufferedReader exception
						e.printStackTrace();
					} catch (InvalidKeyException e) {
						// readBlock exception
						e.printStackTrace();
					} catch (InvalidKeySpecException e) {
						// readBlock exception
						e.printStackTrace();
					} catch (NoSuchAlgorithmException e) {
						// readBlock exception
						e.printStackTrace();
					} catch (SignatureException e) {
						// readBlock exception
						e.printStackTrace();
					} catch (DOMException e) {
						// extendBlockchain exception
						e.printStackTrace();
					} catch (ParserConfigurationException e) {
						// extendBlockchain exception
						e.printStackTrace();
					} catch (SAXException e) {
						// extendBlockchain exception
						e.printStackTrace();
					} catch (TransformerException e) {
						// extendBlockchain exception
						e.printStackTrace();
					} catch (URISyntaxException e) {
						// extendBlockchain exception
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	class Miner extends Thread {
		
		public Miner() {
			super("Miner");
		}
		
		public void run() {
			try {
				while (true) mine();
			} catch (DOMException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (TransactionInputsLessThanOutputsException e) {
				e.printStackTrace();
			} catch (GeneralSecurityException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (TransformerException e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * Private methods
	 */
	private boolean isNewBlock(Block block) {
		String latestHeader = blockExplorer.getLastBlockHeader();
		if (block.header().previousPoW().compareTo(latestHeader) == 0) return true;
		else return false;
	}
	
	private void findPeer(String thisHostname, int thisPort) throws ParserConfigurationException, SAXException, IOException, URISyntaxException {
		
		Document doc = XMLio.parse(PEERS);
		doc.getDocumentElement().normalize();
		
		NodeList hosts = doc.getElementsByTagName("hostname");
		NodeList ports = doc.getElementsByTagName("port");
		
		String hostname = thisHostname;
		int port = thisPort;
		int randomIndex = ThreadLocalRandom.current().nextInt(hosts.getLength());
		
		boolean self = (hostname.compareTo(thisHostname) == 0) & (port == thisPort);
		boolean unique = false;
		
		while (self || !unique) {
			hostname = hosts.item(randomIndex).getTextContent();
			port = Integer.valueOf(ports.item(randomIndex).getTextContent());
			
			self = thisHostname.compareTo(hostname) == 0 & thisPort == port; 
			unique = unique(hostname, port);
		
			randomIndex = ThreadLocalRandom.current().nextInt(hosts.getLength());
		}
		
		Socket socket = new Socket(hostname, port);			// All peers listed must be on line otherwise deadlock
		connect(socket);
	}
	
	private void connect(Socket socket) throws IOException {
		peers.add(new Peer(socket));
		PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
		toPeers.add(writer);
	}
	
	private boolean unique(String hostname, int port) {
		for (int i = 0; i < peers.size(); i++) {
			Peer peer = peers.get(i);
			if (hostname.compareTo(peer.hostname()) == 0 & port == peer.port()) return false;
		}		
		return true;
	}
	
	private void broadcastBlock(Block block) {
		for (int i = 0; i < toPeers.size(); i++) toPeers.get(i).println(block.toString());
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
	
	private Block readBlock(String string) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		
		String[] sections = string.split(" HEAD ");
		String header = sections[0];
		String body = sections[1];
		
		sections = header.split(" ");
		
		String previousPoW = sections[0];
		String pow = sections[1];
		String merkleRoot = sections[2];
		String difficulty = sections[3];
		int nonce = Integer.parseInt(sections[4]);
		int numberOfTransactions = Integer.valueOf(sections[5]);
		
		BlockHeader blockHeader = new BlockHeader(previousPoW, merkleRoot);
		blockHeader.setDifficulty(difficulty);
		blockHeader.setNonce(nonce);
		
		sections = body.split(" TX ");
		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
		
		for (int i = 0; i < numberOfTransactions; i++) {
			
			Transaction transaction = new Transaction();
			
			String section = sections[i];
			String[] subsections = section.split("#");
			
			int numberOfInputs = Integer.parseInt(subsections[0]);
			int numberOfOutputs = Integer.parseInt(subsections[1]);
			
			// Inputs
			String tx = subsections[2];
			subsections = tx.split(" IN ");
			
			String inputs = subsections[0];
			String[] in = inputs.split(" ");
			
			for (int j = 0; j < numberOfInputs; j++) {
				String refPoW = in[j + 0];
				String refTxID = in[j + 1];
				String refOutputID = in[j + 2];
				TransactionReference reference = new TransactionReference(refPoW, refTxID, refOutputID);
				Input input = new Input(reference);
				transaction.addInput(input);
			}
			
			// Outputs
			String subsection = subsections[1];
			subsections = subsection.split(" OUT ");
			
			String outputs = subsections[0];
			String[] outs = outputs.split(" ");
			
			KeyFactory factory = KeyFactory.getInstance("RSA");
			
			for (int j = 0; j < numberOfOutputs; j++) {
				String key = outs[j + 0];
				byte[] encodedPublicKey = BaseConverter.stringHexToDec(key);
				X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
				RSAPublicKey publicKey = (RSAPublicKey) factory.generatePublic(publicKeySpec);
				String amount = outs[j + 1];
				Output output = new Output(publicKey, amount);
				transaction.addOutput(output);
			}
			
			// Signatures
			if (numberOfInputs > 0) {
				
				String signatures = subsections[1];
				String[] sigs = signatures.split(" ");
				transaction.initialiseSignatures(numberOfInputs);
				
				for (int j = 0; j < numberOfInputs; j++) transaction.signatures()[j] = BaseConverter.stringHexToDec(sigs[j]);
			}
			
			transactions.add(transaction);
		}
		
		Block reconstructedBlock = new Block(blockHeader, pow, transactions);
		System.out.println("Block " + reconstructedBlock.pow() + " received and reconstructed: "+ (reconstructedBlock.toString().compareTo(string) == 0));
		
		boolean validBlock = reconstructedBlock.validate(blockExplorer, utxoExplorer);
		System.out.println("Block " + reconstructedBlock.pow() + " validated: " + validBlock);
		
		if (validBlock) return reconstructedBlock;
		return null;
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