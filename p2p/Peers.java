package p2p;

import java.net.Socket;
import java.util.ArrayList;

public class Peers {
	
	private ArrayList<Socket> peers;
	
	public Peers() {
		peers = new ArrayList<Socket>();
	}

	public void add(Socket peer) {
		peers.add(peer);
	}
	
	public ArrayList<Socket> peers() {
		return peers;
	}
}
