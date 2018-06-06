package clientStuff;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Pattern;
import otherStuff.*;

public class ClientThread implements Runnable{
	
	public IPPort Server;
	public Request Request;
	public Socket Socket;
	public ObjectOutputStream out;
	public ObjectInputStream in;
	public String folder;
	
	public ClientThread(IPPort Server, Request r, String folder) throws IOException{
		this.Server = Server;
		this.Request = r;
		//System.out.println("Sendserver: " + Server.getServerPort());
		this.Socket = new Socket(Server.getServerIP(), Server.getServerPort());
		this.folder =folder;
		
	}
	
	public void run(){
		//System.out.println("running request: " + Request.toString() + " for server: " + Server.toString());
		try {
			waitForServer();
			//System.out.println("server connected");
			this.out = new ObjectOutputStream(Socket.getOutputStream());
			//System.out.println("outgood");
			this.in = new ObjectInputStream(Socket.getInputStream());
			//System.out.println("ingood");
			out.writeObject(this.Request);
			//System.out.println("Sent request: " + Request.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (Request.getType().equalsIgnoreCase("d")){
			download();
		}
		else {
			try { upload();} catch (Exception e) {
				System.out.println("error uploading");
			}
		}
	}
	
	public void download(){
		String original = Request.getFileName();
		File f = null;
		while (true){
			f = new File(folder + "/" + Request.getFileName());
			if (f.exists()) {
				//System.out.println(r.getFileName());
				String[] pA = Request.getFileName().split(Pattern.quote("."));
				String[] fA = pA.length>0 ? pA[0].split("-") : Request.getFileName().split("-");
				if (fA.length < 2){
					Request.FileName = fA[0] + "-1";
				}
				else {
					Request.FileName = fA[0] + "-" + Integer.toString(Integer.valueOf(fA[1])+1);
				}
				if (pA.length>1) Request.FileName += "." + pA[1];
			}
			else { break; }
			
		}
		
		WriteFile newFile = new WriteFile(folder + "/"+ Request.getFileName(), true);
		while (true)	
			try {
					String text = (String) in.readObject();
					if (text.equals("-EOF")) break;
					if (text.equals("-999")){
						System.out.println("Server does not have requested file: " + original);
						close();
						return;
					}
					newFile.writeToFile(text);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
		}
		close();
		if (!original.equals(Request.getFileName())) original += "(saved as " + Request.getFileName() + ")";
		System.out.println("Downloaded File: " + original + " from [" + Server.getServerIP().getHostAddress() + ":" + Server.getServerPort() + "]");
	}
	
	public void upload() throws IOException{
		ReadFile upload = new ReadFile(folder + "/" + Request.getFileName());
		String[] fileLines = upload.OpenFile();
		for (int i = 0; i < fileLines.length; i++){
			out.writeObject(fileLines[i]);
		}
		out.writeObject("-EOF");
		System.out.println("Uploaded " + Request.getFileName() + " to [" + Server.getServerIP().getHostAddress() + ":" + Server.getServerPort() + "]");
		close();
	}

	public void waitForServer() throws InterruptedException{
		while (!Socket.isConnected()){
			Thread.sleep(10);
		}
	}
	
	public void close(){
		try {
			Socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
