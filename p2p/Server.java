package p2p;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
	public static int port = 9999;
	
	public Server() {
		
		Peers peers = new Peers();
		
		try (ServerSocket server = new ServerSocket(port)) {
			
			while (true) {
				Socket peer = server.accept();
				System.out.println("Connection established with " + peer.getInetAddress().getHostName());
				peers.add(peer);
			}
			
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}