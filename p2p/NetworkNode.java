package p2p;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;

import org.w3c.dom.Node;

import gui.TransactionalNode;
import obj.Block;
import obj.BlockHeader;
import obj.BlockHeader.BlockHeaderException;
import obj.Input;
import obj.Output;
import obj.Transaction;
import obj.TransactionReference;
import obj.Transaction.TransactionException;
import util.BaseConverter;
import util.BlockExplorer;
import util.MerkleTree;
import util.MerkleTree.MerkleTreeException;
import util.RSA512;
import util.UTXOExplorer;
import util.WalletExplorer;

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
	public final static String WALLET = "dat/wallet";
	public final static String PEERS = "dat/peers.xml";
	public final static String EXT = ".xml";
	
	public final static int REWARD = 50;
	public static String difficulty = "000000ffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
	
	private String hostname;
	private int port;
	private ServerSocket server;
	
	private ArrayList<Peer> peers;
	private ArrayList<PrintWriter> toPeers;
	private String msgSeparator = "MSG";
	private String blockSeparator = "BLK";
	private String separator = " ";
	
	private BlockExplorer blockExplorer;
	private UTXOExplorer utxoExplorer;
	private WalletExplorer walletExplorer;
	
	private ArrayList<Transaction> mempool;
	
	private ConsensusBuilder consensusBuilder;
	private Miner miner;
	
	public NetworkNode() {
		peers = new ArrayList<Peer>();
		toPeers = new ArrayList<PrintWriter>();
		mempool = new ArrayList<Transaction>();
	}
	
	public static void main (String[] args) {
		
		NetworkNode node = new NetworkNode();
		
		// Parse arguments
		if (args[0].compareTo("-t") == 0) /*Launch TransactionalNode*/ {
			
			if (args.length != 2) {
				System.err.println("Arguments: [-t (optional)] [own port]");
				System.exit(0);
			} else {
				
				// Start server
				node.port = Integer.valueOf(args[1]);
				try {
					node.server = new ServerSocket(node.port);
				} catch (IOException e) {
					e.printStackTrace();
				}
				node.hostname = node.server.getInetAddress().getHostAddress();
				
				// Launch TransactionalNode
				TransactionalNode.getInstance(node.hostname, node.port);
			}
		}
		
		else /*Do not launch TransactionalNode*/ {
			
			if (args.length != 1) {
				System.err.println("Arguments: [-t (optional)] [own port]");
				System.exit(0);
			} else {
				
				// Start server
				node.port = Integer.valueOf(args[0]);
				try {
					node.server = new ServerSocket(node.port);
				} catch (IOException e) {
					e.printStackTrace();
				}
				node.hostname = node.server.getInetAddress().getHostAddress();
			}
		}
		
		System.out.println(LocalDateTime.now() + " I am " + node.hostname + " " + node.port);
		
		// Keep listening for connection requests
		SocketListener socketListener = node.new SocketListener(node.server);
		socketListener.start();
		
		// Initialize explorers
		node.initialiseExplorers(
				BLOCKCHAIN + "_" + node.hostname + "_" + node.port + EXT,
				UTXO + "_" + node.hostname + "_" + node.port + EXT,
				WALLET + "_" + node.hostname + "_" + node.port + EXT);
		
		// Connect to peers
		Scanner robot = new Scanner(System.in);
		String response, peerHostname;
		int peerPort;
		Socket socket;
		
		System.out.print("Connect to peer? (Y/N): ");
		response = robot.nextLine();
		
		while (response.compareTo("Y") == 0 || response.compareTo("y") == 0) {
			
			System.out.print("Hostname: ");
			peerHostname = robot.nextLine();
			
			System.out.print("Port: ");
			peerPort = Integer.valueOf(robot.nextLine());
			
			try {
				socket = new Socket(peerHostname, peerPort);
				node.connect(socket);
				System.out.println("Connected to " + peerHostname + " " + peerPort);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			System.out.print("Connect to peer? (Y/N): ");
			response = robot.nextLine();
		}
		
		robot.close();
		
		// Start listening to peers
		BlockListener listener = node.new BlockListener();
		listener.start();
		
		// Start broadcasting height
		Pong pong = node.new Pong();
		pong.start();
		
		// Start syncing chain
		node.consensusBuilder = node.new ConsensusBuilder();
		node.consensusBuilder.syncChain();
		
		// Start mining
		node.miner = node.new Miner();
		node.miner.start();
		
		node.consensusBuilder.start();
	}
	
	/**
	 * Initializes the BlockExplorer and UTXOExplorer with the provided filenames.
	 * @param blockchainFilename blockchain filename
	 * @param utxoFilename UTXO filename
	 */
	public void initialiseExplorers(String blockchainFilename, String utxoFilename, String walletFilename) {
		blockExplorer = new BlockExplorer(blockchainFilename);
		utxoExplorer = new UTXOExplorer(utxoFilename);
		walletExplorer = new WalletExplorer(walletFilename);
	}
	
	/**
	 * Adds a transaction to the memory pool.
	 * @param transaction
	 */
	public void updateMempool(Transaction transaction) {
		mempool.add(transaction);
	}
	
	/* -----------------------------------------------------------------
	 * 							SocketListener
	 * 							& associated methods
	 * -----------------------------------------------------------------
	 */
	
	/**
	 * Keeps a server socket live to accept any incoming connection requests.
	 */
	class SocketListener extends Thread {
		
		private ServerSocket server;
		
		public SocketListener(ServerSocket server) {
			super("Socket listener");
			this.server = server;
		}
		
		public void run() {
			System.out.println(LocalDateTime.now() + " SocketListener thread started");
			while (true) {
				if (this.isInterrupted()) System.out.println("SocketListener is interrupted");
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
	
	/*
	 * Creates a new Peer object using the socket provided.  Adds the new 
	 * peer to the list of peers (for polling using BlockListener).  Adds
	 * the associated PrintWriter to the list of toPeers (for broadcasting).
	 */
	private void connect(Socket socket) throws IOException {
		Peer peer = new Peer(socket);
		peers.add(peer);
		toPeers.add(peer.writer());
	}
	
	/* -----------------------------------------------------------------
	 * 							BlockListener
	 * 							& associated methods
	 * -----------------------------------------------------------------
	 */
	
	/**
	 * Continuously polls each peer for any incoming messages and handles different message
	 * types:
	 * <ul>
	 * <li>request for blockchain height</li>
	 * <li>blockchain height response</li>
	 * <li>request for missing blocks</li>
	 * <li>block broadcast</li>
	 */
	class BlockListener extends Thread {
		
		public BlockListener() {
			super("Block listener");
		}
		
		public void run() {
			
			System.out.println(LocalDateTime.now() + " BlockListener thread started");
			
			BufferedReader reader;
			Peer peer;
			String message, msgType, msgBody, responseMsgType, response;
			String[] split, s;
			String height, pow, same;
			int iHeight;
			
			while (true) {
				if (this.isInterrupted()) System.out.println("BlockListener is interrupted");
				for (int i = 0; i < peers.size(); i++) {
					
					peer = peers.get(i);
					reader = peer.reader();
					
					message = null;
					msgType = null;
					msgBody = null;
					
					try {
						message = reader.readLine();
					} catch (IOException e){
						e.printStackTrace();
					}
					
					if (message != null) {
						
						split = message.split(msgSeparator);
						msgType = split[0];
						if (split.length > 1) msgBody = split[1];
						
						switch (msgType) {
						
							case "0": /*Height broadcast*/ {
								int peerHeight = Integer.valueOf(msgBody);
								peer.updateCurrentHeight(peerHeight);
								break;
							}
							
							case "1": /*Check hash by height*/ {
								
								s = msgBody.split(separator);
								height = s[0];
								pow = s[1];
								
								responseMsgType = "2";
								response = height + separator + pow + separator;
								
								if (pow.compareTo(blockExplorer.getPoWByHeight(height)) == 0) response += "true";
								else response += "false";
								peer.writer().println(responseMsgType + msgSeparator + response);
								System.out.println(responseMsgType + msgSeparator + response);
								break;
							}
							
							case "2": /*Check hash by height response*/ {
								
								s = msgBody.split(separator);
								height = s[0];
								iHeight = Integer.valueOf(height);
								pow = s[1];
								same = s[2];
								
								// If hash at height is correct, set ConsensusBuilder's max consensus height
								if (same.compareTo("true") == 0) {
									consensusBuilder.setMaxConsensusHeight(iHeight);
									break;
								}
								
								// If hash at height is incorrect, check hash at height - 1
								if (same.compareTo("false") == 0) {
									responseMsgType = "1";
									String requestHeight = String.valueOf(iHeight - 1);
									response = requestHeight + separator + blockExplorer.getPoWByHeight(requestHeight);
									peer.writer().println(responseMsgType + msgSeparator + response);
									System.out.println(responseMsgType + msgSeparator + response);
									break;
								}
								
								break;
							}
							
							case "3": /*Block by height request*/ {
								
								responseMsgType = "4";
								response = new String();
								
								s = msgBody.split(separator);
								
								for (int j = 0; j < s.length; j++) 
									response += blockExplorer.getBlock(s[j]).toString() + blockSeparator;
									
								peer.writer().println(responseMsgType + msgSeparator + response);
								System.out.println(responseMsgType + msgSeparator + response);
								break;
							}
							
							case "4": /*Block by height response */ {
								
								s = msgBody.split(blockSeparator);
								
								Block block;
								for (int j = 0; j < s.length; j++) {
									try {
										block = readBlock(s[j], peer.hostname() + " port " + peer.port());
										peer.storedBlocks().add(block);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
								break;
							}
							
							case "5": /*Block broadcast*/ {
								
								Block block = null;
								
								try {
									block = readBlock(msgBody, peer.hostname() + " port " + peer.port());
								} catch (Exception e) {
									e.printStackTrace();
								}
								
								if (isNewBlock(block)) {
									blockExplorer.addNewBlock(block);
									blockExplorer.updateBlockchainFile();
									broadcast("5", msgBody);
								}
								
								break;
							}
							
							case "6": /*Transaction broadcast*/ {
								
								System.out.println("Receiving transaction from " + peer.hostname() + " port " + peer.port());
								
								try {
									
									Transaction transaction = readTransaction(msgBody);
									
									if (transaction.validate(blockExplorer, utxoExplorer)) {
										
										mempool.add(transaction);
										peer.writer().println("OK");
										broadcast("6", msgBody);
										
									} else peer.writer().println("FAIL");
									
								} catch (Exception e) {
									e.printStackTrace();
								}
								break;
							}
						}
					} 
				}
			}
		}
	}
	
	/*
	 * Re-constructs a block from the received block broadcast.  Returns a block
	 * if the re-constructed block is valid; null otherwise.
	 */
	private Block readBlock(String string, String peer) throws MalformedTransactionException, BlockHeaderException, MerkleTreeException, TransactionException {
		
		System.out.println("Receiving block from " + peer);
		
		String[] sections = string.split("HEAD");
		String header = sections[0];
		String _header = sections[1];
		
		// Header
		String[] headerSections = header.split(separator);
		
		int height = Integer.valueOf(headerSections[0]);
		String previousPoW = headerSections[1];
		String pow = headerSections[2];
		String merkleRoot = headerSections[3];
		String difficulty = headerSections[4];
		int nonce = Integer.valueOf(headerSections[5]);
		
		BlockHeader blockHeader = new BlockHeader(previousPoW, merkleRoot);
		blockHeader.setDifficulty(difficulty);
		blockHeader.setNonce(nonce);
		
		// Meta data
		String[] numTx = _header.split("NUM");
		int numberOfTransactions = Integer.valueOf(numTx[0]);
		String _numTx = numTx[1];
		
		// Transactions
		String[] txs = _numTx.split("TX");
		if (txs.length != numberOfTransactions) throw new MalformedTransactionException("Mismatched number of transactions");
		
		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
		Transaction transaction;
		for (int i = 0; i < numberOfTransactions; i++) {
			transaction = readTransaction(txs[i]);
			transactions.add(transaction);
		}
		
		Block reconstructedBlock = new Block(blockHeader, pow, transactions);
		reconstructedBlock.setHeight(height);
		System.out.println("Block " + reconstructedBlock.pow() + " received and reconstructed: "+ (reconstructedBlock.toString().compareTo(string) == 0));
		
		boolean validBlock = reconstructedBlock.validate(blockExplorer, utxoExplorer);
		System.out.println("Block " + reconstructedBlock.pow() + " is valid: " + validBlock);
		
		return reconstructedBlock;
	}
	
	/*
	 * Parses a transaction message and returns a Transaction object.
	 */
	private Transaction readTransaction(String tx) throws MalformedTransactionException {
		
		Transaction transaction = new Transaction();
		
		int numberOfInputs, inputID;
		Input input;
		TransactionReference reference;
		String refpow, reftx, refout;
		
		int numberOfOutputs, outputID;
		Output output;
		String amount;
		
		byte[] encodedPublicKeySpecBytes;
		X509EncodedKeySpec encodedPublicKeySpec;
		RSAPublicKey publicKey = null;
		
		KeyFactory factory = null;
		try {
			factory = KeyFactory.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		String inputs, outputs, signatures, in, out;
		String[] nums, parts, ins, outs, sigs, inparts, outparts;
		int txID;
		
		nums = tx.split("#");
		txID = Integer.valueOf(nums[0]);
		numberOfInputs = Integer.valueOf(nums[1]);
		numberOfOutputs = Integer.valueOf(nums[2]);
		tx = nums[3];
		
		transaction.setTxID(txID);
		
		parts = tx.split("END");
		if (numberOfInputs > 0) {
			if (parts.length != 4) throw new MalformedTransactionException();
			else {	
				inputs = parts[0];
				outputs = parts[1];
				signatures = parts[2];
				
				// Inputs
				ins = inputs.split("IN");
				if (ins.length != numberOfInputs) throw new MalformedTransactionException("Mismatched number of inputs");
				else {
					for (int j = 0; j < ins.length; j++) {
						
						in = ins[j];
						
						inparts = in.split(separator);
						inputID = Integer.valueOf(inparts[0]);
						refpow = inparts[1];
						reftx = inparts[2];
						refout = inparts[3];
						
						reference = new TransactionReference(refpow, reftx, refout);
						input = new Input(reference);
						input.setInputID(inputID);
						
						transaction.addInput(input);
					}
				}
				
				// Signatures
				sigs = signatures.split("SIG");
				if (sigs.length != ins.length) throw new MalformedTransactionException("Number of signatures does not match number of inputs");
				else {
					
					transaction.initialiseSignatures(sigs.length + 1);
					
					for (int k = 0; k < sigs.length; k++) 
						transaction.signatures()[k + 1] = BaseConverter.stringHexToDec(sigs[k]);
				}
			}
		} else /* numberOfInputs = 0 */ {
			inputs = parts[0];
			outputs = parts[1];
		}
		
		// Outputs
		outs = outputs.split("OUT");
		if (outs.length != numberOfOutputs) throw new MalformedTransactionException("Mismatched number of outputs");
		else {
			for (int m = 0; m < outs.length; m++) {
				
				out = outs[m];
				
				outparts = out.split(separator);
				outputID = Integer.valueOf(outparts[0]);
				encodedPublicKeySpecBytes = BaseConverter.stringHexToDec(outparts[1]);
				encodedPublicKeySpec = new X509EncodedKeySpec(encodedPublicKeySpecBytes);
				amount = outparts[2];
				
				try {
					publicKey = (RSAPublicKey) factory.generatePublic(encodedPublicKeySpec);
				} catch (InvalidKeySpecException e) {
					e.printStackTrace();
				}
				
				output = new Output(publicKey, amount);
				output.setOutputID(outputID);
				
				transaction.addOutput(output);
			}
		}
		
		return transaction;
	}
	
	@SuppressWarnings("serial")
	public class MalformedTransactionException extends Exception {
		
		public MalformedTransactionException() {
			super();
		}
		
		public MalformedTransactionException(String msg) {
			super(msg);
		}
		
	}
	
	/* -----------------------------------------------------------------
	 * 							ConsensusBuilder
	 * 							& associated methods
	 * -----------------------------------------------------------------
	 */
	
	/**
	 * Periodically checks the longest chain
	 *
	 */
	class ConsensusBuilder extends Thread {
		
		private int maxConsensusHeight;
		private int numberOfBlocks;
		
		public ConsensusBuilder() {
			super("Consensus builder");
		}
		
		public void run() {
			
			System.out.println(LocalDateTime.now() + " ConsensusBuilder thread started");
			
			while (true) {
				if (this.isInterrupted()) System.out.println("ConsensusBuilder is interrupted");
				syncChain();
				try {
					sleep(60000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		private void setMaxConsensusHeight(int maxConsensusHeight) {
			this.maxConsensusHeight = maxConsensusHeight;
			System.out.println("max consensus height: " + this.maxConsensusHeight);
		}
		
		private void syncChain() {
			
			Peer peer, syncToPeer = null;
			int ownHeight = blockExplorer.getBlockchainHeight();
			int maxHeight = -1;
			int peerHeight;
			
			System.out.println("own height: " + ownHeight);
			
			// Check chain height of peers			
			for (int i = 0; i < peers.size(); i++) {
				
				peer = peers.get(i);
				peerHeight = peer.currentHeight();
				
				if (peerHeight > maxHeight) {
					maxHeight = peerHeight;
					syncToPeer = peer;
				}
			}
			
			System.out.println("max height: " + maxHeight);
			
			// Start sync with longest chain
			String msgType, message;
			if (maxHeight > ownHeight) {
				
				System.out.println("Syncing with " + syncToPeer.hostname() + " " + syncToPeer.port());
				
				// Reset values
				maxConsensusHeight = -1;
				numberOfBlocks = -1;
				
				// Initialize storedBlocks
				syncToPeer.initialiseStoredBlocks();
				
				// Start consensus process
				msgType = "1"; /* Check hash at height */
				message = ownHeight + separator + blockExplorer.getPoWByHeight(String.valueOf(ownHeight));
				syncToPeer.writer().println(msgType + msgSeparator + message);
				System.out.println(msgType + msgSeparator + message);
				
				while (maxConsensusHeight == -1) System.out.println("Waiting...");/* Wait to determine number of blocks */
				numberOfBlocks = maxHeight - maxConsensusHeight;
				
				System.out.println("height difference: " + numberOfBlocks);
				
				// Request blocks
				msgType = "3";
				message = new String();
				int start = maxConsensusHeight + 1;
				
				for (int requestedHeight = start; requestedHeight <= maxHeight; requestedHeight++)
					message += String.valueOf(requestedHeight) + separator;
				
				syncToPeer.writer().println(msgType + msgSeparator + message);
				
				System.out.println(msgType + msgSeparator + message);
				
				while (syncToPeer.storedBlocks().size() < numberOfBlocks) System.out.println("size of stored blocks: " + syncToPeer.storedBlocks().size());/* Wait to receive all requested blocks */
				
				
				// Remove blocks
				Node blockNode;
				for (int height = start; height <= ownHeight; height++) {
					blockNode = blockExplorer.getBlockNodeByHeight(String.valueOf(height));
					blockExplorer.removeBlockNode(blockNode);
					System.out.println("Removed block: " + height);
				}
				
				// Add new blocks
				ArrayList<Block> storedBlocks = syncToPeer.storedBlocks();
				for (int i = 0; i < storedBlocks.size(); i++) {
					blockExplorer.addNewBlock(storedBlocks.get(i));
					System.out.println("Add block: " + i + " of " + storedBlocks.size());
				}
				
				// Update blockchain file
				blockExplorer.updateBlockchainFile();
				
				// Rebuild UTXO file
				utxoExplorer.rebuildUTXOList(blockExplorer);
			}
		}
	}
	
	/* -----------------------------------------------------------------
	 * 							Pong
	 * 							& associated methods
	 * -----------------------------------------------------------------
	 */
	
	/**
	 * Continuously broadcasts own height.
	 */
 	class Pong extends Thread {
		
		public Pong() {
			super("Pong");
		}
		
		public void run() {
			
			System.out.println(LocalDateTime.now() + " Pong thread started");
			
			String msgType = "0";
			String height;
			
			while (true) {
				if (this.isInterrupted()) System.out.println("Pong is interrupted");
				height = String.valueOf(blockExplorer.getBlockchainHeight());
				for (int i = 0; i < toPeers.size(); i++) toPeers.get(i).println(msgType + msgSeparator + height); 
				
				try {
					sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
 	
	
	/* -----------------------------------------------------------------
	 * 							Miner
	 * 							& associated methods
	 * -----------------------------------------------------------------
	 */
 	
	
	/**
	 * Continuously mines new blocks.
	 */
	class Miner extends Thread {
		
		public Miner() {
			super("Miner");
		}
		
		public void run() {
			System.out.println(LocalDateTime.now() + " Miner thread started");
			while (true) {
				if (this.isInterrupted()) System.out.println("Miner is interrupted");
				mine();
			}
		}
	}
	
	/**
	 * Mines a block.  The block is added to the blockchain if it is a valid new
	 * block and then broadcast to peers.  The UTXO list is updated and the memory
	 * pool is cleared of transactions.
	 */
	public void mine() {
		
		Block block = makeBlock(blockExplorer);
		
		if (isNewBlock(block)) {
			
			// Add block to the blockchain and broadcast to peers
			block.setHeight(blockExplorer.getBlockchainHeight() + 1);
			blockExplorer.addNewBlock(block);
			blockExplorer.updateBlockchainFile();
			broadcast("5", block.toString());
			
			// Update UTXO list
			utxoExplorer.update(block);
			utxoExplorer.updateUTXOFile();
			
			// Update wallet balance
			TransactionReference reference = new TransactionReference(block.pow(), "1", "1");
			String address = blockExplorer.outputAddress(reference);
			walletExplorer.updateBalance(address, REWARD);
			
		} // Block is discarded if it is not the latest block
	}
	
	
	/*
	 * Returns a new block after finalizing the transactions which will be included
	 * (every valid transaction in the memory pool), creating a mint transaction,
	 * obtaining a Merkle root of the tree of transactions (including the mint transaction),
	 * and successfully finding a solution for the proof-of-work hash.
	 */
	private Block makeBlock(BlockExplorer blockExplorer) {
		
		// Clone memory pool
		ArrayList<Transaction> transactions = cloneMempool();
		
		// Finalize transactions 
		transactions = finalise(transactions);
		
		// Add transaction fees to the mining reward
		int totalReward = totalFees(transactions) + REWARD;
		
		// Create new key pair and save to wallet
		// If block is not accepted, key pair is saved but the balance is invalid
		// (cannot be spent as there is no valid transaction reference)
		KeyPair newKeyPair = RSA512.generateKeyPair();
		RSAPublicKey publicKey = (RSAPublicKey) newKeyPair.getPublic();
		walletExplorer.save(newKeyPair, "0");
		
		// Create mint transaction
		Output gold = new Output(publicKey, String.valueOf(totalReward));
		gold.setOutputID(1);
		
		Transaction mint = new Transaction();
		mint.addOutput(gold);
		
		// Mint transaction is always at index 0
		transactions.add(0, mint);
		
		// Get Merkle root
		String root = MerkleTree.getRoot(transactions);
		BlockHeader header = new BlockHeader(blockExplorer.getLastPoW(), root);
		
		// Hash to find proof-of-work
		String pow = header.hash(difficulty);
		
		return new Block(header, pow, transactions);	
	}
	
	
	/*
	 * Returns a shallow clone of the transactions in the memory pool.
	 */
	private ArrayList<Transaction> cloneMempool() {
		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
		for (int i = 0; i < mempool.size(); i++) transactions.add(mempool.get(i));
		return transactions;
	}
	
	/*
	 * Removes any transaction which attempts to double-spend within the same transaction or 
	 * group of transactions.  Finalizes the transactions which remain. 
	 */
	private ArrayList<Transaction> finalise(ArrayList<Transaction> transactions) {
		
		ArrayList<TransactionReference> references = new ArrayList<TransactionReference>();
		TransactionReference reference;
		boolean seen;
		
		Transaction tx;
		ArrayList<Input> inputs;
		Input input;
		String inputID;
		
		// Check for double-spending in same transaction or group of transactions
		for (int i = 0; i < transactions.size(); i++) {
			
			tx = transactions.get(i);
			
			inputs = tx.inputs();
			for (int j = 0; j < inputs.size(); j++) {
				
				input = inputs.get(j);
				inputID = input.inputID();
				reference = input.reference();
				
				seen = seen(references, reference);
				
				if (seen) {
					transactions.remove(i);
					System.out.println("Input " + i + "." + inputID + " is invalid and has been removed");
				}
				else references.add(reference);
			}
		}
		
		// Validate transactions
		boolean validTransaction = false;
		
		for ( int k = 0; k < transactions.size(); k++) {
			tx = transactions.get(k);
			System.out.println("Validating transaction " + (k + 1) + " of " + transactions.size() + " ...");
			try {
				validTransaction = tx.validate(blockExplorer, utxoExplorer);
				if (!validTransaction) transactions.remove(tx);
			} catch (TransactionException e) {
				transactions.remove(tx);
				e.printStackTrace();
			}
			
		}
		return transactions;
	}
	
	/*
	 * Returns true if this transaction reference matches any in the list of transaction
	 * references.
	 */
	private boolean seen(ArrayList<TransactionReference> references, TransactionReference newReference) {
		
		TransactionReference reference;
		
		for (int i = 0; i < references.size(); i++) {
			
			reference = references.get(i);
			
			if (newReference.pow().compareTo(reference.pow()) == 0 &
					newReference.transactionID().compareTo(reference.transactionID()) == 0 &
					newReference.outputID().compareTo(reference.outputID()) == 0) return true;
		}
		return false;
	}
	
	/*
	 * Computes the total of fees for all finalized transactions.
	 */
	private int totalFees(ArrayList<Transaction> finalisedTransactions) {
		int fees = 0;
		for (int i = 0; i < finalisedTransactions.size(); i++)
			fees += finalisedTransactions.get(i).transactionFee();
		return fees;
	}
	
	/*
	 * Checks if the block's previous hash is the latest hash in the blockchain.
	 */
	private boolean isNewBlock(Block block) {
		String latestHeader = blockExplorer.getLastPoW();
		if (block.header().previousPoW().compareTo(latestHeader) == 0) return true;
		else return false;
	}
	
	/*
	 * Broadcasts message to all peers.
	 */
	private void broadcast(String msgType, String message) {
		for (int i = 0; i < toPeers.size(); i++) toPeers.get(i).println(msgType + msgSeparator + message); 
	}
	
}