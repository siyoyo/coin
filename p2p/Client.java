package p2p;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import obj.Block;
import obj.BlockExplorer;
import obj.BlockHeader;
import obj.Input;
import obj.Output;
import obj.Transaction;
import obj.TransactionReference;
import obj.UTXOExplorer;
import util.BaseConverter;

public class Client {
	
	public final static String BLOCKCHAIN = "dat/blockchain.xml";
	public final static String UTXO = "dat/utxo.xml";
	
	private static BlockExplorer blockExplorer;
	private static UTXOExplorer utxoExplorer;
	
	public static void main (String[] args) {
		
		try (Socket socket = new Socket(args[0], Integer.parseInt(args[1]));
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
				
			blockExplorer = new BlockExplorer(BLOCKCHAIN);
			utxoExplorer = new UTXOExplorer(UTXO);
			
			String line;
			
			while ((line = reader.readLine()) != null) {
				readBlock(line);
			}
			
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
	}

	private static void readBlock(String string) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		
		String[] sections = string.split(" HEAD ");
		String header = sections[0];
		String body = sections[1];
		
		sections = header.split(" ");
		
		String previousPoW = sections[0];
		String pow = sections[1];
		String merkleRoot = sections[2];
		String difficulty = sections[3];
		int nonce = Integer.parseInt(sections[4]);
		int numberOfTransactions = Integer.valueOf(sections[5]);
		
		BlockHeader blockHeader = new BlockHeader(previousPoW, merkleRoot);
		blockHeader.setDifficulty(difficulty);
		blockHeader.setNonce(nonce);
		
		sections = body.split(" TX ");
		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
		
		for (int i = 0; i < numberOfTransactions; i++) {
			
			Transaction transaction = new Transaction();
			
			String section = sections[i];
			String[] subsections = section.split("#");
			
			int numberOfInputs = Integer.parseInt(subsections[0]);
			int numberOfOutputs = Integer.parseInt(subsections[1]);
			
			// Inputs
			String tx = subsections[2];
			subsections = tx.split(" IN ");
			
			String inputs = subsections[0];
			String[] in = inputs.split(" ");
			
			for (int j = 0; j < numberOfInputs; j++) {
				String refPoW = in[j + 0];
				String refTxID = in[j + 1];
				String refOutputID = in[j + 2];
				TransactionReference reference = new TransactionReference(refPoW, refTxID, refOutputID);
				Input input = new Input(reference);
				transaction.addInput(input);
			}
			
			// Outputs
			String subsection = subsections[1];
			subsections = subsection.split(" OUT ");
			
			String outputs = subsections[0];
			String[] outs = outputs.split(" ");
			
			KeyFactory factory = KeyFactory.getInstance("RSA");
			
			for (int j = 0; j < numberOfOutputs; j++) {
				String key = outs[j + 0];
				byte[] encodedPublicKey = BaseConverter.stringHexToDec(key);
				X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
				RSAPublicKey publicKey = (RSAPublicKey) factory.generatePublic(publicKeySpec);
				String amount = outs[j + 1];
				Output output = new Output(publicKey, amount);
				transaction.addOutput(output);
			}
			
			// Signatures
			if (numberOfInputs > 0) {
				
				String signatures = subsections[1];
				String[] sigs = signatures.split(" ");
				transaction.initialiseSignatures(numberOfInputs);
				
				for (int j = 0; j < numberOfInputs; j++) transaction.signatures()[j] = BaseConverter.stringHexToDec(sigs[j]);
			}
			
			transactions.add(transaction);
		}
		
		Block reconstructedBlock = new Block(blockHeader, pow, transactions);
		System.out.println("Block " + reconstructedBlock.pow() + " received and reconstructed: "+ (reconstructedBlock.toString().compareTo(string) == 0));
		boolean validBlock = reconstructedBlock.validate(blockExplorer, utxoExplorer);
		System.out.println("Block " + reconstructedBlock.pow() + " validated: " + validBlock);
	}
	
}