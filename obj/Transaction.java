package obj;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import obj.Input.InputException;
import util.BaseConverter;
import util.BlockExplorer;
import util.Signature;
import util.UTXOExplorer;
import util.UTXOExplorer.UTXOException;

/**
 * A Transaction is constructed from a list of inputs and outputs.  Inputs reference unspent outputs
 * from previous transactions.  Outputs specify the public keys of the transaction recipients.
 */
public class Transaction {
	
	private ArrayList<Input> inputs;
	private ArrayList<Output> outputs;
	private byte[][] signatures;
	private int transactionFee;
	private int txID;
	
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
	
	public int transactionFee() {
		return transactionFee;
	}
	
	public String txID() {
		return String.valueOf(txID);
	}
	
	public void setTxID(int txID) {
		this.txID = txID;
	}
	
	/**
	 * Adds a new input to the transaction
	 * @param input References an unspent output from a previous transaction
	 */
	public void addInput(Input input) {
		inputs.add(input);
	}
	
	/**
	 * Removes an input from the transaction.  For use in gui/InputsOutputs class.
	 * @param inputAddress input address
	 * @param blockExplorer block explorer
	 */
	public void removeInput(String inputAddress, BlockExplorer blockExplorer) {
		
		Input input;
		for (int i = 0; i < inputs.size(); i++) {
			
			input = inputs.get(i);
			
			if (blockExplorer.outputAddress(input.reference()).compareTo(inputAddress) == 0) {
				inputs.remove(i);
				break;
			}
		}
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
	 * Removes an output from the transaction.  For use in the gui/InputsOutputs class.
	 * @param outputAddress output address 
	 */
	public void removeOutput(String outputAddress) {
		
		System.out.println("number of outputs" + outputs.size());
		
		Output output;	
		for (int i = 0; i < outputs.size(); i++) {
			
			output = outputs.get(i);
			
			if (output.outputAddress().compareTo(outputAddress) == 0) {
				outputs.remove(i);
				break;
			}
		}
		
		System.out.println("number of outputs" + outputs.size());
	}
	
	/**
	 * Returns a table of the inputs that has only one column:
	 * <ul>
	 * <li>input address</li>
	 * </ul>
	 * @param blockExplorer
	 * @return table of inputs
	 */
	public String[][] inputsAsTable(BlockExplorer blockExplorer) {
		
		String[][] inTable = new String[inputs.size()][1];
		Input input;
		
		for (int i = 0; i < inputs.size(); i++) {
			input = inputs.get(i);
			inTable[i][0] = blockExplorer.outputAddress(input.reference());
		}
		
		return inTable;
	}
	
	/**
	 * Returns a table of the outputs that has two columns:
	 * <ul>
	 * <li>amount</li>
	 * <li>output address</li>
	 * </ul>
	 * @return table of outputs
	 */
	public String[][] outputsAsTable() {
		
		String[][] outTable = new String[outputs.size()][2];
		Output output;
		
		for (int i = 0; i < outputs.size(); i++) {
			output = outputs.get(i);
			outTable[i][0] = output.amount();
			outTable[i][1] = output.outputAddress();
		}
		
		return outTable;
	}
	
	/**
	 * Returns all the outputs as an array of bytes
	 * @return outputs as an array of bytes
	 */
	public byte[] getOutputsInBytes() {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < outputs.size(); i++) str.append(outputs.get(i).toString() + "\t");	
		return str.toString().getBytes();
	}
	
	/**
	 * Initializes the array of signatures as a 1-based array i.e. index 0 
	 * is always set to null.
	 * @param size size of the signature array
	 */
	public void initialiseSignatures(int size) {
		signatures = new byte[size][];
		signatures[0] = null;
	}
	
	/**
	 * Calculates the transaction fee.
	 * @param blockExplorer block explorer
	 */
	public int calculateTransactionFee(BlockExplorer blockExplorer) throws TransactionException {
		
		TransactionReference reference;
		Output output;
		int sumInputs = 0, sumOutputs = 0, transactionFee;
		
		for (int i = 0; i < inputs.size(); i++) {
			reference = inputs.get(i).reference();
			sumInputs += Integer.valueOf(blockExplorer.transactionAmount(reference));
		}
		
		for (int j = 0 ; j < outputs.size(); j++) {
			output = outputs.get(j);
			sumOutputs += Integer.valueOf(output.amount());
		}
		
		transactionFee = sumInputs - sumOutputs;
		
		if (transactionFee < 0) throw new TransactionException("Negative transaction fee");
		else return transactionFee;
	}

	/**
	 * Finalizes the transaction by signing the outputs if the sum of input values is at    
	 * least equal to the sum of output values.  Displays the transaction fee as the  
	 * amount by which the value of inputs exceed the value of outputs.
	 * <p>Ordering of inputs and outputs is according to the order in which they were added
	 * to their respective array lists.
	 * <p>Note that the array list of inputs, the array list of outputs and the array of
	 * signatures are <b>one unit larger</b> than the number of actual inputs, output and signatures.
	 * This is because IDs are 1-based and to ensure that input and signature indices are aligned.
	 * @param explorer block explorer
	 * @param txID transaction ID
	 * @return 2D byte array of signatures
	 * @throws TransactionException 
	 */
	public void finalise(BlockExplorer blockExplorer, UTXOExplorer utxoExplorer) throws TransactionException {
			
		// Calculate transaction fee
		transactionFee = calculateTransactionFee(blockExplorer);
		
		// Set output ID
		for (int i = 0; i < outputs.size(); i++) outputs.get(i).setOutputID(i + 1);
		byte[] outputsInBytes = getOutputsInBytes();
		
		/* Array of signatures is one larger than the number of inputs
		 * so that the signature index is the same as the input index.
		 * Index 0 of the signature array is null.
		 */ 
		initialiseSignatures(inputs.size() + 1);
		signatures[0] = null;
		
		Input input;
		boolean validUTXO = false;
		
		for (int i = 0; i < inputs.size(); i++) {
			input = inputs.get(i);
			
			// Check against UTXO list
			try {
				validUTXO = utxoExplorer.valid(input);
			} catch (UTXOException e) {
				e.printStackTrace();
			}
			 
			if (validUTXO) {
				// Set input ID
				input.setInputID(i + 1);
				
				// Sign outputs
				try {
					signatures[i + 1] = input.sign(outputsInBytes);
				} catch (InputException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Checks the UTXO file to see if every input to the transaction is unspent and verifies that
	 * every signature is valid.  The checks begin at index = 1.
	 * @param blockExplorer block explorer
	 * @param utxoExplorer UTXO explorer
	 * @return true if every input is unspent and every signature verifies; false otherwise
	 * @throws TransactionException 
	 */
	public boolean validate(BlockExplorer blockExplorer, UTXOExplorer utxoExplorer) throws TransactionException {
		
		if (inputs.size() == 0) {
			System.out.println("No inputs to this transaction");
			return true;
		}
		
		Signature signature = new Signature();
		RSAPublicKey publicKey;
		byte[] outputsInBytes = getOutputsInBytes();
		
		Input input;
		int inputID;
		for (int i = 0; i < inputs.size(); i++) {
			
			input = inputs.get(i);
			inputID = Integer.valueOf(input.inputID());
			
			// Check if valid UTXO
			try {
				utxoExplorer.valid(input);
			} catch (UTXOException e) {
				throw new TransactionException(e.getMessage());
			}
			
			// Check signature
			publicKey = blockExplorer.publicKey(input.reference());
			try {
				signature.verify(outputsInBytes, signatures[inputID], publicKey);
			} catch (SignatureException | InvalidKeyException e) {
				throw new TransactionException("Signature failed validation");
			}
		}
		return true;
	}
	
	/**
	 * <b>This method should only be called after the transaction has been finalized.</b>
	 * <p>Returns a string representation of the transaction.  Used to send transactions
	 * from one network node to another hence the receiving network node must know how
	 * to decode.
	 */
	public String toString() {
		
		StringBuilder str = new StringBuilder();
		
		str.append(txID + "#");
		str.append(inputs.size() + "#");
		str.append(outputs.size() + "#");
		
		// Inputs
		Input input;
		for (int i = 0; i < inputs.size(); i++) {
			input = inputs.get(i);
			str.append(input.inputID() + separator);
			str.append(input.reference().pow() + separator);
			str.append(input.reference().transactionID() + separator);
			str.append(input.reference().outputID() + "IN");
		}
		str.append("END");
		
		// Outputs
		Output output;
		for (int i = 0; i < outputs.size(); i++) {
			output = outputs.get(i);
			str.append(output.outputID() + separator);
			str.append(BaseConverter.bytesDecToHex(output.publicKey().getEncoded()) + separator);
			str.append(output.amount() + "OUT");
		}
		str.append("END");
		
		// Signatures
		if (inputs.size() > 0)
			for (int i = 0; i < inputs.size(); i++)
				str.append(BaseConverter.bytesDecToHex(signatures[i + 1]) + "SIG"); 
		
		str.append("END");
		str.append("TX");
		
		return str.toString();
	}
	
	@SuppressWarnings("serial") // TODO Serialize
	public class TransactionException extends Exception {
		
		public TransactionException() {
			super();
		}
		
		public TransactionException(String msg) {
			super(msg);
		}
	}
			
}