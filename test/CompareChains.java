package test;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import util.BlockExplorer;

public class CompareChains {

	public static void main(String[] args) {
	
		String blockchain = "dat/blockchain";
		String fextension = ".xml";
		
		String hostname = "_0.0.0.0_";
		String[] ports = {"5002", "5003", "5004", "5006"};
		
		try {
			for (String port1 : ports) {
				for (String port2: ports) {
					if (port1 != port2)
					System.out.println("Longest match between " + port1 + " and " + port2 + ": " + BlockExplorer.longestMatch(blockchain + hostname + port1 + fextension, blockchain + hostname + port2 + fextension));
					System.out.println("-----");
				}
			}

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		

	}

}
