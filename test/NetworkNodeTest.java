package test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import obj.Input;
import obj.Output;
import obj.Transaction;
import obj.Transaction.TransactionInputsLessThanOutputsException;
import p2p.NetworkNode;
import obj.TransactionReference;
import obj.Wallet;
import util.BlockExplorer;
import util.RSA512;

public class NetworkNodeTest {
	
	public static void main (String[] args) {
		
		try {
			
			BlockExplorer explorer = new BlockExplorer("dat/blockchain.xml");
			TransactionReference reference = new TransactionReference("0027068ef179a0d259acd071e153fc1721d45078baa3cf8581cc20db2630203b", "1", "1");
			
			String address = explorer.recipientAddress(reference);
			
			Wallet wallet = new Wallet();
			RSAPrivateCrtKey privateKey = wallet.privateKey(address);
			Input input = new Input(reference, privateKey);
			
			RSAPrivateCrtKey wrongPrivateKey = (RSAPrivateCrtKey) RSA512.generateKeyPair().getPrivate();
			Input wrongInput = new Input(reference, wrongPrivateKey);
			
			KeyPair outputKeyPair = RSA512.generateKeyPair(); 
			RSAPublicKey publicKey = (RSAPublicKey) outputKeyPair.getPublic();
			Output output = new Output(publicKey, "27");
			
			Transaction tx1 = new Transaction();
			Transaction tx2 = new Transaction();
			tx1.addInput(input);
			tx2.addInput(wrongInput);
			tx1.addOutput(output);
			
			NetworkNode node = new NetworkNode();
			node.initialiseExplorers("dat/blockchain.xml", "dat/utxo.xml");
			node.updateMempool(tx1);
			node.updateMempool(tx2);
			node.mine();
			
			wallet.updateBalance(address, output.amount());
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (TransactionInputsLessThanOutputsException e) {
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		
	}

}
