package obj;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * A Transaction is constructed from a list of inputs and outputs.  Inputs reference unspent outputs
 * from previous transactions.  Outputs specify the public keys of the transaction recipients.
 */
public class Transaction {
	
	private ArrayList<Input> inputs;
	private ArrayList<Output> outputs;
	private ArrayList<String> signatures;
	
	private Logger logger = Logger.getLogger(Transaction.class.getName());
	
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
	
	/**
	 * Adds a new input to the transaction
	 * @param input References an unspent output from a previous transaction
	 */
	public void addInput(Input input) {
		inputs.add(input);
		logger.info("Added input[" + inputs.indexOf(input) + "] value " + input.output().amount() + " from " + input.reference());
	}
	
	/**
	 * Removes an input from the transaction
	 * @param input An input from this transaction
	 */
	public void removeInput(Input input) {
		inputs.remove(input);
		logger.info("Removed input value " + input.output().amount() + " from " + input.reference());
	}
	
	/**
	 * Adds a new output to the transaction
	 * @param output Specifies the amount and the recipient's public key
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 */
	public void addOutput(Output output) throws NoSuchAlgorithmException, InvalidKeySpecException {
		outputs.add(output);
		logger.info("Added output[" + outputs.indexOf(output) + "] value " + output.amount() + " to " + output.recipientAddress());
	}
	
	/**
	 * Removes an output from the transaction
	 * @param output An output from this transaction
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 */
	public void removeOutput(Output output) throws NoSuchAlgorithmException, InvalidKeySpecException {
		outputs.remove(output);
		logger.info("Removed output value " + output.amount() + " to " + output.recipientAddress());
	}

	/**
	 * Finalizes the transaction by signing the inputs and outputs if the sum of input   
	 * values is at least equal to the sum of output values.  Displays the transaction fee 
	 * as the amount by which the value of inputs exceed the value of outputs.  
	 * @throws TransactionInputsLessThanOutputsException Sum of inputs values cannot be less than sum of output values
	 * @throws GeneralSecurityException 
	 */
	public void finalise() throws TransactionInputsLessThanOutputsException, GeneralSecurityException {
		
		if (inputsLargerThanOutputs()) {
			logger.info("Transaction fee: " + (sumInputs() - sumOutputs()));
			logger.info(this.toString());
			this.sign();
		} else {
			throw new TransactionInputsLessThanOutputsException("Transaction failed to finalise");
		}
		
	}
	
	public String toString() {
		
		StringBuilder str = new StringBuilder();
		
		for (int i = 0; i < inputs.size(); i++) {
			
			Input in = inputs.get(i);
			str.append("in" + i + "." + in.reference() + "." + in.output().amount() + " ");
			
		}
		
		for (int i = 0; i < outputs.size(); i++) {
			
			Output out = outputs.get(i);
			
			try {
				str.append("out" + i + "." + out.recipientAddress() + "." + out.amount() + " ");
			} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
				e.printStackTrace();
			}
			
		}
		
		return str.toString();
		
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
	private boolean inputsLargerThanOutputs() {
		return (sumInputs() >= sumOutputs()) ? true : false;
	}
	
	private int sumInputs() {
		
		int sumInputs = 0;
		for (int i = 0; i < inputs.size(); i++) sumInputs += inputs.get(i).output().amount();
		return sumInputs;
	}
	
	private int sumOutputs() {
		
		int sumOutputs = 0;
		for (int i = 0; i < outputs.size(); i++) sumOutputs += outputs.get(i).amount();
		return sumOutputs;
	}
	
	private void sign() throws GeneralSecurityException {
		
		signatures = new ArrayList<String>();
		
		for (int i = 0; i < inputs.size(); i++) {
			String signature = inputs.get(i).sign(outputs);
			signatures.add(signature);
		}
		
	}
	
}
