package serverStuff;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingDeque;

import otherStuff.*;

public class Server0{
	
	protected static DatagramSocket socket = null;
	protected static ServerSocket Server;
	protected static Socket newSock = null;
	protected static Socket streamSocket;
	public static ObjectOutputStream out;
	public static ObjectInputStream in;
	protected static IPPort host;
	
	public static int serverNumber;
	public static int farmNumber;
	public static LinkedList<IPPort> Managers = new LinkedList<IPPort>();
	public static LinkedList<IPPort> servers = new LinkedList<IPPort>();
	public static IPPort RM;
	public static String Folder;
	
	public static int farm = 0;
	
	public static void main(String[] args) throws Exception{
		socket = new DatagramSocket();
		Server = new ServerSocket(0,999, InetAddress.getLocalHost());
		host = new IPPort(socket,Server);
		initFolder();
		
		
		Managers = Util.getManagers();
		RM = Managers.get(farm);
		
		System.out.println("Datagram Socket: " + InetAddress.getLocalHost().getHostAddress() + ":" + socket.getLocalPort());
		System.out.println("Server:  " + Server.getInetAddress().getHostAddress() + ":" + Server.getLocalPort());
		
		Util.startHB(host, RM);
		
		listener.start();
		
		Thread sT = null;
		while (true) {
			try{
			Socket clientSocket = Server.accept();
			ServerThread s = new ServerThread(host, clientSocket, servers, Folder, RM);
			sT = new Thread(s);
			sT.start();	
			}
			catch(Exception e){
				//System.out.println("Server error");
				//e.printStackTrace();
			}
			
		}
	}
	
	public static void initFolder(){
		Folder = "files/Server " + host.getServerPort() + " farm " + farm;
		Util.createFolder(Folder);
	}
	
	public static void sleep(int secs){
		try {
			Thread.sleep(secs*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static Thread listener = new Thread(new Runnable(){
		public void run(){
			whileLoop:
			while (true){
				try {
					//System.out.println("servers: " + servers.toString());
					IPPort ip = (IPPort) Util.receiveObjectUDP(socket);
					if (!servers.contains(ip)) servers.add(ip);
					else servers.get(servers.indexOf(ip)).setActive(ip.isActive());
					//System.out.println("Updated servers: " + servers.toString());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	});
}
