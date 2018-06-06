package otherStuff;
import java.io.Serializable;


public class Request implements Serializable {
	
	public String FileName;
	public String upDown;
	public IPPort dest;
	public int version = 0;
	
	public Request(String upDown, String fileName, IPPort dest){
		this.FileName = fileName;
		this.upDown = upDown;
		this.dest = dest;
	}
	
	public Request(String upDown, String fileName, IPPort dest, int version){
		this.FileName = fileName;
		this.upDown = upDown;
		this.dest = dest;
		this.version = version;
	}
	
	public String getFileName(){
		return this.FileName;
	}
	
	public IPPort getDest(){
		return this.dest;
	}
	
	public String getType(){
		return this.upDown;
	}
	
	public boolean isUpload(){
		return this.upDown.equals("u");
	}
	
	public boolean isDownload(){
		return this.upDown.equals("d");
	}
	
	public int getVersion(){
		return this.version;
	}
	
	public void setVersion(int v){
		this.version = v;
	}
	
	public void setType(String s){
		this.upDown = s;
	}
	
	public String toString(){
		return this.upDown + " " + this.FileName;
		//return this.isUpload() ? "Upload " + this.FileName : "Download " + this.FileName;
	}
}
