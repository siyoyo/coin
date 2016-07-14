package test;

import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import obj.NetworkNode;

public class TestRun {
	
	public static void main (String[] args) {
		
		NetworkNode node = new NetworkNode();
		try {
			node.mine();
			System.out.println(node.wallet().getBalances());
			node.mine();
			System.out.println(node.wallet().getBalances());
		} catch (NoSuchAlgorithmException | URISyntaxException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
	
	}

}
