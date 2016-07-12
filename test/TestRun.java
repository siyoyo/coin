package test;

import java.security.NoSuchAlgorithmException;

import obj.Node;

public class TestRun {
	
	public static void main (String[] args) {
		
//		Transaction tx1 = new Transaction();
//		tx1.addInput(new Input("1:3:4"));
//		tx1.addOutput(new Output("9873472", 10));
//		tx1.addInput(new Input("3:4:6"));
//		Input fault = new Input("4:2:1");
//		tx1.addInput(fault);
//		try {
//			tx1.finalise();
//			tx1.removeInput(fault);
//			tx1.finalise();
//		} catch (TransactionInputsLessThanOutputsException e) {
//			e.printStackTrace();
//		}
		
		Node node = new Node();
		try {
			node.mine();
			System.out.println(node.wallet().getBalances());
			node.mine();
			System.out.println(node.wallet().getBalances());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		
		
		
	
	}

}
