package test;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.w3c.dom.DOMException;
import obj.Input;
import obj.Output;
import obj.Transaction;
import p2p.NetworkNode;
import obj.TransactionReference;
import util.BlockExplorer;
import util.RSA512;
import util.WalletExplorer;

public class NetworkNodeTest {
	
	public static void main (String[] args) {
		
		try {
			
			BlockExplorer explorer = new BlockExplorer("dat/blockchain.xml");
			TransactionReference reference = new TransactionReference("0027068ef179a0d259acd071e153fc1721d45078baa3cf8581cc20db2630203b", "1", "1");
			
			String address = explorer.outputAddress(reference);
			
			WalletExplorer wallet = new WalletExplorer("dat/wallet.xml");
			RSAPrivateKey privateKey = wallet.privateKey(address);
			Input input = new Input(reference, privateKey);
			
			RSAPrivateKey wrongPrivateKey = (RSAPrivateKey) RSA512.generateKeyPair().getPrivate();
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
			node.initialiseExplorers("dat/blockchain.xml", "dat/utxo.xml", "dat/wallet.xml");
			node.updateMempool(tx1);
			node.updateMempool(tx2);
			node.mine();
			
			int amount = Integer.valueOf(output.amount());
			
			wallet.updateBalance(address, -amount);
			
		} catch (DOMException e) {
			e.printStackTrace();
		}
		
	}

}
