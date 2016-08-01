package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class URLTest {
	
	public static void main (String[] args) {
		
		try {
			
			URL oracle = new URL("https://www.airbnb.com");
			BufferedReader reader = new BufferedReader(new InputStreamReader(oracle.openStream()));
//			System.out.println(reader.readLine());
			String line;
			while((line = reader.readLine()) != null) System.out.println(line);
			
			reader.close();
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

}
