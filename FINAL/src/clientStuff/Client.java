package clientStuff;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.LinkedList;
import otherStuff.*;

public class Client {
	
	
	public static DatagramSocket socket;
	public static IPPort Server;
	protected static IPPort host;
	
	public static String Folder;
	
	public static LinkedList<IPPort> Managers = new LinkedList<IPPort>();
	
	public static int numManagers = 2;
	
	public static void main(String[] args) throws IOException, NoSuchMethodException, SecurityException{
		socket = new DatagramSocket();
		host = new IPPort(InetAddress.getLocalHost(), socket.getLocalPort());
		System.out.println(host.toString());
		Managers = Util.getManagers();
		System.out.println("Managers: " + Managers.toString());
		
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(
				System.in));
		System.out.println("Format for commands is u/d fileName (ex. u test)");
		
		initFolder();
		
		while (true){
			String in = inFromUser.readLine();
			String[] ar = in.split(" ");
			if (ar.length < 2) continue;
			Request r = new Request(ar[0], ar[1], host);
			if (!fileInFolder(r) && r.getType().equalsIgnoreCase("u")) {
				System.out.println("Error: No such file in client directory.");
				continue;
			}
			if (r.getType().equalsIgnoreCase("d")||r.getType().equalsIgnoreCase("u")){
				Server = sendRequest(r);
				try{
				ClientThread t = new ClientThread(Server, r, Folder);
				Thread ct = new Thread(t);
				ct.start();
				}
				catch (Exception e){
					//System.out.println("Client error");
					//e.printStackTrace();
				}
				continue;
			}
			else{
				System.out.println("Error: incorrect syntax. Format is u/d fileName (ex. u test)");
			}
		}
	}
	
	public static void initFolder(){
		Folder = "files/client " + host.getPort();
		Util.createFolder(Folder);
		File copyFile = new File("cTest");
		try {
			Files.copy(copyFile.toPath(), new File(Folder + "/cTest").toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		copyFile = new File("cTest1");
		try {
			Files.copy(copyFile.toPath(), new File(Folder + "/cTest1").toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		copyFile = new File("cTest2");
		try {
			Files.copy(copyFile.toPath(), new File(Folder + "/cTest2").toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		copyFile = new File("cTest4");
		try {
			Files.copy(copyFile.toPath(), new File(Folder + "/cTest4").toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		copyFile = new File("moby");
		try {
			Files.copy(copyFile.toPath(), new File(Folder + "/moby").toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		copyFile = new File("prize");
		try {
			Files.copy(copyFile.toPath(), new File(Folder + "/prize").toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static boolean fileInFolder(Request r){
		String fileName = r.getFileName();
		File dir = new File(Folder);
		File[] files = dir.listFiles();
		for (int i = 0 ; i < files.length; i ++){
			if (files[i].getName().equalsIgnoreCase(fileName)) return true;
		}
		return false;
	}
	
	public static IPPort sendRequest(Request r) throws IOException{
		int i = 0;
		boolean loop = true;
		IPPort RM = null;
		Socket Socket = null;
		while (loop){
			RM = Managers.get(i);
			//System.out.println("checking: " + Managers.get(i).toString());
			try{ 
				Socket = new Socket(RM.getServerIP(), RM.getServerPort());
				break;
			}
			catch (Exception e) {}
			i = Managers.size() <= i ? 0 : i+1;
		}
		while (!Socket.isConnected()){ try {Thread.sleep(30);} catch (Exception e) {}}
		ObjectOutputStream out = new ObjectOutputStream(Socket.getOutputStream());
		out.writeObject(r);
		System.out.println("connection with " + Socket.toString());
		
		ObjectInputStream in = new ObjectInputStream(Socket.getInputStream());
		
	
		IPPort server = null;
		try{
			server =  (IPPort) in.readObject();
			}
		catch (Exception e){}
		
		//System.out.println("client given address: " + server.getServerPort());
		return server;
	}
}