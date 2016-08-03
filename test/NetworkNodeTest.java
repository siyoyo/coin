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
import util.BaseConverter;
import util.BlockExplorer;
import util.RSA512;

public class NetworkNodeTest {
	
	public static void main (String[] args) {
		
		try {
			
			BlockExplorer explorer = new BlockExplorer("dat/blockchain.xml");
			TransactionReference reference = new TransactionReference("00c34517ab38e4727c4ffb1412673ad460374dda84f84c6038e5a65011d3289f", "1", "1");
			
			RSAPublicKey referencePublicKey = explorer.recipientPublicKey(reference);
			String encodedPublicKeyInHex = BaseConverter.bytesDecToHex(referencePublicKey.getEncoded());
			
			Wallet wallet = new Wallet();
			RSAPrivateCrtKey privateKey = wallet.privateKey(encodedPublicKeyInHex);
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
			
			NetworkNode node1 = new NetworkNode();
			
			node1.updateMempool(tx1);
			node1.updateMempool(tx2);
			node1.mine();
			
			wallet.updateBalance(encodedPublicKeyInHex, output.amount());
			
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
