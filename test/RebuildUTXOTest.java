package test;

import p2p.NetworkNode;
import util.Filename;

public class RebuildUTXOTest {
	
	public static void main (String[] args) {
		
		NetworkNode node1 = new NetworkNode();
		NetworkNode node2 = new NetworkNode();
		
		String directory = "dat/";
		String blockchain = "blockchain";
		String utxolist = "utxolist";
		String wallet = "wallet";
		String extension = ".xml";
		String hostname = "0.0.0.0";
		String port1 = "5003";
		String port2 = "5004";
		
		Filename blockchain1 = new Filename(directory, blockchain, extension, hostname, port1);
		Filename blockchain2 = new Filename(directory, blockchain, extension, hostname, port2);
		
		Filename utxolist1 = new Filename(directory, utxolist, extension, hostname, port1);
		Filename utxolist2 = new Filename(directory, utxolist, extension, hostname, port2);
		
		Filename wallet1 = new Filename(directory, wallet, extension, hostname, port1);
		Filename wallet2 = new Filename(directory, wallet, extension, hostname, port2);
		
		node1.initialiseExplorers(blockchain1, utxolist1, wallet1);
		node2.initialiseExplorers(blockchain2, utxolist2, wallet2);
		
		// Need to change access of BlockExplorer and UTXOExplorer in NetworkNode 
		// to public in order to run
//		node1.utxoExplorer.rebuildUTXOList(node1.blockExplorer);
//		node2.utxoExplorer.rebuildUTXOList(node2.blockExplorer);
		
	}

}
