package test;

import p2p.NetworkNode;

public class GenesisBlock {
	
	public static void main (String[] args) {
		
		// previousPoW = b1769976a749b969f3dd57ac31b302805a84665848600e57c756b1fad44a12d7
		NetworkNode node = new NetworkNode();
		node.initialiseExplorers("dat/blockchain.xml", "dat/utxo.xml");
		node.mine();
	}
}