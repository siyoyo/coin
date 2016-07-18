package test;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import org.w3c.dom.Node;

import obj.BlockExplorer;
import obj.Input;
import obj.NetworkNode;
import obj.Output;
import obj.Transaction;
import obj.Transaction.TransactionInputsLessThanOutputsException;
import obj.TransactionReference;
import obj.Wallet;
import util.RSA512;
import util.Signature.ValidationFailureException;

public class TestRun {
	
	public static void main (String[] args) {
		
		try {
//			URI domain = BlockExplorerTest.class.getClassLoader().getResource("dat/blockchain.xml").toURI();
//			BlockExplorer explorer = new BlockExplorer(domain);
			TransactionReference reference = new TransactionReference("000337a5cfd5c7db77a49eeaf39c544c72c828ff79a35828b3c58ed6a8d09465", "1", "1");
			RSA512 rsa512 = new RSA512();
			KeyPair keyPair = rsa512.generateKeyPair();
			PublicKey publicKey = keyPair.getPublic();
//			Wallet wallet = new Wallet();
//			wallet.save(keyPair, 25);
			Output output = new Output(publicKey, 25);
			Input input = new Input(reference, keyPair.getPrivate()); // Should throw error if private key cannot verify public address
			Transaction tx = new Transaction();
			tx.addInput(input);
			tx.addOutput(output);
			tx.finalise();
			NetworkNode node = new NetworkNode();
			node.updateMempool(tx);
			node.mine();
			System.out.println(node.wallet().getBalances());
//			System.out.println(explorer.transactionAmount(reference));
//			System.out.println(explorer.recipientAddress(reference));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (TransactionInputsLessThanOutputsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ValidationFailureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

//		
//		try {
//			node.mine();
//			System.out.println(node.wallet().getBalances());
//			node.mine();
//			System.out.println(node.wallet().getBalances());
//		} catch (NoSuchAlgorithmException | URISyntaxException | InvalidKeySpecException e) {
//			e.printStackTrace();
//		}
	
	}

}
