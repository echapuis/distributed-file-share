import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import otherStuff.*;

public class test {
	
	public static void main(String[] args) throws IOException{
		
		File cd = new File("files");
		File[] files =  cd.listFiles();
		for (int i = 0; i < files.length; i ++){
			System.out.println(files[i]);
		}
	}
}
