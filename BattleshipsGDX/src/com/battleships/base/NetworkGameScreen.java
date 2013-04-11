package com.battleships.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

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
	
	public NetworkGameScreen()
	{
		super();
	}
	
	public void updatePlayers(Map<Short, UnitData> message) {
		for(Entry<Short, UnitData> entry : message.entrySet()) {
			if(shipMap.containsKey(entry.getKey())) {
				shipMap.get(entry.getKey()).setDesiredVelocity(entry.getValue().direction.x, entry.getValue().direction.y);
				System.out.println("DesiredVelocity "+entry.getValue().direction);
						//+ "\nCurrentVelocity "+shipMap.get(entry.getKey()).CurrentVelocity
						//+ "\nCurrentPosition "+shipMap.get(entry.getKey()).CollisionBody.getPosition());
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
		playerData.position.set(localPlayerShip.CollisionBody.getPosition());
		lobbyClient.sendDirection(playerData);
		
		serverDataBuffer = new HashMap<Integer, Map<Short, UnitData>>();
		playerDataBuffer = new ArrayList<UnitData>();
		UnitData auxData = new UnitData();
    	auxData.position.set(localPlayerShip.CollisionBody.getPosition());
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
	                	for(int i = 0; i < 10; i++) {
	                		message = msgQueue.poll(10, TimeUnit.MILLISECONDS);
	                		if(message != null) {
	                			serverTick = message.get(slot).tick;
	                			//System.out.println("got " + serverTick + " from server");
	                			synchronized(serverDataBuffer) {
	                				serverDataBuffer.put(serverTick, message);
	                			}
	                			break;
	                		} else {
	                			if(playerDataBuffer.size() > serverTick+1) {
	                				//System.out.println("send " + (serverTick+1) + " " + playerDataBuffer.get(serverTick+1).tick);
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
				box_accu = 0;
		    	
				synchronized(playerDataBuffer) {
			    	this.playerData.position.set(localPlayerShip.CollisionBody.getPosition());
			    	this.playerData.direction.set(localPlayerDirection);
			    	this.playerData.tick = this.tick + 1; 
			    	UnitData auxData = new UnitData();
			    	auxData.position.set(this.playerData.position);
			    	auxData.direction.set(localPlayerDirection);
			    	auxData.tick = this.tick + 1;
			    	auxData.shopAction = this.playerData.shopAction;
			    	auxData.gameID = this.playerData.gameID;
			    	auxData.unitKey = this.playerData.unitKey;
			    	this.playerDataBuffer.add(auxData);
			    	this.playerData.shopAction=0;
		    	}
				this.tick++;
		    	//System.out.println("#tick on client: " + tick);
		    	
		    	Weapon.RNG.setSeed(tick);
			} else
			if(this.serverDataBuffer.get(tick-2) != null) {
				
				stepNow = true;
				synchronized(serverDataBuffer){
			        updatePlayers(this.serverDataBuffer.get(tick-2));
			        System.out.println("render tick: " + this.serverDataBuffer.get(tick-2).get(slot).tick);
			        this.serverDataBuffer.remove(tick-2);
				}
				GLUH.onUpdate(BOX_STEP);
				
				physicsWorld.step(BOX_STEP, BOX_VELOCITY_ITERATIONS, BOX_POSITION_ITERATIONS); 
				
				box_accu = 0;
				synchronized(this.playerDataBuffer) {
			    	this.playerData.position.set(localPlayerShip.CollisionBody.getPosition());
			    	this.playerData.direction.set(localPlayerDirection);
			    	this.playerData.tick = this.tick + 1;
			    	UnitData auxData = new UnitData();
			    	auxData.position.set(this.playerData.position);
			    	auxData.direction.set(localPlayerDirection);
			    	auxData.tick = this.tick + 1;
			    	auxData.shopAction = this.playerData.shopAction;
			    	auxData.gameID = this.playerData.gameID;
			    	auxData.unitKey = this.playerData.unitKey;
			    	this.playerDataBuffer.add(auxData);
			    	this.playerData.shopAction=0;
				}
				this.tick++;
				//System.out.println("tick on client: " + tick);
				
				Weapon.RNG.setSeed(tick);
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
