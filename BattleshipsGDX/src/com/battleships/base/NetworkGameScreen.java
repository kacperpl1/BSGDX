package com.battleships.base;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;

import com.battleships.network.BSClient;
import com.battleships.network.Player;

public class NetworkGameScreen extends GameScreen{
	private BSClient lobbyClient;
	private BlockingQueue<Map<Short, UnitData>> msgQueue;
	private UnitData playerData;// = new UnitData();
	private Map<Short, PlayerShip> shipMap; // = new HashMap<Integer, PlayerShip>();
	//private UnitMap playerData = new UnitMap();
	
	public NetworkGameScreen()
	{
		super();
		
	}
	
	public void handleMessage(Map<Short, UnitData> message) {
		for(Entry<Short, UnitData> entry : message.entrySet()) {
			if(shipMap.containsKey(entry.getKey())) {
				if(shipMap.get(entry.getKey()) != localPlayerShip)
					shipMap.get(entry.getKey()).CollisionBody.setTransform(entry.getValue().position.x,entry.getValue().position.y,0);
				
				shipMap.get(entry.getKey()).setDesiredVelocity(entry.getValue().direction.x, entry.getValue().direction.y);
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
			if(lobbyClient.getPlayer().getSlotNumber() < 3) {
				LocalPlayerTeam = "red";
			} else {
				LocalPlayerTeam = "blue";
			}
			
			lobbyClient.getGame().getPlayerList();
			
			
			for(Player player : lobbyClient.getGame().getPlayerList() ) {
				if(player.getSlotNumber() < 3) {
					shipMap.put((short)player.getSlotNumber(), new PlayerShip("red", 0, 768, player.getSlotNumber()));
				} else {
					shipMap.put((short)player.getSlotNumber(), new PlayerShip("blue", 0, -768, player.getSlotNumber()));
				}
			}
			localPlayerShip = shipMap.get((short)lobbyClient.getPlayer().getSlotNumber());
			//localPlayerShip = shipMap.get((short)3);

			//send initial direction packet
			playerData.position.set(localPlayerShip.CollisionBody.getPosition());
			lobbyClient.sendDirection(playerData);
	}
	
	public void worldStep(float delta)
	{
		box_accu+=delta;
		
		if(box_accu - delta <BOX_STEP/2f && box_accu > BOX_STEP/2f)
			update();
		
		if(box_accu>BOX_STEP)
		{
			try {
				if(!msgQueue.isEmpty()) {
					Map<Short, UnitData> message = msgQueue.take();
	                handleMessage(message);

	        		GLUH.onUpdate(BOX_STEP);
	        		
	        		update();
	        		
	    			physicsWorld.step(BOX_STEP, BOX_VELOCITY_ITERATIONS, BOX_POSITION_ITERATIONS); 
	    			box_accu = 0;
	    			
	    			this.playerData.position.set(localPlayerShip.CollisionBody.getPosition());
	    			this.playerData.direction.set(localPlayerDirection);
	    			lobbyClient.sendDirection(playerData);
	    			
	    			msgQueue.clear();
	    			Weapon.m_w = 1182370352;
	    			Weapon.m_z = 1352118237;
				}
				else
				{
					System.out.println("No messages");
	    			lobbyClient.sendDirection(playerData);
				}
            } catch (InterruptedException e) {
                //e.printStackTrace();
            	System.out.println("Connection Error!");
            }
		}
	}	

}
