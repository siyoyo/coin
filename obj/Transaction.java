package obj;

import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
//import java.util.logging.Logger;

/**
 * A Transaction is constructed from a list of inputs and outputs.  Inputs reference unspent outputs
 * from previous transactions.  Outputs specify the public keys of the transaction recipients.
 */
public class Transaction {
	
	private ArrayList<Input> inputs;
	private ArrayList<Output> outputs;
	private byte[][] signatures;
	private String transactionFee;
	
//	private Logger logger = Logger.getLogger(Transaction.class.getName());
	
	public Transaction() {
		inputs = new ArrayList<Input>();
		outputs = new ArrayList<Output>();
	}
	
	public ArrayList<Input> inputs() {
		return inputs;
	}
	
	public ArrayList<Output> outputs() {
		return outputs;
	}
	
	public byte[][] signatures() {
		return signatures;
	}
	
	public String transactionFee() {
		return transactionFee;
	}
	
	/**
	 * Adds a new input to the transaction
	 * @param input References an unspent output from a previous transaction
	 */
	public void addInput(Input input) {
		inputs.add(input);
	}
	
	/**
	 * Removes an input from the transaction
	 * @param input An input from this transaction
	 */
	public void removeInput(Input input) {
		inputs.remove(input);
	}
	
	/**
	 * Adds a new output to the transaction
	 * @param output Specifies the amount and the recipient's public key
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 */
	public void addOutput(Output output) {
		outputs.add(output);
	}
	
	/**
	 * Removes an output from the transaction
	 * @param output An output from this transaction
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 */
	public void removeOutput(Output output) {
		outputs.remove(output);
	}

	/**
	 * Finalizes the transaction by signing the inputs and outputs if the sum of input   
	 * values is at least equal to the sum of output values.  Displays the transaction fee 
	 * as the amount by which the value of inputs exceed the value of outputs.  
	 * @throws TransactionInputsLessThanOutputsException Sum of inputs values cannot be less than sum of output values
	 * @throws SignatureException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws GeneralSecurityException 
	 * @throws URISyntaxException 
	 */
	public byte[][] finalise(BlockExplorer explorer) throws TransactionInputsLessThanOutputsException, InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		
		int txFee = sumInputs(explorer) - sumOutputs();
		transactionFee = String.valueOf(txFee);
		
		if (txFee >= 0) {
		
			byte[] outputsInBytes = getOutputsInBytes();
			signatures = new byte[inputs.size()][];
			
			for (int i = 0; i < inputs.size(); i++) signatures[i] = inputs.get(i).sign(outputsInBytes); 
			
			return signatures;
			
		} else throw new TransactionInputsLessThanOutputsException("Transaction failed to finalise");
			
	}
	
	public byte[] getOutputsInBytes() {
		
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < outputs.size(); i++) str.append(outputs.get(i).toString() + "\n");
		
		return str.toString().getBytes();
	}

	@SuppressWarnings("serial") // TODO Serialize
	public class TransactionInputsLessThanOutputsException extends Exception {
		
		public TransactionInputsLessThanOutputsException() {
			super();
		}
		
		public TransactionInputsLessThanOutputsException(String msg) {
			super(msg);
		}
		
	}
		
	/*
	 * Private methods
	 */
	private int sumInputs(BlockExplorer explorer) {
		
		int sumInputs = 0;
		
		for (int i = 0; i < inputs.size(); i++) {
			TransactionReference reference = inputs.get(i).reference();
			sumInputs += Integer.parseInt(explorer.transactionAmount(reference));
		}
		
		return sumInputs;
	}
	
	private int sumOutputs() {
		
		int sumOutputs = 0;
		
		for (int i = 0; i < outputs.size(); i++) sumOutputs += Integer.parseInt(outputs.get(i).amount());
		
		return sumOutputs;
	}
	
}