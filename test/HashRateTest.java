package test;

import java.io.IOException;
import java.time.LocalDateTime;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import p2p.NetworkNode;
import util.Filename;

public class HashRateTest {
	
	public static void main (String[] args) throws SAXException, IOException, ParserConfigurationException {
		
		NetworkNode node = new NetworkNode();
		
		String directory = "dat/";
		String extension = ".xml";
		
		Filename blockchainFilename = new Filename(directory, "blockchain", extension);
		Filename utxoFilename = new Filename(directory, "utxolist", extension);
		Filename walletFilename = new Filename(directory, "wallet", extension);
		
		node.initialiseExplorers(blockchainFilename, utxoFilename, walletFilename);
		System.out.println(LocalDateTime.now() + " " + node.difficulty);
		
		for (int i = 0; i < 10; i++) node.mine();
		
	}

}
