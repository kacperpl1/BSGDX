package com.battleships.base;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;

import com.battleships.network.BSClient;
import com.battleships.network.Player;
import com.battleships.network.UnitMap;

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
		//System.out.println("heheheheh");
		//System.out.println(message.size());
		for(Entry<Short, UnitData> entry : message.entrySet()) {
			System.out.println(entry.getKey());
			if(shipMap.containsKey(entry.getKey())) {
				System.out.println("jest");
				shipMap.get(entry.getKey()).setDesiredVelocity(entry.getValue().direction.x, entry.getValue().direction.y);
				//	unitMap.get(entry.getKey()).updateUnitData(entry.getValue());
			//{
			} else {
			//	unitMap.put(entry.getKey(), Unit.createNewUnit(entry.getValue()));
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
	}
	
	public void worldStep(float delta)
	{
		box_accu+=delta;
		if(box_accu>BOX_STEP)
		{
			try {
				if(!msgQueue.isEmpty()) {
					Map<Short, UnitData> message = msgQueue.take();
	                handleMessage(message);
				}
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
			physicsWorld.step(BOX_STEP, BOX_VELOCITY_ITERATIONS, BOX_POSITION_ITERATIONS); 
			box_accu-=BOX_STEP;
		}
//			this.playerData.position.set(localPlayerShip.CollisionBody.getPosition());
//			lobbyClient.move(playerData);

			//localPlayerShip.setDesiredVelocity(localPlayerDirection.x, localPlayerDirection.y);
			this.playerData.direction.set(localPlayerDirection);
			lobbyClient.sendDirection(playerData);
	}	

}
