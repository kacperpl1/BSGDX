package com.battleships.network;

import java.util.UUID;

public class Player{
	private UUID id;
	private String name;
	private boolean online = true;
	private boolean ready = false;
	private boolean host = false;
	private String gameId = "";
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
	public void joinGame(String gameId){
		this.gameId = gameId;
	}
	public void leaveGame(){
		this.gameId = "";
		this.ready = false;
		this.slotNumber = -1;
		this.setHost(false);
	}
	public String getGameId(){
		return this.gameId;
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
