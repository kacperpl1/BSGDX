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
	        	String id = part.nextToken();
	        	String name = part.nextToken();
	        	getGame(id, name);
	        }
		}
	}
	
	public Game getGame(String id, String name) {
		for(Game game : gameList) {
			if(game.getId().equals(id)) {
				return game;
			}
		}
		gameList.add(new Game(id, name));
		return gameList.getLast();
	}
	
	public Game getGameById(String id) {
		for(Game game : gameList) {
			if(game.getId().equals(id)) {
				return game;
			}
		}
		return null;
	}
	
	public void addGame(Game game) {
		gameList.add(game);
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
	    while(part.hasMoreTokens()) {
	    	String id = part.nextToken();
	    	String name = part.nextToken();
	    	getPlayer(id, name);
	    }
	}
	public void getPlayer(String id, String name) {
		for(Player player : playerList) {
			if(player.getId().equals(id)){
				return;
			}
		}
		Player player = new Player(id, name);
		playerList.add(player);
	}
	
	public Player getPlayer(String id) {
		for(Player player : playerList) {
			if(player.getId().equals(id)){
				return player;
			}
		}
		return null;
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
