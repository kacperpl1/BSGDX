package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import shared.UnitData;
import shared.UnitMap;

public class GameThread extends Thread{
	private ServerGame game;
	private int currentTick = 1;
	
	public UnitMap unitMap = new UnitMap();
	Map<Short, LinkedBlockingQueue<UnitData>> queueMap = new HashMap<Short, LinkedBlockingQueue<UnitData>>();
	
	private Map<Short, UnitData> playerShipMap = new HashMap<Short, UnitData>();
	private Map<Short, Integer> counterMap = new HashMap<Short, Integer>();
	private ArrayList<Thread> playerThreadArray = new ArrayList<Thread>();
	
	public GameThread(ServerGame game){
		this.game = game;
		this.setName(game.getId());
		
		Iterator<ServerPlayer> iter = game.getPlayerList().iterator();
		while(iter.hasNext()) {
			ServerPlayer player = iter.next();
			queueMap.put(player.getSlotNumber(), new LinkedBlockingQueue<UnitData>());
			UnitData data = new UnitData();
			data.gameID = this.getName();
			data.unitKey = player.getSlotNumber();
			playerShipMap.put(player.getSlotNumber(), data);
			
			counterMap.put(player.getSlotNumber(), 0);
		}
	}
	
	public boolean allSync() {
		for(Entry<Short, UnitData> entry : playerShipMap.entrySet()) {
			if(entry.getValue().tick != this.currentTick-1) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isSync(short slot) {
		if(playerShipMap.get(slot).tick == this.currentTick-1) {
			return true;
		}
		return false;
	}
	
	public boolean nextTick(short slot) {
		if(playerShipMap.get(slot).tick < this.currentTick-1) {
			return true;
		}
		return false;
	}
	
	public void sendToAll() {
		Iterator<ServerPlayer> iter = game.getPlayerList().iterator();
		while(iter.hasNext()) {
			iter.next().getConnection().sendUDP(playerShipMap);
		}
	}
	
	public void dropPlayer(short slot) {
		System.out.println(game.getPlayerList().size());
		game.dropPlayer(slot);
		System.out.println("player " + slot + " dropped");
		System.out.println(game.getPlayerList().size());
	}
	
	public boolean allFinished() {
		Iterator<Thread> tIter = this.playerThreadArray.iterator();
		while(tIter.hasNext()) {
			if(tIter.next().isAlive()) {
				return false;
			}
			tIter.remove();
		}
		return true;
	}
	
	public void run(){
		System.out.println("game " + game.getName() + " started");
		while(playersAreConnected()){
			Iterator<ServerPlayer> iter = game.getPlayerList().iterator();
			while(!allFinished()) {
				// Wait for all playerThreads to finish
			}
			while(iter.hasNext()) {
				short slot = iter.next().getSlotNumber();
				
				if(!isSync(slot)) {
					Thread aux = new PlayerThread(slot, currentTick, queueMap, playerShipMap, game);
					aux.start();
					playerThreadArray.add(aux);
				}
			}
			if(allSync()) {
				System.out.println("all sync " + (this.currentTick - 1));
				sendToAll();
				this.currentTick++;
			}
		}
		System.out.println("game " + game.getName() + " stopped");
	}
	
	public LinkedBlockingQueue<UnitData> getMsgQueue(short slot) {
		return this.queueMap.get(slot);
	}
	
	public boolean playersAreConnected() {
		if(game.getPlayerList().size() > 0) {
			Iterator<ServerPlayer> iter = game.getPlayerList().iterator();
			while(iter.hasNext()) {
				if(iter.next().getConnection().isConnected()) {
					return true;
				}
			}
		}
		return false;
	}	
}

class PlayerThread extends Thread {
	
	private short slot = 0;
	private int currentTick = 0;
	private Map<Short, LinkedBlockingQueue<UnitData>> queueMap;
	private Map<Short, UnitData> playerShipMap;
	private ServerGame game;
	
	public PlayerThread(short slot, int currentTick, Map<Short, LinkedBlockingQueue<UnitData>> queueMap, Map<Short, UnitData> playerShipMap, ServerGame game) {
		this.slot = slot;
		this.queueMap = queueMap;
		this.playerShipMap = playerShipMap;
		this.game = game;
		this.currentTick = currentTick;
	}
	
	public boolean nextTick(short slot) {
		if(playerShipMap.get(slot).tick == currentTick-2) {
			return true;
		}
		return false;
	}
	
	public void dropPlayer() {
		System.out.println(game.getPlayerList().size());
		game.dropPlayer(slot);
		System.out.println("player " + slot + " dropped");
		System.out.println(game.getPlayerList().size());
	}
	
	public void run() {
		int counter = 0;
		while(counter < 5) {
			if(nextTick(slot)) {
				try {
					UnitData message = queueMap.get(slot).poll(30, TimeUnit.MILLISECONDS);
					if(message != null) {
						System.out.println(slot + " ! " + message.tick);
						if(message.tick == playerShipMap.get(slot).tick) {
							game.getPlayerBySlot(slot).getConnection().sendUDP(playerShipMap);
						} else {
							if(message.tick == this.currentTick-1) {
								playerShipMap.get(slot).direction.set(message.direction.x,message.direction.y);
								playerShipMap.get(slot).position.set(message.position.x,message.position.y);
								playerShipMap.get(slot).tick = message.tick;
								playerShipMap.get(slot).shopAction = message.shopAction;
								break;
							}
						}
					}
				} catch (InterruptedException e) {
					//e.printStackTrace();
				}
			} else {
				
				break;
			}
			counter++;
		}
		if(counter == 20) { 
			//playerShipMap.get(slot).tick = playerShipMap.get(slot).tick+1;
			//dropPlayer();
		}
	}
	
}
