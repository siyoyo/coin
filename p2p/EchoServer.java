package p2p;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServer {
	
	public static void main (String[] args) {
		
		int port = 9999;
		
		System.out.println("Server is alive!");
		
		try (ServerSocket socket = new ServerSocket(port);
			Socket client = socket.accept();
			PrintWriter writer = new PrintWriter(client.getOutputStream(), true);
			BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
			
			System.out.println("Connection established!");
			
			String line;
			
			while((line = reader.readLine()) != null) {
				System.out.println("Roger" + line);
				writer.println(line);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	

}
