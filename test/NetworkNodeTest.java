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

import obj.BlockExplorer;
import obj.Input;
import obj.NetworkNode;
import obj.Output;
import obj.Transaction;
import obj.Transaction.TransactionInputsLessThanOutputsException;
import obj.TransactionReference;
import obj.Wallet;
import util.BaseConverter;
import util.RSA512;

public class NetworkNodeTest {
	
	public static void main (String[] args) {
		
		try {
			
			BlockExplorer explorer = new BlockExplorer("dat/blockchain.xml");
			TransactionReference reference = new TransactionReference("00a74c1a4f478a7c3729e36a3bbb10ef398ebea5c509c3e7e0dd1b36ff49aac1", "1", "1");
			
			RSAPublicKey referencePublicKey = explorer.recipientPublicKey(reference);
			String encodedPublicKeyInHex = BaseConverter.bytesDecToHex(referencePublicKey.getEncoded());
			
			Wallet wallet = new Wallet();
			RSAPrivateCrtKey privateKey = wallet.privateKey(encodedPublicKeyInHex);
			Input input = new Input(reference, privateKey);
			
			RSAPrivateCrtKey wrongPrivateKey = (RSAPrivateCrtKey) RSA512.generateKeyPair().getPrivate();
			Input wrongInput = new Input(reference, wrongPrivateKey);
			
			KeyPair outputKeyPair = RSA512.generateKeyPair(); 
			RSAPublicKey publicKey = (RSAPublicKey) outputKeyPair.getPublic();
			Output output = new Output(publicKey, "5");
			
			Transaction tx = new Transaction();
			tx.addInput(input);
			tx.addInput(wrongInput);
			tx.addOutput(output);
			
			NetworkNode node = new NetworkNode();
			node.updateMempool(tx);
			node.mine();
			
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
