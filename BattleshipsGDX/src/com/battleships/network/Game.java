package com.battleships.network;

import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.UUID;

public class Game {
	private UUID id;
	private String name;
	private LinkedList<Player> playerList = new LinkedList<Player>();
	private final int MAX_PLAYERS = 6;
	private final int MIN_PLAYERS = 1;
	private boolean running = false;
	
	public Game(String name) {
		this.id = UUID.randomUUID();
		this.name = name;
	}
	public Game(String id, String name){
		this.id = UUID.fromString(id);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	public String getId(){
		return String.valueOf(this.id);
	}
	public Player getPlayer(int index){
		if(index >= this.MIN_PLAYERS - 1 && index < this.MAX_PLAYERS){
			return playerList.get(index);
		}
		return null;
	}
	public boolean addPlayer(Player player){
		if(playerList.size() < this.MAX_PLAYERS){
			playerList.add(player);
			return true;
		}
		return false;
	}
	public boolean removePlayer(Player player) {
		return playerList.remove(player);
	}
	public LinkedList<Player> getPlayerList(){
		return this.playerList;
	}
	public void translateServerString(String received) {
	    StringTokenizer part = new StringTokenizer(received);
	    part.nextToken();
	    playerList.clear();
	    while(part.hasMoreTokens()) {
	    	String id = part.nextToken();
	    	String name = part.nextToken();
	    	getPlayer(id, name);
	    }
	}
	public Player getPlayer(String id, String name) {
		synchronized(playerList) {
			for(Player player : playerList) {
				if(player.getId().equals(id)){
					return player;
				}
			}
			Player player = new Player(id, name);
			playerList.add(player);
			return player;
		}
	}
	
	public Player getPlayer(String id) {
		synchronized(playerList) {
			for(Player player : playerList) {
				if(player.getId().equals(id)){
					return player;
				}
			}
			return null;
		}
	}
	public boolean allReady(){
		for(Player player : playerList) {
			if(player.isReady() == false) {
				return false;
			}
		}
		return true;
	}
	public void start(){
		this.running = true;
	}
	public void stop(){
		this.running = false;
	}
	public boolean isRunning(){
		return running;
	}
}