package obj;

import java.util.ArrayList;

import obj.BlockHeader.BlockHeaderException;
import obj.Transaction.TransactionException;
import util.BlockExplorer;
import util.MerkleTree;
import util.MerkleTree.MerkleTreeException;
import util.UTXOExplorer;

/**
 * A Block consists of a BlockHeader and an ArrayList of Transactions.
 */
public class Block {
	
	private int height;
	private BlockHeader header;
	private String pow;
	private ArrayList<Transaction> transactions;
	
	public String separator = " ";
	
	public Block(BlockHeader header, String pow, ArrayList<Transaction> transactions) {
		this.header = header;
		this.pow = pow;
		this.transactions = transactions;
	}
	
	public String height() {
		return String.valueOf(height);
	}
	
	public void setHeight(int height) {
		this.height = height;
	}
	
	public BlockHeader header() {
		return header;
	}
	
	public String pow() {
		return pow;
	}
	
	public ArrayList<Transaction> transactions() {
		return transactions;
	}
	
	/**
	 * Returns true if the proof-of-work is valid AND if all the transactions are valid AND if the root
	 * of the Merkle tree is valid.
	 * @param blockExplorer block explorer
	 * @param utxoExplorer UTXO explorer
	 * @return true if the proof-of-work and every transaction and the Merkle root are all valid
	 * @throws BlockHeaderException
	 * @throws MerkleTreeException
	 * @throws TransactionException
	 */
	public boolean validate(BlockExplorer blockExplorer, UTXOExplorer utxoExplorer) throws BlockHeaderException, MerkleTreeException, TransactionException {

		// Check header hash
		boolean validPoW = header.checkHash(pow);
		if (!validPoW) return false;

		// Validate transactions
		int numberOfTransactions = transactions.size();
		Transaction tx;
		String txID;
		boolean validTransaction = true;
		
		for (int i = 0; i < numberOfTransactions; i++) {
			
			tx = transactions.get(i);
			txID = tx.txID();
			
			System.out.println("Validating transaction " + txID + " of " + numberOfTransactions + " ...");
			validTransaction = tx.validate(blockExplorer, utxoExplorer);
			if (!validTransaction) return false;
		}
		
		// Check Merkle root
		boolean validRoot = MerkleTree.validateRoot(transactions, header.merkleRoot());
		if (!validRoot) return false;
		
		if (validPoW & validTransaction & validRoot) return true;
		return false;
	}
	
	/**
	 * <b>This method should only be called after the block has been successfully mined
	 * otherwise there will be missing elements.</b>
	 * <p>Returns a string representation of the block.  Used to send blocks from one
	 * network node to another hence the receiving network node must know how to decode.
	 */
	public String toString() {
		
		StringBuilder str = new StringBuilder();
		
		// Height
		str.append(height + separator);
		
		// Header
		str.append(header.previousPoW() + separator);
		str.append(pow + separator);
		str.append(header.merkleRoot() + separator);
		str.append(header.difficulty() + separator);
		str.append(header.nonce());
		str.append("HEAD");
		
		// Transactions
		int numberOfTransactions = transactions.size();
		str.append(numberOfTransactions);
		str.append("NUM");
		
		for (int i = 0; i < numberOfTransactions; i++) str.append(transactions.get(i).toString());
		
		return str.toString();
	}
	
	@SuppressWarnings("serial") // TODO
	public class BlockException extends Exception {
		
		public BlockException() {
			super();
		}
		
		public BlockException(String msg) {
			super(msg);
		}
	}
	
}