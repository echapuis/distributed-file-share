package otherStuff;

import java.io.Serializable;
import java.util.LinkedList;

public class IPList implements Serializable{
	
	public LinkedList<IPPort> list;
	
	public IPList(LinkedList<IPPort> ips){
		this.list = ips;
	}
	
	public LinkedList<IPPort> getList(){
		return this.list;
	}
}
