package p2p;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Peer {
	
	private Socket socket;
	private String hostname;
	private int port;
	private BufferedReader reader;
	
	public Peer(Socket socket) throws IOException {
		this.socket = socket;
		hostname = socket.getInetAddress().getHostName();
		port = socket.getPort();
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}
	
	public Socket socket() {
		return socket;
	}
	
	public String hostname() {
		return hostname;
	}
	
	public int port() {
		return port;
	}

	public BufferedReader reader() {
		return reader;
	}
}
