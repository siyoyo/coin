package obj;

import java.util.ArrayList;

/**
 * A Block consists of a BlockHeader and an ArrayList of Transactions.
 */
public class Block {
	
	public final static String BLOCKCHAIN = "dat/blockchain.xml";
	
	private BlockHeader header;
	private String pow;
	private ArrayList<Transaction> transactions;
	
	public Block(BlockHeader header, String pow, ArrayList<Transaction> transactions) {
		this.header = header;
		this.pow = pow;
		this.transactions = transactions;
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
}