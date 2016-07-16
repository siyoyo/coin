package util;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
//import java.util.logging.Logger;

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
	
//	private Logger logger = Logger.getLogger(MerkleTree.class.getName());	// TODO
	private ArrayList<Transaction> orderedTransactions;
	
	/**
	 * TODO
	 * @param transactions
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public String getRoot(ArrayList<Transaction> transactions) throws NoSuchAlgorithmException {
		
		orderTransactions(transactions);
		orderedTransactions = transactions;
		int numberOfLeaves = countLeaves(transactions);	// Number of leaves must be a power of 2
		String root = buildTree(transactions, numberOfLeaves);
		return root;
	}

	public ArrayList<Transaction> orderedTransactions() {
		return orderedTransactions;
	}
	
	/*
	 * Private methods
	 */
	private void orderTransactions(ArrayList<Transaction> transactions) {
		
		Collections.sort(transactions, new Comparator<Transaction>() {

			@Override
			public int compare(Transaction tx1, Transaction tx2) {
				String str1 = tx1.toString();
				String str2 = tx2.toString();
				return str1.compareTo(str2);
			}
			
		});
	}
	
	private int countLeaves(ArrayList<Transaction> transactions) {		
		
		int powerOfTwo = 1;
		while (Math.pow(2, powerOfTwo) < transactions.size()) powerOfTwo++;
		
		return (int) Math.pow(2, powerOfTwo);
	}
	
	private String buildTree(ArrayList<Transaction> transactions, int numberOfLeaves) throws NoSuchAlgorithmException {
		
		SHA256 sha256 = new SHA256();
		
		ArrayList<String> hashes = new ArrayList<String>();
		String hash;
		int i;
		for (i = 0; i < transactions.size(); i++) {
			hash = sha256.hashString(transactions.get(i).toString()); 
			hashes.add(hash);
		}
		
		for (int j = i; j < numberOfLeaves; j++) {
			hash = sha256.hashString(transactions.get(i - 1).toString());
			hashes.add(hash);
		}
		
		while (hashes.size() > 1) {
			hashes = combine(hashes);
		}
		
		return hashes.get(0);
	}
	
	private ArrayList<String> combine(ArrayList<String> hashes) throws NoSuchAlgorithmException {
		
		SHA256 sha256 = new SHA256();
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