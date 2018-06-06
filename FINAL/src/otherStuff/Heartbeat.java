package otherStuff;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;

import otherStuff.*;

public class Heartbeat implements Runnable{
	
	public IPPort host;
	public IPPort dest;
	public DatagramSocket socket;
	
	public Heartbeat(IPPort host, IPPort dest){
		this.host = host;
		this.dest = dest;
		try {
			this.socket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run() {
		while (true){
			try {
				Util.sendObjectUDP(socket, dest, host);
				Thread.sleep(500);
			} catch (IOException e) {
				
			} catch (InterruptedException e) {
			
			}
		}
		
	}
	
	
}
