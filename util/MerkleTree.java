package util;

import java.util.ArrayList;
import obj.Transaction;

/**
 * A utility which uses SHA-256 to generate the root of a Merkle tree given a list of transactions. <br>
 * <br>
 * <b>References</b>
 * <ul>
 * <li> <a href="http://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html"> 
 * Java Cryptography Architecture Standard Algorithm Name Documentation </a> </li>
 * </ul>
 */
public class MerkleTree {
	
	/**
	 * Returns the hash of the root of the Merkle tree built using the transactions provided.
	 * @param transactions array list of transactions
	 * @return root of the Merkle tree of transactions, including the mint transaction
	 */
	public static String getRoot(ArrayList<Transaction> transactions) {
		int numberOfLeaves = countLeaves(transactions.size());	
		return buildTree(transactions, numberOfLeaves);
	}
	
	/**
	 * Checks if the Merkle tree root corresponds to the transactions
	 * @param transactions
	 * @param root Merkle tree root
	 * @return true if the provided root matches the root from the reconstructed Merkle tree
	 * @throws MerkleTreeException
	 */
	public static boolean validateRoot(ArrayList<Transaction> transactions, String root) throws MerkleTreeException {
		String rebuiltRoot = getRoot(transactions);
		if (rebuiltRoot.compareTo(root) == 0) return true;
		else throw new MerkleTreeException("Root does not match transactions");
	}

	@SuppressWarnings("serial") // TODO
	public static class MerkleTreeException extends Exception {
		
		public MerkleTreeException() {
			super();
		}
		
		public MerkleTreeException(String msg) {
			super(msg);
		}
	}
	
	/* -----------------------------------------------------------------
	 * 							Private methods
	 * -----------------------------------------------------------------
	 */
	
	/*
	 * Returns the minimum number of leaves that is larger than the number of transactions,
	 * with the condition that the number of leaves must be a power of two.
	 */
	private static int countLeaves(int numberOfTransactions) {		
		int powerOfTwo = 0;
		while (Math.pow(2, powerOfTwo) < numberOfTransactions) powerOfTwo++;
		return (int) Math.pow(2, powerOfTwo);
	}
	
	/*
	 * Returns the root of the Merkle tree.  ***Note that the array list of hashes is 0-based.***
	 */
	private static String buildTree(ArrayList<Transaction> transactions, int numberOfLeaves) {
		
		int numberOfTransactions = transactions.size();
		String txAsString;
		
		SHA256 sha256 = new SHA256();
		ArrayList<String> hashes = new ArrayList<String>();
		String hash = null;
		
		// Set transaction ID
		for (int i = 0; i < numberOfTransactions; i++) transactions.get(i).setTxID(i + 1); 
		
		// Get hash of transaction string
		String[] txsAsString = getTransactionsAsStringArray(transactions);
		for (int j = 0; j < txsAsString.length; j ++) {
			txAsString = txsAsString[j];
			hash = sha256.hashString(txAsString); 
			hashes.add(hash);
		}
			
		// Add the last real transaction leaf to the tree until there are enough total leaves 
		for (int leaf = numberOfTransactions; leaf <= numberOfLeaves; leaf++) hashes.add(hash);
		
		while (hashes.size() > 1) hashes = fold(sha256, hashes);
		
		return hashes.get(0);
	}
	
	/*
	 * Ensures that the transactions are ordered according to their transaction IDs.
	 */
	private static String[] getTransactionsAsStringArray(ArrayList<Transaction> transactions) {
		
		int numberOfTransactions = transactions.size();
		String[] txArray = new String[numberOfTransactions];
		
		Transaction transaction;
		int transactionID;
		
		for (int i = 0; i < numberOfTransactions; i++) {
			transaction = transactions.get(i);
			transactionID = Integer.valueOf(transaction.txID());
			txArray[transactionID - 1] = transaction.toString();
		}
		
		return txArray;
	}
	
	/*
	 * Folds each level of the tree into half.
	 */
	private static ArrayList<String> fold(SHA256 sha256, ArrayList<String> hashes) {
		
		ArrayList<String> results = new ArrayList<String>();
		
		int i = 0;
		while (i < hashes.size()) {
			String concat = hashes.get(i) + hashes.get(i + 1);
			String result = sha256.hashString(concat);
			results.add(result);
			i = i + 2;
		}
		
		return results;
	}
	
}