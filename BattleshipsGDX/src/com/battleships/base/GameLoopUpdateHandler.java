package com.battleships.base;

import java.util.Iterator;

public class GameLoopUpdateHandler{
	
	private float WaveDelay;
	private float WaveCounter;
	static float FrameDelay = 1f/15f;
	
	public GameLoopUpdateHandler()
	{
		WaveDelay = 30;
		WaveCounter = 20;
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
		}
	}
}
