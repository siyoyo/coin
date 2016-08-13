package test;

import util.BlockExplorer;

public class CompareChains {

	public static void main(String[] args) {
	
		String blockchain = "dat/blockchain";
		String fextension = ".xml";
		
		String hostname = "_0.0.0.0_";
		String[] ports = {"5004", "5003"};
			
		for (String port1 : ports) {
			for (String port2: ports) {
				if (port1 != port2)
				System.out.println("Longest match between " + port1 + " and " + port2 + ": " + BlockExplorer.longestMatch(blockchain + hostname + port1 + fextension, blockchain + hostname + port2 + fextension));
				System.out.println("-----");
			}
		}
	}
}