package com.battleships.base;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;

import com.battleships.network.BSClient;
import com.battleships.network.UnitMap;

public class NetworkGameScreen extends GameScreen{
	private BSClient lobbyClient;
	private BlockingQueue<UnitMap> msgQueue;
	static Map<Integer, Unit> unitMap = new HashMap<Integer, Unit>();
	private UnitData playerData = new UnitData();
	//private UnitMap playerData = new UnitMap();
	
	public NetworkGameScreen()
	{
		super();
		lobbyClient = BSClient.getInstance();
		msgQueue = lobbyClient.getMainGameQueue();
	}
	
	public void handleMessage(UnitMap message) {
		for(Entry<Integer, UnitData> entry : message.map.entrySet()) {
			if(unitMap.containsKey(entry.getKey())) {
				//if(!entry.getKey().equals(this.unitHash)) {
					unitMap.get(entry.getKey()).updateUnitData(entry.getValue());
			//{
			} else {
				unitMap.put(entry.getKey(), Unit.createNewUnit(entry.getValue()));
			}
		}
	}
	
	public void loadPlayers()
	{
			if(lobbyClient.getPlayer().getSlotNumber() < 3) {
				LocalPlayerTeam = "red";
			} else {
				LocalPlayerTeam = "blue";
			}
			
			UnitMap message = null;
			try {
	            message = msgQueue.take();
	            handleMessage(message);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
			
			for(Entry<Integer, UnitData> entry : message.map.entrySet()) {
				if(entry.getValue().slot == lobbyClient.getPlayer().getSlotNumber()) {
					localPlayerShip = (PlayerShip) unitMap.get(entry.getKey());
					playerData = entry.getValue();
					break;
				}
			}
	}
	
	public void worldStep(float delta)
	{
		box_accu+=delta;
		if(box_accu>BOX_STEP)
		{
			try {
				if(!msgQueue.isEmpty()) {
	                UnitMap message = msgQueue.take();
	                handleMessage(message);
				}
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
			physicsWorld.step(BOX_STEP, BOX_VELOCITY_ITERATIONS, BOX_POSITION_ITERATIONS); 
			box_accu-=BOX_STEP;
		}
			this.playerData.position.set(localPlayerShip.CollisionBody.getPosition());
			lobbyClient.move(playerData);
	}	

}
