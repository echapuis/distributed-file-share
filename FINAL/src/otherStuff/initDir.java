package otherStuff;
import java.io.File;
import java.io.IOException;


public class initDir {
	
	public static void main(String[] args) throws IOException{
		File serverFolder = new File("files");
		File[] files =  serverFolder.listFiles();
		for (int i = 0; i < files.length; i ++){
			if (files[i].isDirectory()) Util.deleteDirectory(files[i]);
		}
	}

}
