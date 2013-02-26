package com.battleships.network;

import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.UUID;

public class GameList {
	private LinkedList<Game> gameList;
	private static String msg = "";
	
	public GameList(){
		gameList = new LinkedList<Game>();
	}
	public void translateServerString(String received){
		if(!(msg.equals(received))){
			msg = received;
	        StringTokenizer part = new StringTokenizer(received);
	        part.nextToken();
	        
	        checkForFinishedGames();
	        
	        while(part.hasMoreTokens()) {
	        	String piece = part.nextToken();
	        	getByName(piece);
	        }
		}
	}
	public Game getByName(String name){
		for(int i = 0; i < gameList.size(); i++){
			if(gameList.get(i).getName().equals(name)){
				return gameList.get(i);
			}
		}
		gameList.add(new Game(name));
		return gameList.getLast();
	}
	public Game getGame(int index){
		return gameList.get(index);
	}
	public void checkForFinishedGames(){
		for(int i = 0; i < gameList.size(); i++){
			if(gameList.get(i).getPlayerList().size() == 0){
				gameList.remove(i);
			}
		}
	}
	public int size(){
		return gameList.size();
	}
	public void reset(){
		GameList.msg = "";
	}
}

class Game {
	private UUID id;
	private String name;
	private LinkedList<Player> playerList;
	private final int MAX_PLAYERS = 6;
	private final int MIN_PLAYERS = 1;
	private boolean running;
	
	public Game(String name){
		this.name = name;
		this.playerList = new LinkedList<Player>();
		this.running = false;
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
	public LinkedList<Player> getPlayerList(){
		return this.playerList;
	}
	public boolean allReady(){
		for(int i = 0; i < playerList.size(); i++){
			if(playerList.get(i).isReady() == false){
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
