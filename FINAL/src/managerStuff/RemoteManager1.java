package managerStuff;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

import otherStuff.*;

public class RemoteManager1{
	
	protected static DatagramSocket socket = null;
	protected static ServerSocket Server = null;
	protected static Socket newSock = null;
	protected BufferedReader in = null;
	protected static LinkedList<IPPort> IPs;
	protected static LinkedList<Integer> IPPingCount;
	
	protected static LinkedList<IPPort> RMs;
	protected static LinkedList<IPPort> Servers;
	
	protected static LinkedList<String> files = new LinkedList<String>();
	protected static LinkedList<Integer> versions = new LinkedList<Integer>();
	
	protected static IPPort s;
	protected static IPPort host;
	protected static String folder;
	
	static int farm = 1;
	
	public static void main(String[] args) throws Exception{
		socket = new DatagramSocket(5000+farm);
		Server = new ServerSocket(6000+farm,999, InetAddress.getLocalHost());
		host = new IPPort(socket,Server);
		s = host;
		
		IPs = new LinkedList<IPPort>();
		IPPingCount = new LinkedList<Integer>();
		RMs = Util.getManagers();
		Servers = new LinkedList<IPPort>();
		
		initFolder();
		
		
		System.out.println(host.toString());
		
		for (int i = 0; i < RMs.size();i++){
			if (!RMs.get(i).equals(host)) {
				Util.startHB(host, RMs.get(i));
				IPs.add(RMs.get(i));
				IPPingCount.add(0);
			}
		}
		
		IPHandler.start();
		Thread sT = null;
		
		while (true) {
			try{
			//System.out.println("waiting for new request");
			Socket clientSocket = Server.accept();
			
			ObjectOutputStream out =new ObjectOutputStream(clientSocket.getOutputStream());  
			ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
			Request r = (Request) in.readObject();
			//System.out.println("Request: " + r.toString());
			
			if (r.getType().equalsIgnoreCase("r")) {
				if (files.contains(r.getFileName())) r.setVersion(versions.get(files.indexOf(r.getFileName())));
				//System.out.println("RMs: " + RMs.toString());
				//System.out.println("IPs: " + IPs.toString());
				ObjectOutputStream newOS = out;
				ObjectInputStream newIS = in;
				ManagerThread m = new ManagerThread(newIS, newOS, r, getRM(), folder, host);
				m.run();
				continue;
			}
			
			if (files.contains(r.getFileName()) && r.getVersion() != 0){
					continue;
				}
			
			
			for (int i = 0; i < Servers.size(); i++) {
				Util.sendObjectUDP(socket, Servers.getLast(), Servers.get(i));
			}
			try { Thread.sleep(100);} catch (Exception e){}
			
			System.out.println("Received 'new' Request: " + r.getType() + " " + r.getFileName());
			if (r.getType().equalsIgnoreCase("u")) updateFile(r);
			out.writeObject(Servers.getLast());
			out.flush();
			in.close();
			out.close();
			
			}
			catch (Exception e){
				System.out.println("RM error");
				e.printStackTrace();
			}
		}
	}
	
	public static void initFolder(){
		folder = "files/RM " + farm;
		Util.createFolder(folder);
	}
	
	public static void updateFile(Request r){
		String fileStr = "files/RM " + farm + "/files";
		//Util.createFile(fileStr, true);
		if (files.contains(r.getFileName())) {
			int index = files.indexOf(r.getFileName());
			int v = r.getVersion() == 0 ? versions.get(index) + 1 : r.getVersion();
			versions.set(index, v);
		}
		else {
			files.add(r.getFileName());
			versions.add(1);
		}
		
//		WriteFile vFile = new WriteFile(fileStr,true);
//		for (int i = 0; i < files.size(); i++){
//			try {
//				vFile.writeToFile(files.get(i) + "\t\tv: " + versions.get(i));
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}
	
	static Thread IPHandler = new Thread(new Runnable(){
		public void run(){
			whileLoop:
			while (true){
				try {
					IPPort ip = (IPPort) Util.receiveObjectUDP(socket);
					if (!IPs.contains(ip)) {
						IPs.add(ip);
						IPPingCount.add(1);
						if (!RMs.contains(ip)){
							Servers.add(ip);
							System.out.println("Added Server: " + ip.toString());				
						}
					}
					else {
						int index = IPs.indexOf(ip);
						int setTo = IPPingCount.get(index)+1;
						if (setTo>=10){
							for (int x = 0; x < IPPingCount.size();x++){
								if (IPPingCount.get(x) < 5) {
									IPPingCount.set(x,-1);
									IPs.get(x).setActive(false);
									if (RMs.contains(IPs.get(x))) RMs.get(RMs.indexOf(IPs.get(x))).setActive(false);
								}
								else {
									if (!IPs.get(x).isActive && RMs.contains(IPs.get(x))){
										UpdateThread u = new UpdateThread(IPs.get(x), host, farm, files, versions);
										Thread uT = new Thread(u);
										uT.start();
									}
									IPPingCount.set(x,0);
									IPs.get(x).setActive(true);
								}
							}
						}
						else IPPingCount.set(index, setTo);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	});
	
	
	public static void sendObjectUDP(DatagramSocket s, IPPort dest, Object o) throws IOException{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(outputStream);
		os.writeObject(o);
		
		byte[] ping = outputStream.toByteArray();
		DatagramPacket send = new DatagramPacket(ping, ping.length,
				dest.getIP(), dest.getPort());
		s.send(send);
		os.flush();
	}
	
	public static IPPort getRM(){
		if(farm == 0) return RMs.get(1).isActive() ? RMs.get(1) : RMs.get(2).isActive() ? RMs.get(2) : host;
		if(farm == 1) return RMs.get(2).isActive() ? RMs.get(2) : host;
		return host;
		
	}		

}
