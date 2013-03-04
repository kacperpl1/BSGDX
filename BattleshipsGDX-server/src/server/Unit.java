package server;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.battleships.base.Weapon;

public class Unit {
	
	GameThread ownerThread;
	BodyPool bodyPool;

	String team;
    int Health = 1000;
	int MaxHealth = 1000;
	int moveSpeed = 50;
	int goldworth = 50;
	Body CollisionBody;
	
	Vector2 DesiredVelocity = new Vector2(0,0);
	Weapon gun;
	
	Unit(String Team, float InitialX, float InitialY, GameThread owner)
	{
		team = Team;
		ownerThread = owner;
		bodyPool = new BodyPool(ownerThread);
		createBody(InitialX,InitialY);
	}

	void createBody(float initialX, float initialY)
	{ 
		CollisionBody = bodyPool.obtain();
		CollisionBody.setType(BodyType.DynamicBody);
		CollisionBody.setTransform(initialX*GameThread.WORLD_TO_BOX,initialY*GameThread.WORLD_TO_BOX, 0);
		
		CircleShape dynamicCircle = new CircleShape();  
	    dynamicCircle.setRadius(16f*GameThread.WORLD_TO_BOX);  
	    FixtureDef fixtureDef = new FixtureDef();  
	    fixtureDef.shape = dynamicCircle;  
	    fixtureDef.density = 1.0f;  
	    fixtureDef.friction = 0.0f;  
	    fixtureDef.restitution = 0.0f;
	    CollisionBody.createFixture(fixtureDef);  
	    CollisionBody.getFixtureList().get(0).setUserData(this);
	} 	
	
	void onUpdate(float delta)
	{
		updateVelocity();
    	if(Health<=0)
    	{
        	int fixtures = CollisionBody.getFixtureList().size();
    		for(int i=0; i<fixtures; i++)
    		{
    			CollisionBody.destroyFixture(CollisionBody.getFixtureList().get(0));
    		}
    		bodyPool.free(CollisionBody);   	
        	return;
    	}
		//gun.onUpdate(delta);
	}
	
    void updateVelocity()
    {
    	float velocity = DesiredVelocity.len();
    	if(velocity > 0)
    	{
    		CollisionBody.setLinearVelocity(DesiredVelocity.x*moveSpeed/velocity*GameThread.WORLD_TO_BOX, DesiredVelocity.y*moveSpeed/velocity*GameThread.WORLD_TO_BOX);
    	}
    	else
    		CollisionBody.setLinearVelocity(0, 0);
    }
}
