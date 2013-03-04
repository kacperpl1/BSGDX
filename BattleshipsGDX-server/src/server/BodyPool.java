package server;


import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Pool;

public class BodyPool extends Pool<Body>{

	GameThread ownerThread;
	
	public BodyPool(GameThread owner)
	{
		ownerThread = owner;
	}
	
	@Override
	protected Body newObject() {
		BodyDef bodyDef = new BodyDef();  
        bodyDef.type = BodyType.DynamicBody;
		return ownerThread.physicsWorld.createBody(bodyDef);
	}

}
