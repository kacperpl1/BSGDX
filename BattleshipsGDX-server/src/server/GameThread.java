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
	
	public GameThread(ServerGame game){
		this.game = game;
		this.running = true;
		this.setName(game.getId());
		
		for(int i = 0; i < game.getPlayerList().size(); i++) {
			queueMap.put(game.getPlayerList().getServerPlayer(i).getSlotNumber(), new LinkedBlockingQueue<UnitData>());
			UnitData data = new UnitData();
			data.gameID = this.getName();
			data.unitKey = game.getPlayerList().getServerPlayer(i).getSlotNumber();
			playerShipMap.put(game.getPlayerList().getServerPlayer(i).getSlotNumber(), data);
		}
	}
	
	public boolean allSync() {
		for(Entry<Short, UnitData> entry : playerShipMap.entrySet()) {
			if(entry.getValue().tick >= this.currentTick) {
				//System.out.println("current: " + this.currentTick + " slot: " + entry.getValue().unitKey + " " +  entry.getValue().tick);
				reSync(entry.getValue().tick);
				return false;
			}
			if(entry.getValue().tick != this.currentTick-1) {
				return false;
			}
		}
		return true;
	}
	
	public void reSync(int tick) {
		System.out.println("resync");
		Iterator<ServerPlayer> iter = game.getPlayerList().iterator();
		this.currentTick = tick + 1;
		while(iter.hasNext()) {
			short slot = iter.next().getSlotNumber();
			if(!queueMap.get(slot).isEmpty()) {
				System.out.println("slot: " + slot + " tick: " + queueMap.get(slot).peek().tick + " current: " + this.currentTick + " fucked: " + tick);
				while(queueMap.get(slot).peek().tick <= this.currentTick) {
					UnitData message = queueMap.get(slot).poll();
					//System.out.println("got " + message.tick + " tick from player in slot " + slot);
					playerShipMap.get(message.unitKey).direction.set(message.direction.x,message.direction.y);
					playerShipMap.get(message.unitKey).position.set(message.position.x,message.position.y);
					playerShipMap.get(message.unitKey).tick = message.tick;
				}
			}
		}
		sendToAll();
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
							//System.out.println("got " + message.tick + " tick from player in slot " + slot);
							playerShipMap.get(message.unitKey).direction.set(message.direction.x,message.direction.y);
							playerShipMap.get(message.unitKey).position.set(message.position.x,message.position.y);
							playerShipMap.get(message.unitKey).tick = message.tick;
							//System.out.println("current: " + this.currentTick + " slot" + slot + ": " + message.tick);
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
