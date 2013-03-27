package server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;

import shared.UnitData;
import shared.UnitMap;

class GameThread extends Thread{
	private ServerGame game;
	private volatile boolean running;
	private int currentTick = 1;
	
	public UnitMap unitMap = new UnitMap();
	Map<Short, LinkedBlockingQueue<UnitData>> queueMap = new HashMap<Short, LinkedBlockingQueue<UnitData>>();
	
	private Map<Short, UnitData> playerShipMap = new HashMap<Short, UnitData>();
	private Map<Short, Integer> counterMap = new HashMap<Short, Integer>();
	
	public GameThread(ServerGame game){
		this.game = game;
		this.running = true;
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
	
	public void run(){
		System.out.println("game " + game.getName() + " started");
		while(running){
			if(playersAreConnected()) {
				Iterator<ServerPlayer> iter = game.getPlayerList().iterator();
				while(iter.hasNext()) {
					short slot = iter.next().getSlotNumber();
					if(!queueMap.get(slot).isEmpty()) {
						if(nextTick(slot)) {
							UnitData message = queueMap.get(slot).poll();
							if(message.tick == playerShipMap.get(message.unitKey).tick) {
								counterMap.put(slot, counterMap.get(slot) + 1);
								System.out.println(slot + " " + counterMap.get(slot));
								if(counterMap.get(slot) > 3) {
									game.getPlayerBySlot(slot).getConnection().sendUDP(playerShipMap);
									counterMap.put(slot, 0);
								}
							} else {
								counterMap.put(slot, 0);
								playerShipMap.get(message.unitKey).direction.set(message.direction.x,message.direction.y);
								playerShipMap.get(message.unitKey).position.set(message.position.x,message.position.y);
								playerShipMap.get(message.unitKey).tick = message.tick;
								playerShipMap.get(message.unitKey).shopAction = message.shopAction;
								System.out.println("current: " + this.currentTick + " slot" + slot + ": " + message.tick);
							}
						}
					}
				}
				if(allSync()) {
					System.out.println("all sync " + (this.currentTick - 1));
					sendToAll();
					this.currentTick++;
				}
			} else {
				this.running = false;
			}
		}
		System.out.println("game " + game.getName() + " stopped");
	}
	
	public void stopGame(){
		this.running = false;
	}
	
	public LinkedBlockingQueue<UnitData> getMsgQueue(short slot) {
		return this.queueMap.get(slot);
	}
	
	public boolean playersAreConnected() {
		Iterator<ServerPlayer> iter = game.getPlayerList().iterator();
		while(iter.hasNext()) {
			if(iter.next().getConnection().isConnected()) {
				return true;
			}
		}
		return false;
	}	
}
