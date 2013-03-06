package server;


public class GameLoopUpdateHandler{
	
	private float WaveDelay;
	private float WaveCounter;
	static float FrameDelay = 1f/15f;
	private GameThread ownerThread;
	
	public GameLoopUpdateHandler(GameThread owner)
	{
		ownerThread = owner;
		WaveDelay = 30;
		WaveCounter = 20;
	}
	
	public void SpawnWave()
	{

		new Cruiser("red",-32, 512,"MID", ownerThread);
		new Cruiser("red",0, 480,"MID", ownerThread);
		new Cruiser("red",32, 512,"MID", ownerThread);
		
		new Cruiser("red",-256, 670,"LEFT", ownerThread);
		new Cruiser("red",-288, 700,"LEFT", ownerThread);
		new Cruiser("red",-256, 700,"LEFT", ownerThread);
		
		new Cruiser("red",256, 670,"RIGHT", ownerThread);
		new Cruiser("red",288, 700,"RIGHT", ownerThread);
		new Cruiser("red",256, 700,"RIGHT", ownerThread);

		new Cruiser("blue",-32, -512,"MID", ownerThread);
		new Cruiser("blue",0, -480,"MID", ownerThread);
		new Cruiser("blue",32, -512,"MID", ownerThread);
		
		new Cruiser("blue",-256, -670,"LEFT", ownerThread);
		new Cruiser("blue",-288, -700,"LEFT", ownerThread);
		new Cruiser("blue",-256, -700,"LEFT", ownerThread);
		
		new Cruiser("blue",256, -670,"RIGHT", ownerThread);
		new Cruiser("blue",288, -700,"RIGHT", ownerThread);
		new Cruiser("blue",256, -700,"RIGHT", ownerThread);
		
	}

	public void onUpdate(float pSecondsElapsed) {
		WaveCounter += pSecondsElapsed;
		if(WaveCounter>WaveDelay)
		{
			WaveCounter=0;
			SpawnWave();
		}
	}
}
