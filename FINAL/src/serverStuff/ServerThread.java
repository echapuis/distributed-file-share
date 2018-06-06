package serverStuff;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import otherStuff.*;

public class ServerThread implements Runnable {
	public ObjectOutputStream out;
	public ObjectInputStream in;
	public Request Request;
	
	
	protected DatagramPacket packet;
	protected int portNumber;
	protected int type;
	protected String fileName;
	protected InetAddress ip;
	protected int port;
	protected Socket socket;
	public ReadFile upload;
	public Request r = null;
	public LinkedList<IPPort> servers = new LinkedList<IPPort>();
	public String folder;
	public IPPort host;
	public IPPort RM;
	
	public ServerThread(IPPort host, Socket s, LinkedList<IPPort> servers, String folder, IPPort RM) throws IOException {
//		this.ip = s.getInetAddress();
//		this.port = s.getPort();
//		this.socket = s
		//System.out.println("thread with servers: " + servers.toString());
		this.host = host;
		this.in = new ObjectInputStream(s.getInputStream());
		this.out = new ObjectOutputStream(s.getOutputStream());
		this.RM = RM;
		try {
			this.Request = (Request) in.readObject();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.servers = servers;
		this.folder = folder;
	}
	
	public void run(){
		//System.out.println("Thread ID# " + Thread.currentThread().getId() + " started for thread IP " + this.ip + " port " + this.port );
		
		try{
		if (Request.getType().equalsIgnoreCase("u")) upload(Request);
		else if (Request.getType().equalsIgnoreCase("d")) download(Request, out);
		
		}
		catch (Exception e){
			e.printStackTrace();
		}
		System.out.println("Completed Request: " + Request.toString());
	}
	
	
	
	public void upload(Request r) throws ClassNotFoundException{
		//System.out.println("uploading " + r.getFileName());
		File f = new File(folder + "/" + r.getFileName());
		if (f.exists()) f.delete();
		WriteFile newFile = new WriteFile(folder + "/" + r.getFileName(), true);
		while (true)	
			try {
					String text = (String) in.readObject();
					if (text.equals("-EOF")) break;
					if (text.equals("-999")){
						System.out.println("Server does not have requested file: " + Request.getFileName());
						close();
						return;
					}
					newFile.writeToFile(text);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
		}
		//System.out.println("finished uploading.");
		close();
		try{ sendToServers(r);} catch (Exception e){ e.printStackTrace();}
		try{ sendToRM(r);} catch (Exception e){ e.printStackTrace();}
	}
	
	public void download(Request r, ObjectOutputStream output) throws IOException{
		//System.out.println("requested file for download: " + r.getFileName());
		File f = new File(folder + "/" + r.getFileName());
		if (!f.exists()){
			output.writeObject("-999");
			close();
			return;
		}
		else {
			ReadFile upload = new ReadFile(f.getPath());
			String[] fileLines = null;
			try {
				fileLines = upload.OpenFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (int i = 0; i < fileLines.length; i++){
				output.writeObject(fileLines[i]);
			}
			output.writeObject("-EOF");
		}
		close();
		//System.out.println("completed download for file: " + r.getFileName());
	}
	
	public boolean FileExists(){
		boolean fileExists = false;
		try{
			upload = new ReadFile(folder +"/" + r.getFileName());
			fileExists = true;
		}
		catch (Exception e){
		}
		return fileExists;
	}
	
	public void close(){
		try{
			socket.close();
		}
		catch (Exception e){
			
		}
		
	}
	
	public void sendToServers(Request r) throws IOException{
		for (int i = 0; i < servers.size(); i ++){
			if (servers.get(i).equals(host)) continue;
			Socket s = new Socket(servers.get(i).getServerIP(), servers.get(i).getServerPort());
			try {
				waitForServer(s);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ObjectOutputStream o = new ObjectOutputStream(s.getOutputStream());
			o.writeObject(r);
			download(r,o);			
		}
	}
	
	public void sendToRM(Request r) throws IOException{
		Request newReq = new Request("r", r.getFileName(), this.host, 1);
		Socket s = new Socket(RM.getServerIP(), RM.getServerPort());
		try {
			waitForServer(s);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ObjectOutputStream o = new ObjectOutputStream(s.getOutputStream());
		o.writeObject(newReq);
		download(newReq,o);		
		
	}
	
	public void waitForServer(Socket Socket) throws InterruptedException{
		while (!Socket.isConnected()){
			Thread.sleep(10);
		}
	}

}
