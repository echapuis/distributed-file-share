package otherStuff;


import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

public class WriteFile {
	
	private String path;
	private boolean append_to_file = false;
	private boolean token = false;
	
	public WriteFile(String file_path){
		path = file_path;
	}
	
	public WriteFile(String file_path, boolean append_value) {
		path = file_path;
		append_to_file = append_value;
	}
	
	public void writeToFile(String textLine) throws IOException{
		this.getToken();
		FileWriter write = new FileWriter (path, append_to_file);
		PrintWriter print_line = new PrintWriter(write);
		
		print_line.printf("%s" + "\n", textLine);
		
		print_line.close();
		this.returnToken();
	}
	
	public void write(String line) throws IOException{
		FileWriter write = new FileWriter (path, append_to_file);
		PrintWriter print_line = new PrintWriter(write);
		
		print_line.printf("%s" + "\n", line);
		
		print_line.close();
	}
	
	public synchronized boolean getToken(){
		if (!token) {
			token = true;
			//System.out.println("Thread " + ID + " given token.");
			return token;
		}
		else {
			//System.out.println("Thread " + ID + " must wait for token.");
			try {
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return getToken();
		}
	}
	
	public boolean waitForToken(int ID){
		while (!tokenFree()){
			//System.out.println("Thread " + ID + " waiting...");
			try{
				Thread.sleep(1000);
			}
			catch (Exception e){};
		}
		return getToken();
	}
	
	public synchronized boolean tokenFree(){
		return !token;
	}
	
	public synchronized void returnToken(){
		token = false;
		this.notifyAll();
	}
	
}
