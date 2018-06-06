package managerStuff;


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
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.regex.Pattern;

import otherStuff.*;

public class ManagerThread implements Runnable {
	public ObjectOutputStream out;
	public ObjectInputStream in;
	protected DatagramPacket packet;
	protected int portNumber;
	protected int type;
	protected String fileName;
	protected InetAddress ip;
	protected int port;
	
	public ReadFile upload;
	public static Request r = null;
	
	protected Socket socket;
	protected Socket sendSocket;
	protected IPPort RM;
	protected Request Request;
	protected String folder;
	
	protected IPPort Server;
	protected IPPort host;
	
	public ManagerThread(ObjectInputStream is, ObjectOutputStream os, Request r, IPPort RM, String folder, IPPort host) throws IOException {
//		this.ip = s.getInetAddress();
//		this.port = s.getPort();
//		this.socket = s;
		this.Request = r;
		this.RM = RM;
		this.folder = folder;
		this.Request.setType("u");
		this.host = host;
		this.in = is;
		
		if (!RM.equals(host)){
		
		try{ 
		
		this.Server = sendRequest(Request);
		
		this.sendSocket = new Socket(Server.getServerIP(), Server.getServerPort());
		
		ObjectOutputStream o =new ObjectOutputStream(sendSocket.getOutputStream());  
		out = o;
		}
		catch (Exception e) {e.printStackTrace();}
		}
	}
	
	public void run(){
		try {
			download();
			if (!RM.equals(host) && Request != null){
				out.writeObject(Request);
				upload();
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//System.out.println("ManagerThread completed");
	}
	
	public IPPort sendRequest(Request r) throws IOException{
		Socket Socket = null;
		try{ 
			Socket = new Socket(this.RM.getServerIP(), this.RM.getServerPort());
		}
		catch (Exception e) {}
		
		
		while (!Socket.isConnected()){try {Thread.sleep(30);} catch (Exception e) {}}
		ObjectOutputStream outStream = new ObjectOutputStream(Socket.getOutputStream());
		outStream.writeObject(r);
		ObjectInputStream inStream = new ObjectInputStream(Socket.getInputStream());
		
	
		IPPort server = null;
		try{
			server =  (IPPort) inStream.readObject();
			}
		catch (Exception e){}
		
		//System.out.println("got assigned server: " + server.toString());
		outStream.close();
		inStream.close();
		if (Socket != null) Socket.close();
		return server;
	}
	
	
	public void download(){
		File f = new File(folder + "/" + Request.getFileName());
		if (f.exists()) f.delete();
		
		WriteFile newFile = new WriteFile(folder + "/"+ Request.getFileName(), true);
		
		while (true)	
			try {
					String text = (String) in.readObject();
					if (text.equals("-EOF")) break;
					if (text.equals("-999")){
						//System.out.println("Server does not have requested file: " + original);
						//close();
						return;
					}
					newFile.writeToFile(text);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
		}
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void upload() throws IOException{
		ReadFile upload = new ReadFile(folder + "/" + Request.getFileName());
		String[] fileLines = upload.OpenFile();
		for (int i = 0; i < fileLines.length; i++){
			out.writeObject(fileLines[i]);
		}
		out.writeObject("-EOF");
		close();
	}

	public void waitForServer() throws InterruptedException{
		while (!sendSocket.isConnected()){
			Thread.sleep(10);
		}
	}
	
	public void close(){
		try {
			sendSocket.close();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
