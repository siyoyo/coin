package test;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.w3c.dom.Node;

import obj.BlockExplorer;
import obj.NetworkNode;

public class TestRun {
	
	public static void main (String[] args) {
		
		try {
			URI domain = BlockExplorerTest.class.getClassLoader().getResource("dat/blockchain.xml").toURI();
			BlockExplorer explorer = new BlockExplorer(domain);
//			explorer.recipientAddress("000337a5cfd5c7db77a49eeaf39c544c72c828ff79a35828b3c58ed6a8d09465", 1, 1);
			explorer.transactionAmount("000337a5cfd5c7db77a49eeaf39c544c72c828ff79a35828b3c58ed6a8d09465", 1, 1);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
//		NetworkNode node = new NetworkNode();
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
