package com.battleships.network;

import java.util.LinkedList;
import java.util.StringTokenizer;

public class GameList {
	private LinkedList<Game> gameList = new LinkedList<Game>();;
	private static String msg = "";
	
	public GameList(){
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
	public void clear() {
		this.gameList.clear();
	}
}
