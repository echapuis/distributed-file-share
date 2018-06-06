package otherStuff;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


public class Pinger implements Runnable{
	
	public DatagramSocket source;
	public IPPort dest;
	public IPPort host;
	public boolean active;
	public int pingCount = 0;
	public int ackCount = 0;
	int i = 0;
	
	public Pinger(IPPort RM, IPPort dest){
		try {
			this.source = new DatagramSocket();
			this.source.setSoTimeout(2000); //if no reply within 2 sec, declare dead;
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.host = RM;
		this.dest = dest;
		this.active = true;
	}
	
	
	public void run(){
		ackThread.start();
		pingThread.start();
	}
	
	Thread ackThread = new Thread(new Runnable(){
		public void run(){
			while(true){
				byte[] buf = new byte[256];  //needs to be big enough to hold entire Serializable object
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				try {
					source.receive(packet);
				} catch (IOException e) {
					active = false;
					System.out.println("SERVER DOWN:\n" + dest.toString());
					try {
						Util.sendObjectUDP(source, host, dest);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					return;
				}
				pingCount = 0;		
			}
		}
	});
	
	
	Thread pingThread = new Thread(new Runnable(){
		public void run(){
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectOutputStream os = null;
			try {
				os = new ObjectOutputStream(outputStream);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			byte[] ping;
			while (true){
				if (pingCount > 20) return; //dest inactive for 2 sec it is officially dead
				try{
				os.writeObject("1");
				ping = outputStream.toByteArray();
				DatagramPacket send = new DatagramPacket(ping, ping.length,
						dest.getIP(), dest.getPort());
				source.send(send);
				os.flush();
				sleep();
				pingCount++;
				//System.out.println("sent ping: " + i);
				i++;
				}
				catch (Exception e){}
			}
		}});
	
	public void sleep(){
		try {
			Thread.sleep(100);
		}
		catch (Exception e){}
		
	}
	
	public boolean isActive(){
		return this.active && pingCount < 3; 
		// if the delay is too great will consider inactive; THIS DOES NOT MEAN ITS DEAD
	}
	
	
}
