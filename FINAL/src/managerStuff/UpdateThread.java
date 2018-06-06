package managerStuff;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;

import otherStuff.*;


public class UpdateThread implements Runnable{
	
	public IPPort manager;
	public IPPort host;
	public IPPort Server;
	public int farm;
	public String folder;
	
	public LinkedList<String> files;
	public LinkedList<Integer> versions;
	
	
	public UpdateThread(IPPort manager, IPPort host, int farm, LinkedList<String> files, LinkedList<Integer> versions){
		this.manager = manager;
		this.host = host;
		this.farm = farm;
		this.files = files;
		this.versions = versions;
		
		//System.out.println("files: " + files.toString());
		//System.out.println("vers: " + versions.toString());
		
		this.folder = "files/RM " + farm;
	}
	
	public void run(){
		//System.out.println("updating");
		File dir = new File(folder);
		File[] files = dir.listFiles();
		if (files.length == 0) return;
		
		//System.out.println("files in dir");
		LinkedList<String> fs = new LinkedList<String>();
		for (int i = 0; i < files.length; i++){
			fs.add(files[i].getName());
			if (files[i].getName().substring(0,1).equals(".")) files[i].delete();
		}
		
		Request req = getRequest(files[0]);
		IPPort server = null;
		try {
			server = sendRequest(req);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (int i = 1; i < files.length; i++){
			try {
				//System.out.println("file " + files[i].getName());
				//System.out.println("files: " + fs.toString());
				if (files[i].getName().equalsIgnoreCase("files") || files[i].getName().substring(0,1).equals(".")) continue;
				req = getRequest(files[i]);
				if (req == null) {
					//System.out.println("request null");
					continue;
				}
				    
				    //System.out.println("received server: " + server.toString());
					upload(req,server);
				
			} catch (Exception e){ e.printStackTrace();}
		}
	}
	
	public IPPort sendRequest(Request r) throws IOException{
		//System.out.println("sendreq: " + manager.getServerIP() + ":" + manager.getServerPort());
		Socket Socket = null;
		try{ 
			Socket = new Socket(this.manager.getServerIP(), this.manager.getServerPort());
		}
		catch (Exception e) {e.printStackTrace();}
		
		//System.out.println("got a socket, waiting to connect...");
		while (!Socket.isConnected()){ try { Thread.sleep(30);} catch (Exception e) {e.printStackTrace();}}
		//System.out.println("connected.");
		ObjectOutputStream outStream = new ObjectOutputStream(Socket.getOutputStream());
		outStream.writeObject(r);
		//System.out.println("os done.");
		ObjectInputStream inStream = new ObjectInputStream(Socket.getInputStream());
		//System.out.println("is done.");
		
	
		IPPort server = null;
		try{
			server =  (IPPort) inStream.readObject();
			}
		catch (Exception e){}
		
		//System.out.println("got assigned server: " + server.toString());
		outStream.close();
		inStream.close();
		Socket.close();
		return server;
	}
	
	public Request getRequest(File f){
	
		
		String filename = f.getName();
		int v = files.contains(filename) ? versions.get(files.indexOf(filename)) : 0;
		
		Request r = new Request("u", filename, manager, 0);
		//System.out.println("gR Req: " + r.toString());
		return r;
		
	}
	public void upload(Request r, IPPort server) throws IOException, InterruptedException{
		//System.out.println("upload " + r.toString() + " to " + server.toString());
		ReadFile upload = new ReadFile(folder + "/" + r.getFileName() );
		
		Socket s = new Socket(server.getServerIP(), server.getServerPort());
		while (!s.isConnected()){
				Thread.sleep(30);
		}
		//System.out.println("upload socket connected");
		ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
		
		out.writeObject(r);
		String[] fileLines = upload.OpenFile();
		for (int i = 0; i < fileLines.length; i++){
			out.writeObject(fileLines[i]);
		}
		out.writeObject("-EOF");
		//System.out.println("uploaded " + r.getFileName() + " to " + server.toString());
		out.close();
	}

	}
