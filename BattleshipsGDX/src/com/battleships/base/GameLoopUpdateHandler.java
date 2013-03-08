package com.battleships.base;

import java.util.Iterator;

public class GameLoopUpdateHandler{
	
	private float WaveDelay;
	private float WaveCounter;
	static float FrameDelay = 1f/15f;
	
	public GameLoopUpdateHandler()
	{
		WaveDelay = 30;
		WaveCounter = 25;
	}
	
	public void SpawnWave()
	{

		new PracticeCruiser("red",-32, 512,"MID");
		new PracticeCruiser("red",0, 480,"MID");
		new PracticeCruiser("red",32, 512,"MID");
		
		new PracticeCruiser("red",-256, 670,"LEFT");
		new PracticeCruiser("red",-288, 700,"LEFT");
		new PracticeCruiser("red",-256, 700,"LEFT");
		
		new PracticeCruiser("red",256, 670,"RIGHT");
		new PracticeCruiser("red",288, 700,"RIGHT");
		new PracticeCruiser("red",256, 700,"RIGHT");

		new PracticeCruiser("blue",-32, -512,"MID");
		new PracticeCruiser("blue",0, -480,"MID");
		new PracticeCruiser("blue",32, -512,"MID");
		
		new PracticeCruiser("blue",-256, -670,"LEFT");
		new PracticeCruiser("blue",-288, -700,"LEFT");
		new PracticeCruiser("blue",-256, -700,"LEFT");
		
		new PracticeCruiser("blue",256, -670,"RIGHT");
		new PracticeCruiser("blue",288, -700,"RIGHT");
		new PracticeCruiser("blue",256, -700,"RIGHT");
		
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
				if(current.Owner.Health<=0)
					iter.remove();
			}
			WaveCounter=0;
			if(GameScreen.test_mode)
				SpawnWave();
		}
	}
}
