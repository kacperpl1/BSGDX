package com.battleships.base;

import java.util.Iterator;

public class GameLoopUpdateHandler{
	
	private float WaveDelay;
	private float WaveCounter;
	
	public GameLoopUpdateHandler()
	{
		WaveDelay = 30;
		WaveCounter = 25;
	}
	
	public void SpawnWave()
	{

		new Cruiser("red",-32, 512,"MID");
		new Cruiser("red",0, 480,"MID");
		new Cruiser("red",32, 512,"MID");
		
		new Cruiser("red",-256, 670,"LEFT");
		new Cruiser("red",-288, 700,"LEFT");
		new Cruiser("red",-256, 700,"LEFT");
		
		new Cruiser("red",256, 670,"RIGHT");
		new Cruiser("red",288, 700,"RIGHT");
		new Cruiser("red",256, 700,"RIGHT");

		new Cruiser("blue",-32, -512,"MID");
		new Cruiser("blue",0, -480,"MID");
		new Cruiser("blue",32, -512,"MID");
		
		new Cruiser("blue",-256, -670,"LEFT");
		new Cruiser("blue",-288, -700,"LEFT");
		new Cruiser("blue",-256, -700,"LEFT");
		
		new Cruiser("blue",256, -670,"RIGHT");
		new Cruiser("blue",288, -700,"RIGHT");
		new Cruiser("blue",256, -700,"RIGHT");
		
	}

	public void onUpdate(float pSecondsElapsed) {
		WaveCounter += pSecondsElapsed;
		if(WaveCounter>WaveDelay)
		{
			Visor current;
			Iterator<Visor> iter = Visor.VisorList.iterator();
			while(iter.hasNext())
			{
				current = iter.next();
				if(current.Owner.Health<=0 && !(current.Owner instanceof PlayerShip))
					iter.remove();
			}
			WaveCounter=0;
			SpawnWave();
		}
	}
}
