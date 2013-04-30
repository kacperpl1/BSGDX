package com.battleships.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.math.Vector2;
import com.battleships.network.BSClient;
import com.battleships.network.Player;

public class NetworkGameScreen extends GameScreen{
	private BSClient lobbyClient;
	private BlockingQueue<Map<Short, UnitData>> msgQueue;
	protected UnitData playerData;
	private Map<Short, PlayerShip> shipMap;
	private int tick = 0;
	private volatile int serverTick = 0;
	private Thread messenger;
	private Map<Integer, Map<Short, UnitData>> serverDataBuffer;
	private ArrayList<UnitData> playerDataBuffer;
	private short slot;
	Vector2 tempDirection = new Vector2();
	
	public NetworkGameScreen()
	{
		super();
	}
	
	public void updatePlayers(Map<Short, UnitData> message) {
		for(Entry<Short, UnitData> entry : message.entrySet()) {
			if(shipMap.containsKey(entry.getKey())) {
				if(shipMap.get(entry.getKey()) != localPlayerShip)
					shipMap.get(entry.getKey()).setDesiredVelocity(entry.getValue().direction);
				if(entry.getValue().shopAction>0)
					shipMap.get(entry.getKey()).buyItem(entry.getValue().shopAction-1);
				else if(entry.getValue().shopAction<0)
					shipMap.get(entry.getKey()).sellItem(-entry.getValue().shopAction-1);
			}
		}
	}
	
	public void loadPlayers()
	{
		shipMap = new HashMap<Short, PlayerShip>();
		playerData = new UnitData();
		lobbyClient = BSClient.getInstance();
		msgQueue = lobbyClient.getMainGameQueue();
		this.playerData.gameID = lobbyClient.getGame().getId();
		this.playerData.unitKey = (short) lobbyClient.getPlayer().getSlotNumber();
		this.slot = (short) lobbyClient.getPlayer().getSlotNumber();
		if(lobbyClient.getPlayer().getSlotNumber() < 3) {
			LocalPlayerTeam = "red";
		} else {
			LocalPlayerTeam = "blue";
		}
		
		lobbyClient.getGame().getPlayerList();
		
		
		for(Player player : lobbyClient.getGame().getPlayerList() ) {
			if(player.getSlotNumber() < 3) {
				shipMap.put((short)player.getSlotNumber(), new PlayerShip("red", -64 + 64*player.getSlotNumber(), 768, player.getSlotNumber()));
			} else {
				shipMap.put((short)player.getSlotNumber(), new PlayerShip("blue", -256 + 64*player.getSlotNumber(), -768, player.getSlotNumber()));
			}
		}
		localPlayerShip = shipMap.get((short)lobbyClient.getPlayer().getSlotNumber());
			
		//send initial direction packet
		lobbyClient.sendDirection(playerData);
		
		serverDataBuffer = new HashMap<Integer, Map<Short, UnitData>>();
		playerDataBuffer = new ArrayList<UnitData>();
		UnitData auxData = new UnitData();
    	auxData.tick = this.tick;
    	auxData.shopAction = 0;
    	auxData.gameID = this.playerData.gameID;
    	auxData.unitKey = this.playerData.unitKey;
		this.playerDataBuffer.add(auxData);
		
		this.messenger = new Thread(new Runnable() {
	        @Override
	        public void run() {
	            while (true) {
	                try {
	                	Map<Short, UnitData> message = null;
	                	for(int i = 0; i < 5; i++) {
	                		message = msgQueue.poll(50, TimeUnit.MILLISECONDS);
	                		if(message != null) {
	                			serverTick = message.get(slot).tick;
	                			synchronized(serverDataBuffer) {
	                				serverDataBuffer.put(serverTick, message);
	                			}
	                			break;
	                		} else {
	                			if(playerDataBuffer.size() > serverTick+1) {
	                				System.out.println("send " + (serverTick+1));
			        				synchronized(playerDataBuffer) {
			        					lobbyClient.sendDirection(playerDataBuffer.get(serverTick+1));
			        				}
	                			}
	                		}
	                		
	                	}
	                	
	                } catch (InterruptedException e) {
	                    //System.out.println("Messanger interrupted");
	                }
	            }
	        }
	    }, "Messanger");
		this.messenger.start();
	}
	
	public void worldStep(float delta)
	{
		box_accu+=delta;
		stepNow = false;
		
		if(box_accu>BOX_STEP) {
			if(tick < 2) {
				//box_accu -= BOX_STEP;
				box_accu = 0;
		    	
				synchronized(playerDataBuffer) {
			    	this.playerData.tick = this.tick + 1; 
			    	UnitData auxData = new UnitData();
			    	auxData.tick = this.playerData.tick;
			    	auxData.gameID = this.playerData.gameID;
			    	auxData.unitKey = this.playerData.unitKey;
			    	this.playerDataBuffer.add(auxData);
		    	}
				this.tick++;
			} else
			if(this.serverDataBuffer.get(tick-1) != null) {
				
				stepNow = true;
				synchronized(serverDataBuffer){
			        updatePlayers(this.serverDataBuffer.get(tick-1));
			        this.serverDataBuffer.remove(tick-1);
				}
				GLUH.onUpdate(BOX_STEP);
				
				physicsWorld.step(BOX_STEP, BOX_VELOCITY_ITERATIONS, BOX_POSITION_ITERATIONS); 
				
				//box_accu -= BOX_STEP;
				box_accu = 0;
				
				synchronized(this.playerDataBuffer) {
			    	this.playerData.direction.set(localPlayerDirection);
			    	this.playerData.tick = this.tick + 1;
			    	UnitData auxData = new UnitData();
			    	auxData.direction.set(this.playerData.direction);
			    	auxData.tick = this.playerData.tick;
			    	auxData.shopAction = this.playerData.shopAction;
			    	auxData.gameID = this.playerData.gameID;
			    	auxData.unitKey = this.playerData.unitKey;
			    	this.playerDataBuffer.add(auxData);
			    	this.playerData.shopAction=0;
				}
				this.tick++;
		    	
		    	//System.out.println("checksum: "+Unit.checksum + " checkHealth: "+Unit.checkHealth + " RNG: " + Weapon.RNG.nextInt());
		    	
				Weapon.RNG.setSeed(tick);
				
		    	localPlayerShip.setDesiredVelocity(tempDirection);
		    	tempDirection.set(localPlayerDirection);
		    	//Unit.checksum=0;
		    	//Unit.checkHealth=0;
			}
			else
			{
				System.out.println("tick: "+tick+" ERROR");
			}
		}
	}	
	public void dispose() {
		super.dispose();
		this.lobbyClient.stopGame();
		this.lobbyClient.destroy();
		this.messenger.interrupt();
	}

}
