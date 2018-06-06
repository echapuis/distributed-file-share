package otherStuff;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Util {
	
	public static LinkedList<IPPort> Managers;
	public static LinkedList<Integer> RMCount;
	public static int numManagers = 2;
	
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
	
	public static Object receiveObjectUDP(DatagramSocket s) throws IOException, ClassNotFoundException{
		byte[] buf = new byte[256];  //needs to be big enough to hold entire Serializable object
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		s.receive(packet);
		ObjectInputStream iStream = new ObjectInputStream( new ByteArrayInputStream(packet.getData()));
		return iStream.readObject();
	}
	
	public static IPPort getPing(DatagramSocket s) throws IOException{
		byte[] buf = new byte[256];  //needs to be big enough to hold entire Serializable object
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		s.receive(packet);
		return new IPPort(packet.getAddress(), packet.getPort());
	}
	

	public static String getIPTag(IPPort ip){
		String IPStr = ip.getIP().getHostAddress();
		String[] sA = IPStr.split("\\.");
		return sA[sA.length-1] + "-" + ip.getPort();
	}
	
	public static LinkedList<IPPort> getManagers() throws NumberFormatException, UnknownHostException{
		Path filePath = Paths.get("RMConfig");
		LinkedList<IPPort> ips = new LinkedList<IPPort>();
		Scanner scanner = null;
		try {
			scanner = new Scanner(filePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<String> strings = new LinkedList<String>();
		while (scanner.hasNext()) {
		    strings.add(scanner.next());
		}
		for (int i = 0; i < strings.size(); i+=4){
			ips.add(new IPPort(InetAddress.getByName(strings.get(i)),Integer.valueOf(strings.get(i+1)),
					InetAddress.getByName(strings.get(i+2)),Integer.valueOf(strings.get(i+3))));
		}
		return ips;
	}
	
	public static void startHB(IPPort host, IPPort dest){
		Heartbeat hb = new Heartbeat(host, dest);
		Thread hT = new Thread(hb);
		hT.start();
	}
	
	public static void deleteDirectory(final File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }
        Path p = directory.toPath();
        if (!Files.isSymbolicLink(p)) {
            cleanDirectory(directory);
        }

        if (!directory.delete()) {
            final String message =
                "Unable to delete directory " + directory + ".";
            throw new IOException(message);
        }
    }
	 
	
	 
	 public static void cleanDirectory(final File directory) throws IOException {
		         if (!directory.exists()) {
		             final String message = directory + " does not exist";
		             throw new IllegalArgumentException(message);
		         }
		 
		         if (!directory.isDirectory()) {
		             final String message = directory + " is not a directory";
		             throw new IllegalArgumentException(message);
		         }
		 
		         final File[] files = directory.listFiles();
		         if (files == null) {  // null if security restricted
		             throw new IOException("Failed to list contents of " + directory);
		         }
		 
		         IOException exception = null;
		         for (final File file : files) {
		             try {
		                 forceDelete(file);
		             } catch (final IOException ioe) {
		                 exception = ioe;
		             }
		         }

		         if (null != exception) {
		             throw exception;
		         }
		     }
	 
	 public static void forceDelete(final File file) throws IOException {
		         if (file.isDirectory()) {
		             deleteDirectory(file);
		         } else {
		             final boolean filePresent = file.exists();
		             if (!file.delete()) {
		                 if (!filePresent){
		                     throw new FileNotFoundException("File does not exist: " + file);
		                 }
		                 final String message =
		                     "Unable to delete file: " + file;
		                 throw new IOException(message);
		             }
		         }
		     }
	 
	public static void createFolder( String folderPath){
		
		File dir = new File(folderPath);
		dir.mkdir();
		
	}
	
	public static boolean createFile(String filePath, Boolean replace)
   {	
		if (!replace){
   	try {
   		 
	      File file = new File(filePath);
	      
	      if (!file.createNewFile()){
	        //System.out.println("File: " + filePath + " exists already.");
	        return false;
	      }
	      
   	} catch (IOException e) {
   	  System.out.println("Error creating file: " + filePath);
	      e.printStackTrace();
	}
		}
		else {
			Path p = Paths.get(filePath);
			try {
				Files.deleteIfExists(p);
				File file = new File(filePath);
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			
		}
   	return true;
   }
	
	public static boolean fileOfType (String fileName, String type){
		int l = fileName.length();
		String t = type;
		if (l < t.length()) return false;
		for (int i=1; i < t.length()+1; i++){
			if (!(fileName.charAt(l-i) == t.charAt(t.length()-i))) return false;
		}
		return true;
	}
	
	public static boolean lineContains(String line, String word, boolean startsWith){
		if (word.length() > line.length()) return false;
		if (startsWith){
			if (line.substring(0,word.length()).equals(word)) return true;
		}
		else {
			for (int l = 0; l < line.length() - word.length()+1; l++){
				if (line.substring(l,l+word.length()).equals(word)) {
					if (l != 0) {
						if (!Character.isWhitespace(line.charAt(l-1))) return false;
					}
					return true;
				}
			}
		}
		return false;
	}
}
