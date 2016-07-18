package obj;

public class TransactionReference {
	
	private String pow;
	private String transactionID;
	private String outputID;

	public TransactionReference(String pow, String transactionID, String outputID) {
		this.pow = pow;
		this.transactionID = transactionID;
		this.outputID = outputID;
	}
	
	public String pow() {
		return pow;
	}
	
	public String transactionID() {
		return transactionID;
	}
	
	public String outputID() {
		return outputID;
	}
	
}