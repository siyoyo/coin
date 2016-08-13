package obj;

import util.SHA256;

/**
 * A BlockHeader consists of the hash of previous block's header, the root of the 
 * Merkle tree containing the transactions, and the nonce value that results in the
 * hash value of the concatenation of the three elements of the BlockHeader falling 
 * below the proof-of-work difficulty threshold.
 */
public class BlockHeader {
	
	private String previousPoW;
	private String merkleRoot;
	private String difficulty;
	private int nonce;

	public BlockHeader(String previousPoW, String merkleRoot) {		
		this.previousPoW = previousPoW;
		this.merkleRoot = merkleRoot;
	}
	
	/**
	 * Tries nonce values in increasing order from zero until it obtains a hash value of the concatenation
	 * of the previous block header, the root of the Merkle tree containing the transactions, and the nonce,
	 * which is smaller than the required proof-of-work difficulty.
	 * @return String of 64 hex digits that represents the successful hash of the header
	 */
	public String hash(String difficulty) {
		
		this.difficulty = difficulty; 
		String result = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
		nonce = -1;
		
		while (result.compareTo(this.difficulty) > 0) result = tryNonce(++nonce);
		
		System.out.println(nonce + "\t" + result);
		return result;
	}
	
	public boolean checkHash(String pow) throws BlockHeaderException {
		String result = tryNonce(nonce);
		if (result.compareTo(pow) == 0) return true;
		else throw new BlockHeaderException("Invalid proof-of-work");
	}
	
	public String previousPoW() {
		return previousPoW;
	}
	
	public int nonce() {
		return nonce;
	}
	
	public void setNonce(int nonce) {
		this.nonce = nonce;
	}
	
	public String merkleRoot() {
		return merkleRoot;
	}
	
	public String difficulty() {
		return difficulty;
	}
	
	public void setDifficulty(String difficulty) {
		this.difficulty = difficulty;
	}
	
	@SuppressWarnings("serial") // TODO
	public class BlockHeaderException extends Exception {
		
		public BlockHeaderException() {
			super();
		}
		
		public BlockHeaderException(String msg) {
			super(msg);
		}
	}
	
	/* -----------------------------------------------------------------
	 * 							Private methods
	 * -----------------------------------------------------------------
	 */
	
	private String tryNonce(int nonce) {
		SHA256 sha256 = new SHA256();
		String hash = sha256.hashString(previousPoW + merkleRoot + String.valueOf(nonce));
		return hash;
	}

}