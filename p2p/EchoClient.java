package p2p;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * <b>References</b>
 * <ul>
 * <li><a href="https://support.apple.com/kb/PH18720?locale=en_US">
 * OS X Yosemite: Find your computer's name and address</a></li> 
 * </ul>
 */

public class EchoClient {
	
	public static void main (String[] args) throws SocketException {
		
		String host = "yinyee.local";
		int port = 9999;
		
		try (Socket socket = new Socket(host, port);
			PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {
			
			System.out.println("Client is alive!");
			
			String line;
			
			while((line = userInput.readLine()) != null) {
				writer.println(line);
				System.out.println(reader.readLine());
			}
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		
	}

}
