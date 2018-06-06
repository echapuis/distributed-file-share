package otherStuff;
import java.io.Serializable;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;


public class IPPort implements Serializable, Comparable{
	
	public InetAddress IP;
	public int Port;
	public InetAddress serverIP = null;
	public int serverPort;
	public boolean isActive = true;
	
	public IPPort (InetAddress ip, int port){
		this.IP = ip;
		this.Port = port;
	}
	
	public IPPort (InetAddress ip, int port, InetAddress serverIP, int serverPort){
		this.IP = ip;
		this.Port = port;
		this.serverIP =serverIP;
		this.serverPort = serverPort;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((IP == null) ? 0 : IP.hashCode());
		result = prime * result + Port;
		result = prime * result
				+ ((serverIP == null) ? 0 : serverIP.hashCode());
		result = prime * result + serverPort;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IPPort other = (IPPort) obj;
		if (IP == null) {
			if (other.IP != null)
				return false;
		} else if (!IP.equals(other.IP))
			return false;
		if (Port != other.Port)
			return false;
		if (serverIP == null) {
			if (other.serverIP != null)
				return false;
		} else if (!serverIP.equals(other.serverIP))
			return false;
		if (serverPort != other.serverPort)
			return false;
		return true;
	}

	public IPPort(DatagramSocket ds, ServerSocket s){
		try {
			this.IP = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.Port = ds.getLocalPort();
		this.serverIP = s.getInetAddress();
		this.serverPort = s.getLocalPort();
	}
	
	public InetAddress getIP(){
		return this.IP;
	}
	
	public InetAddress getServerIP(){
		return this.serverIP;
	}
	
	public String getIPStr(){
		return "[" + this.IP.getHostAddress() + ":" + this.Port + "]";
	}
	
	public int getPort(){
		return this.Port;
	}
	
	public int getServerPort(){
		return this.serverPort;
	}
	
	public boolean isActive(){
		return this.isActive;
	}
	
	public void setActive(boolean s){
		this.isActive = s;
	}
	
	
	public String toString(){
		String s = "[" + this.IP.getHostAddress() + ":" + this.Port + "] " + this.isActive;
//		if (this.serverIP != null) s += "\n" + "Server: [" + this.serverIP.getHostAddress() + ":" + this.serverPort + "]";
		return s;
	}
	
	public String sendString(){
		return this.IP.getHostAddress() + ":" + this.Port;
	}

	public int compareTo(Object o) {
		IPPort ip = (IPPort) o;
		return this.Port > ip.getPort() ? -1 : 0;
	}
}
