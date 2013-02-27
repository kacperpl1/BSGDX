package com.battleships.network;

import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.UUID;

public class PlayerList {
	
	LinkedList<Player> list = new LinkedList<Player>();
	private static String msg = "";
	
	public PlayerList() {
	}
	public void translateServerString(String received) {
		if(!(msg.equals(received))){
			msg = received;
	        StringTokenizer part = new StringTokenizer(received);
	        part.nextToken();
	        //if user not present
	        kickAll();
	        
	        while(part.hasMoreTokens()) {
	        	String id = part.nextToken();
	        	String name = part.nextToken();
	        	getPlayer(id, name);
	        	// THIS HAS TO BE CHANGED!
	        	//if(!piece.equals("Empty"))
	        		//getByName(piece);
	        }
		}
	}
	public void kickAll() {
		for(int i = 0; i < list.size(); i++) {
			list.get(i).setOffline();
		}
	}
	public void checkKick() {
		for(int i = list.size()-1; i >= 0; i--) {
			if(list.get(i).isOffline())	{
				list.remove(i);
			}
		}
	}
	public Player getPlayer(String id, String name){
		for(Player player : list) {
			if(player.getId().equals(id)){
				player.setOnline();
				return player;
			}
		}
		Player player = new Player(id, name);
		list.add(player);
		return player;
	}
	public Player getByName(String n) {
		for(int i = 0; i < list.size(); i++) {
			if(list.get(i).getName().equals(n)) {
				list.get(i).setOnline();
				return list.get(i);
			}
		}
		list.add(new Player(n));
		return list.get(list.size()-1);
	}
	public Player getPlayer(int index) {
		return list.get(index);
	}
	public int size() {
		return list.size();
	}
	public void reset(){
		PlayerList.msg = "";
	}
}

class Player{
	private UUID id;
	private String name;
	private boolean online = true;
	private boolean ready = false;
	private boolean host = false;
	private String gameName = "";
	private int slotNumber = -1;
	
	public Player(String name){
		this.id = UUID.randomUUID();
		this.name = name;
	}
	
	public Player(String id, String name) {
		this.id = UUID.fromString(id);
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	public boolean isOnline(){
		return online;
	}
	public boolean isOffline(){
		return !online;
	}
	public void setOnline(){
		online = true;
	}
	public void setOffline(){
		online = false;
	}
	public boolean isReady(){
		return ready;
	}
	public void setReady(boolean ready){
		this.ready = ready;
	}
	public boolean isHost(){
		return host;
	}
	public void setHost(boolean host){
		this.host = host;
	}
	public String getId(){
		return String.valueOf(this.id);
	}
	public void joinGame(String gameName){
		this.gameName = gameName;
	}
	public void leaveGame(){
		this.gameName = "";
	}
	public String getGame(){
		return this.gameName;
	}
	public void takeSlot(int s){
		this.slotNumber = s;
	}
	public void freeSlot(){
		this.slotNumber = -1;
	}
	public int getSlotNumber(){
		return this.slotNumber;
	}
}