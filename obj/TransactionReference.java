package obj;

public class TransactionReference {
	
	private String pow;
	private int transactionID;
	private int outputID;

	public TransactionReference(String pow, int transactionID, int outputID) {
		this.pow = pow;
		this.transactionID = transactionID;
		this.outputID = outputID;
	}
	
	public String pow() {
		return pow;
	}
	
	public int transactionID() {
		return transactionID;
	}
	
	public int outputID() {
		return outputID;
	}
	
}