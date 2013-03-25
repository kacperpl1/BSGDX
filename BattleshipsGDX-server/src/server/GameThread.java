package server;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import shared.UnitData;
import shared.UnitMap;

class GameThread extends Thread{
	private ServerGame game;
	private volatile boolean running;
	
	static final float BOX_STEP=1f/10f;
    static final int BOX_VELOCITY_ITERATIONS=6;  
    static final int BOX_POSITION_ITERATIONS=2;  
    static final float WORLD_TO_BOX=0.01f;  
    static final float BOX_WORLD_TO=100.0f; 
	
	public UnitMap unitMap = new UnitMap();
	Map<Short, Stack<UnitData>> stackMap = new HashMap<Short, Stack<UnitData>>();
	
	private Map<Short, UnitData> playerShipMap = new HashMap<Short, UnitData>();
	
	public GameThread(ServerGame game){
		this.game = game;
		this.running = true;
		this.setName(game.getId());
		
		for(int i = 0; i < game.getPlayerList().size(); i++) {
			stackMap.put(game.getPlayerList().getServerPlayer(i).getSlotNumber(), new Stack<UnitData>());
			UnitData data = new UnitData();
			data.gameID = this.getName();
			data.unitKey = game.getPlayerList().getServerPlayer(i).getSlotNumber();
			playerShipMap.put(game.getPlayerList().getServerPlayer(i).getSlotNumber(), data);
		}
	}
	
	public void run(){
		System.out.println("game " + game.getName() + " started");
		while(running){
			if(playersAreConnected()) {
				try {
					
					int counter = game.getPlayerList().size();
					
					while(counter > 0) {
						for(int i = 0 ; i < game.getPlayerList().size(); i++) {
							short slot = game.getPlayerList().getServerPlayer(i).getSlotNumber(); 
							if(!stackMap.get(slot).isEmpty()) {
								UnitData message = stackMap.get(slot).pop();
								//System.out.println("got msg from " + slot);
								playerShipMap.get(message.unitKey).direction.set(message.direction.x,message.direction.y);
								playerShipMap.get(message.unitKey).position.set(message.position.x,message.position.y);
								stackMap.get(slot).clear();
								--counter;
							}
						}
					}
					
					for(int i = 0; i < game.getPlayerList().size(); i++) {
						game.getPlayerList().getServerPlayer(i).getConnection().sendTCP(playerShipMap);
					}
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
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
	
	public Stack<UnitData> getMsgStack(short slot) {
		return this.stackMap.get(slot);
		//return this.stackArray.get(slot);
	}
	
	public boolean playersAreConnected() {
		for(int i = 0; i < game.getPlayerList().size(); i++) {
			if(game.getPlayerList().getServerPlayer(i).getConnection().isConnected()) {
				return true;
			}
		}
		return false;
	}	
}
