package com.battleships.base;

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
	private boolean rdyToHalfStep;
	
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

			//send initial direction packet
			playerData.position.set(localPlayerShip.CollisionBody.getPosition());
			lobbyClient.sendDirection(playerData);
	}
	
	public void worldStep(float delta)
	{
		box_accu+=delta;
		stepNow = false;
		
		if(box_accu > BOX_STEP/2f && rdyToHalfStep)
		{
			update();
			rdyToHalfStep = false;
		}
		
		if(box_accu>BOX_STEP)
		{
			try {
				Map<Short, UnitData> message = null;
				for(int i = 0; i < 3; i++) {
					message = msgQueue.poll(31, TimeUnit.MILLISECONDS);
					if(message != null) {
						break;
					}
					lobbyClient.sendDirection(playerData);
					System.out.println(i+1);
				}
				if(message != null) {
					
					//rdyToHalfStep = true;
					
					stepNow = true;
					
		            handleMessage(message);

		        	GLUH.onUpdate(BOX_STEP);
		        	
		        	update();
		        	
		    		physicsWorld.step(BOX_STEP, BOX_VELOCITY_ITERATIONS, BOX_POSITION_ITERATIONS); 
		    		box_accu = 0;
		    		
		    		this.playerData.position.set(localPlayerShip.CollisionBody.getPosition());
		    		this.playerData.direction.set(localPlayerDirection);
		    		this.tick++;
		    		this.playerData.tick = this.tick;
		    		lobbyClient.sendDirection(playerData);
		    		playerData.shopAction=0;
		    		System.out.println("tick on client: " + tick);
		    		
		    		msgQueue.clear();
		    		//System.out.println("CheckRandom: "+Weapon.RNG.nextInt());
		    		Weapon.RNG.setSeed(tick);
				} else {
//					System.out.println("no msg");
					//lobbyClient.sendDirection(playerData);
				}
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}	

}
