package obj;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import org.w3c.dom.DOMException;

/**
 * A Block consists of a BlockHeader and an ArrayList of Transactions.
 */
public class Block {
	
	private BlockHeader header;
	private String pow;
	private ArrayList<Transaction> transactions;
	
	public String separator = " ";
	
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
	
	public boolean validate(BlockExplorer blockExplorer, UTXOExplorer utxoExplorer) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, DOMException, SignatureException {

		// Check header hash
		boolean validHash = this.header.checkHash(this.pow);
		if (!validHash) return false;
		
		// Check transactions against UTXO list and verify signatures
		boolean validTransaction;
		for (int i = 0; i < this.transactions.size(); i++) {
			Transaction transaction = transactions.get(i);
			System.out.println("Validating transaction " + (i + 1) + " of " + this.transactions.size() + " ...");
			validTransaction = transaction.validate(i, blockExplorer, utxoExplorer);
			if (!validTransaction) return false;
		}
		
		return true;
	}
	
	public String toString() {
		
		StringBuilder str = new StringBuilder();
		
		// Header
		str.append(header.previousPoW() + separator);
		str.append(pow + separator);
		str.append(header.merkleRoot() + separator);
		str.append(header.difficulty() + separator);
		str.append(header.nonce() + separator);
		str.append(transactions.size());
		str.append(" HEAD ");
		
		// Transactions
		for (Transaction transaction:transactions) str.append(transaction.toString());
		
		return str.toString();
	}
	
}