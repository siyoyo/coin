package p2p;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import obj.Block;

public class Peer {
	
	private Socket socket;
	private String hostname;
	private int port;
	private BufferedReader reader;
	private PrintWriter writer;
	private int currentHeight;
	private ArrayList<Block> storedBlocks;
	
	public Peer(Socket socket) throws IOException {
		this.socket = socket;
		hostname = socket.getInetAddress().getHostName();
		port = socket.getPort();
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writer = new PrintWriter(socket.getOutputStream(), true);
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
	
	public PrintWriter writer() {
		return writer;
	}
	
	public int currentHeight() {
		return currentHeight;
	}
	
	public void updateCurrentHeight(int height) {
		currentHeight = height;
	}
	
	public void initialiseStoredBlocks() {
		storedBlocks = new ArrayList<Block>();
	}
	
	public ArrayList<Block> storedBlocks() {
		return storedBlocks;
	}
	
}