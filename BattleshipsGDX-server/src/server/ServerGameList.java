package server;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class ServerGameList{
	private List<ServerGame> gameList = Collections.synchronizedList(new LinkedList<ServerGame>());
	
	public ServerGameList(){
	}
	
	public int size(){
		return gameList.size();
	}
	
	public ServerGame createGame(String id, String name) {
		ServerGame game = new ServerGame(id, name);
		synchronized(gameList) {
			gameList.add(game);
		}
		return game;
	}
	
	public ServerGame getById(String id) {
		synchronized(gameList) {
			for(ServerGame game : gameList) {
				if(game.getId().equals(id)) {
					return game;
				}
			}
		}
		return null;
	}
	
	public ServerGame getGameByName(String name){
		synchronized(gameList) {
			for(ServerGame game : gameList) {
				if(game.getName().equals(name)) {
					return game;
				}
			}
		}
		return null;
	}
	
	
	public ServerGame getServerGame(int index){
		if(index >=0 && index < gameList.size()){
			return gameList.get(index);
		}
		return null;
	}
	
	public String toString(){
		String info = "";
		synchronized(gameList) {
			for(int i = 0; i < gameList.size(); i++){
				info += gameList.get(i).getId() + " " + gameList.get(i).getName() + " ";
			}
		}
		return info;
	}
	
	public void remove(String id){
		synchronized(gameList) {
			for(ServerGame game : gameList) {
				if(game.getId().equals(id)) {
					gameList.remove(game);
					break;
				}
			}
		}
	}
}

class ServerGame {
	private UUID id;
	private String name;
	private ServerPlayerList playerList = new ServerPlayerList();
	private final int MAX_PLAYERS = 6;
	private final int MIN_PLAYERS = 1;
	private boolean running = false;
	
	public ServerGame(String id, String name) {
		this.name = name;
		this.id = UUID.fromString(id);
	}
	
	public ServerGame(String name){
		this.name = name;
		this.id = UUID.randomUUID();
	}
	public String getId(){
		return String.valueOf(this.id);
	}
	public String getName(){
		return this.name;
	}
	public boolean addPlayer(ServerPlayer player){
		synchronized(playerList) {
			if(playerList.size() < this.MAX_PLAYERS){
				playerList.add(player);
				return true;
			}
			return false;
		}
	}
	public void removePlayer(ServerPlayer player){
		synchronized(playerList) {
			playerList.remove(player.getId());
		}
	}
	
	public ServerPlayerList getPlayerList(){
		return this.playerList;
	}
	public void start(){
		if(playerList.size() >= this.MIN_PLAYERS && playerList.size() <= this.MAX_PLAYERS){
			this.running = true;
		}
	}
	public void stop(){
		this.running = false;
	}
	public boolean isRunning(){
		return running;
	}
}
