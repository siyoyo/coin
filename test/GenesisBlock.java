package test;

import p2p.NetworkNode;
import util.Filename;

public class GenesisBlock {
	
	public static void main (String[] args) {
		
		// previousPoW = afd18fcd591cb7ae4e984fb91f94363293f0e82b611512b77f51218f26f367ec
		NetworkNode node = new NetworkNode();
		
		String directory = "dat/";
		String extension = ".xml";
		
		Filename blockchainFile = new Filename(directory, "blockchain", extension);
		Filename utxoFile = new Filename(directory, "utxolist", extension);
		Filename walletFile = new Filename(directory, "wallet", extension);
		
		node.initialiseExplorers(blockchainFile, utxoFile, walletFile);
		
		node.mine();
	}
}