package server;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.esotericsoftware.kryonet.Connection;

public class ServerPlayerList
{
	private List<ServerPlayer> list = Collections.synchronizedList(new LinkedList<ServerPlayer>());
	//protected LinkedList<ServerPlayer> list = new LinkedList<ServerPlayer>();
	String test = "";
	
	public ServerPlayerList() {
	}
	public void advance(float elapsedTime) {
		synchronized(list) {
			for(int x=list.size()-1;x>=0;x--) {
				list.get(x).advance(elapsedTime);
				if(list.get(x).getKick()) {
					System.out.println("LOG: " + list.get(x).getName()+" Timed Out");
					list.remove(x);
					System.out.println(elapsedTime);
				}
			}
		}
	}
	public String toString() {
		String info = "";
		synchronized(list) {
			for(ServerPlayer player : list) {
				info += player.getId() + " " + player.getName() + " ";
			}
		}
		return info;
	}
	public String toString(int flag){
		String info = "";
		String[] strList = {"0 Empty", "0 Empty", "0 Empty", "0 Empty", "0 Empty", "0 Empty"};
		synchronized(list) {
			for(ServerPlayer player : list) {
				if(player.getSlotNumber() >= 0) {
					strList[player.getSlotNumber()] = player.getId() + " " + player.getName();
				}
			}
		}
		for(int i = 0; i < strList.length; i++){
			info += strList[i] + " ";
		}
		return info;
	}
	public void add(ServerPlayer player){
		list.add(player);
	}
	
	public void addPlayer(String id, String name, Connection con){
		list.add(new ServerPlayer(id, name, con));
	}
	public ServerPlayer getByName(String n) {
		synchronized(list) {
			for(ServerPlayer player : list) {
				if(player.getName().equals(n)) {
					return player;
				}
			}
		}
		return null;
	}
	public ServerPlayer getById(String id) {
		synchronized(list) {
			for(ServerPlayer player : list) {
				if(player.getId().equals(id)){
					return player;
				}
			}
		}
		return null;
	}
	public ServerPlayer getServerPlayer(int index) {
		return list.get(index);
	}
	public int size() {
		return list.size();
	}
	public boolean exists(String id) {
		synchronized(list) {
			for(ServerPlayer player : list) {
				if(player.getId().equals(id)){
					return true;
				}
			}
		}
		return false;
	}
	public void remove(String id){
		synchronized(list) {
			for(ServerPlayer player : list) {
				if(player.getId().equals(id)) {
					list.remove(player);
				}
			}
		}
	}
	public void remove(int index){
		list.remove(index);
	}
}

class ServerPlayer {
	//name
	private String name = "";
	private String ip = "";
	private Connection connection;
	private int slotNumber = -1;
	private String id = "";
	
	//time out counter
	int timeOut = 0,kickTime = 5000;
	boolean kick = false;
	
	public ServerPlayer(String id, String n, Connection c) {
		this.id = id;
		this.name = n;
		this.connection = c;
		System.out.println("LOG: " + name+" Connected");
	}
	public void advance(float eT) {
		stepTimeout(eT);
	}
	public void stepTimeout(float eT) {
		timeOut += eT;
		if(timeOut>kickTime){
			kick = true;
		}
	}
	public void resetKeys()	{
		timeOut=0;
	}
	public boolean getKick() {
		return kick;
	}
	public String getName()	{
		return name;
	}
	public String getIP() {
		return ip;
	}
	public Connection getConnection() {
		return connection;
	}
	public void setSlotNumber(int num){
		this.slotNumber = num;
	}
	public int getSlotNumber(){
		return slotNumber;
	}
	public String getId(){
		return this.id;
	}
}
