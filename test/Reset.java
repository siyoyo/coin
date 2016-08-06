package test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Reset {

	public static void main(String[] args) {
		
		String fextension = ".xml";
		
		String blockchain = "dat/blockchain";
		String utxo = "dat/utxo";
		
		String hostname = "_0.0.0.0_";
		String[] ports = {"5002", "5003", "5004", "5006", "5007", "5008"};
		
		try {
			
			URI from = Reset.class.getClassLoader().getResource(blockchain + fextension).toURI();
			Path path = Paths.get(from);
			
			byte[] fileContents = Files.readAllBytes(path);
			
			URI to;
			for (String port : ports) {
				to = Reset.class.getClassLoader().getResource(blockchain + hostname + port + fextension).toURI();
				path = Paths.get(to);
				Files.write(path, fileContents);
			}
			
			from = Reset.class.getClassLoader().getResource(utxo + fextension).toURI();
			path = Paths.get(from);
			fileContents = Files.readAllBytes(path);
			
			for (String port : ports) {
				to = Reset.class.getClassLoader().getResource(utxo + hostname + port + fextension).toURI();
				path = Paths.get(to);
				Files.write(path, fileContents);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		

	}

}
