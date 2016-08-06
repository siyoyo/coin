package obj;

import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import org.w3c.dom.DOMException;

import util.BaseConverter;
import util.BlockExplorer;
//import java.util.logging.Logger;
import util.Signature;
import util.UTXOExplorer;

/**
 * A Transaction is constructed from a list of inputs and outputs.  Inputs reference unspent outputs
 * from previous transactions.  Outputs specify the public keys of the transaction recipients.
 */
public class Transaction {
	
	private ArrayList<Input> inputs;
	private ArrayList<Output> outputs;
	private byte[][] signatures;
	private String transactionFee;
	
	private String separator = " ";
	
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
	
	public void initialiseSignatures(int size) {
		signatures = new byte[size][];
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
			initialiseSignatures(inputs.size());
			
			for (int i = 0; i < inputs.size(); i++) signatures[i] = inputs.get(i).sign(outputsInBytes); 
			
			return signatures;
			
		} else throw new TransactionInputsLessThanOutputsException("Transaction failed to finalise");
			
	}
	
	public boolean validate(int transactionID, BlockExplorer blockExplorer, UTXOExplorer utxoExplorer) throws NoSuchAlgorithmException, InvalidKeySpecException, DOMException, InvalidKeyException, SignatureException {
		
		byte[] outputsInBytes = this.getOutputsInBytes();
		
		if (this.inputs.size() == 0) {
			System.out.println("No inputs to this transaction");
		}
		
		// For each input
		for (int j = 0; j < this.inputs.size(); j++) {
			
			// Check UTXO list
			Input input = this.inputs.get(j);
			boolean validUTXO = utxoExplorer.valid(input);
			System.out.println("Input " + (transactionID + 1) + "." + j + " is found in the utxo list: " + validUTXO);
			if (!validUTXO) return false;
			
			// Check signature
			TransactionReference reference = this.inputs.get(j).reference();
			RSAPublicKey publicKey = blockExplorer.recipientPublicKey(reference);
			
			Signature signature = new Signature();
			boolean validSignature = signature.verify(outputsInBytes, this.signatures[j], publicKey); 
			System.out.println("Input " + (transactionID + 1) + "." + j + " signature is verified: " + validSignature);
			if (!validSignature) return false;
		}
		
		return true;
	}
	
	public byte[] getOutputsInBytes() {
		
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < outputs.size(); i++) str.append(outputs.get(i).toString() + "\t");	
		return str.toString().getBytes();
	}
	
	public String toString() {
		
		StringBuilder str = new StringBuilder();
		
		str.append(inputs.size() + "#");
		str.append(outputs.size() + "#");
			
		for (Input input:inputs) {
			str.append(input.reference().pow() + separator);
			str.append(input.reference().transactionID() + separator);
			str.append(input.reference().outputID() + separator);
		}
		str.append(" IN ");
		
		for (Output output:outputs) {
			str.append(BaseConverter.bytesDecToHex(output.recipientPublicKey().getEncoded()) + separator);
			str.append(output.amount() + separator);
		}
		str.append(" OUT ");
		
		if (inputs.size() > 0) {
			for (int i = 0; i < signatures.length; i++) str.append(BaseConverter.bytesDecToHex(signatures[i]) + separator); 
		}
		str.append(" TX ");
		
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