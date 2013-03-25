package server;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.esotericsoftware.kryonet.Connection;

public class ServerPlayerList
{
	private List<ServerPlayer> list = Collections.synchronizedList(new LinkedList<ServerPlayer>());
	
	public ServerPlayerList() {
	}
	public String mainLobbyToString() {
		String info = "";
		synchronized(list) {
			for(ServerPlayer player : list) {
				if(!player.isInGame()) {
					info += player.getId() + " " + player.getName() + " ";
				}
			}
		}
		return info;
	}
	public String gameLobbyToString() {
		String info = "";
		synchronized(list) {
			for(ServerPlayer player : list) {
				if(player.isInGame()) {
					info += player.getId() + " " + player.getName() + " ";
				}
			}
		}
		return info;
	}
	public String slotListToString() {
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
			Iterator<ServerPlayer> iter =  list.iterator();
			while(iter.hasNext()) {
				if(iter.next().getId().equals(id)) {
					iter.remove();
				}
			}
		}
	}
	public void remove(int index){
		synchronized(list){
			list.remove(index);
		}
	}
}

class ServerPlayer {
	//name
	private boolean inGame = false;
	private String name = "";
	private Connection connection;
	private short slotNumber = -1;
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
	public Connection getConnection() {
		return connection;
	}
	public void setSlotNumber(short num){
		this.slotNumber = num;
	}
	public short getSlotNumber(){
		return slotNumber;
	}
	public String getId(){
		return this.id;
	}
	public void joinGame() {
		this.inGame = true;
	}
	public void leaveGame() {
		this.inGame = false;
	}
	public boolean isInGame() {
		return this.inGame;
	}
}
