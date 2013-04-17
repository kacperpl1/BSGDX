package server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;

import shared.UnitData;
import shared.UnitMap;

public class GameThread extends Thread{
	private ServerGame game;
	private int currentTick = 1;
	
	public UnitMap unitMap = new UnitMap();
	Map<Short, LinkedBlockingQueue<UnitData>> queueMap = new HashMap<Short, LinkedBlockingQueue<UnitData>>();
	
	private Map<Short, UnitData> playerShipMap = new HashMap<Short, UnitData>();
	private Map<Short, UnitData> auxShipMap = new HashMap<Short, UnitData>();
	private Map<Short, Integer> counterMap = new HashMap<Short, Integer>();
	
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
			auxShipMap.put(player.getSlotNumber(), data);
			counterMap.put(player.getSlotNumber(), 0);
		}
	}
	
	public boolean allSync() {
		for(Entry<Short, UnitData> entry : playerShipMap.entrySet()) {
			if(entry.getValue().tick != this.currentTick-1) {
				return false;
			}
		}
		for(Entry<Short, UnitData> entry : playerShipMap.entrySet()) {
			UnitData auxData = new UnitData();
			auxData.direction.set(entry.getValue().direction);
			auxData.tick = entry.getValue().tick;
			auxData.shopAction = entry.getValue().shopAction;
			auxShipMap.put(entry.getKey(), auxData);
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
		if(playerShipMap.get(slot).tick == currentTick-2) {
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
		game.dropPlayer(slot);
		this.playerShipMap.remove(slot);
		System.out.println("player " + slot + " dropped");
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
	
	public void checkDisc() {
		Iterator<ServerPlayer> iter = game.getPlayerList().iterator();
		while(iter.hasNext()) {
			ServerPlayer aux = iter.next();
			if(!aux.getConnection().isConnected()) {
				dropPlayer(aux.getSlotNumber());
			}
		}
	}
	
	public void run(){
		System.out.println("game " + game.getName() + " started");
		while(playersAreConnected()){
			checkDisc();
			Iterator<ServerPlayer> iter = game.getPlayerList().iterator();
			while(iter.hasNext()) {
			  short slot = iter.next().getSlotNumber();
			  if(!isSync(slot)) {
				  if(!queueMap.get(slot).isEmpty()) {
				    if(nextTick(slot)) {
				      UnitData message = queueMap.get(slot).poll();
				      if(message.tick == playerShipMap.get(slot).tick) {
							counterMap.put(slot, counterMap.get(slot) + 1);
							if(counterMap.get(slot) > 3) {
								System.out.println("resend tick " + message.tick + " to " + slot);
								game.getPlayerBySlot(slot).getConnection().sendUDP(auxShipMap);
								counterMap.put(slot, 0);
							}
						} else {
							if(message.tick == this.currentTick-1) {
								counterMap.put(slot, 0);
								playerShipMap.get(slot).direction.set(message.direction.x,message.direction.y);
								playerShipMap.get(slot).tick = message.tick;
								playerShipMap.get(slot).shopAction = message.shopAction;
							}
						}
				    }
				  }
			  }
			}
				
			if(allSync()) {
				//System.out.println("all sync " + (this.currentTick - 1));
				sendToAll();
				this.currentTick++;
			}
		}
		System.out.println("game " + game.getName() + " stopped");
	}
	
	public LinkedBlockingQueue<UnitData> getMsgQueue(short slot) {
		return this.queueMap.get(slot);
	}
	
}
