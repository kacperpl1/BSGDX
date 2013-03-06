package server;


import shared.UnitData;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public abstract class Unit {
	
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
	
	void createUnitData(UnitData.Type unitType)
	{
		UnitData uData = new UnitData();
		uData.position = CollisionBody.getPosition();
		uData.velocity = CollisionBody.getLinearVelocity();
		uData.health = Health;
		uData.type = unitType;
		ownerThread.unitMap.map.put(hashCode(), uData);		
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
	
	float getX()
	{
		return CollisionBody.getPosition().x*GameThread.WORLD_TO_BOX;
	}
	
	float getY()
	{
		return CollisionBody.getPosition().y*GameThread.WORLD_TO_BOX;
	}	
	
    void TakeDamage(int Damage, Unit Instigator)
    {
    	Health -= Damage;
    }
    
    void Destroy()
    {
		// TODO Handle unit removal and gold from frag
    	int fixtures = CollisionBody.getFixtureList().size();
		for(int i=0; i<fixtures; i++)
		{
			CollisionBody.destroyFixture(CollisionBody.getFixtureList().get(0));
		}
		bodyPool.free(CollisionBody); 
    }
    
	void onUpdate(float delta)
	{
    	if(Health <=0)
    	{
    		Health = 0;
    		Destroy();
    		return;
    	}
		updateVelocity();
		gun.onUpdate(delta);
	}
	
    void setDesiredVelocity(float X, float Y)
    {
    	DesiredVelocity.x = X;
    	DesiredVelocity.y = Y;
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
