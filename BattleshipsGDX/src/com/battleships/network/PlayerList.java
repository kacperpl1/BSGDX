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
	public Player getById(String id) {
		for(Player player : list) {
			if(player.getId().equals(id)) {
				return player;
			}
		}
		return null;
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
