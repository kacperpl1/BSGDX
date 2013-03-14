package server;


import shared.Common;
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
    short Health = 1000;
	short MaxHealth = 1000;
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
	
	void createUnitData(short unitType, Object... shipData)
	{
		UnitData uData = new UnitData();
		uData.position = CollisionBody.getPosition();
		uData.health = Health;
		uData.type = unitType;
		if(unitType == Common.PLAYER_SHIP) {
			uData.slot = (short) shipData[0];
			uData.gameID = (String) shipData[1];
			uData.unitKey = hashCode();
		}
		ownerThread.unitMap.map.put(hashCode(), uData);	
	}
	
	void updateUnitData()
	{
		UnitData uData = ownerThread.unitMap.map.get(hashCode());
		uData.position = CollisionBody.getPosition();
		uData.health = Health;
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
	    CollisionBody.setUserData(this);
	} 	
	
	float getX()
	{
		return CollisionBody.getPosition().x*GameThread.BOX_WORLD_TO;
	}
	
	float getY()
	{
		return CollisionBody.getPosition().y*GameThread.BOX_WORLD_TO;
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
		CollisionBody.setUserData(null);
		bodyPool.free(CollisionBody); 
		ownerThread.unitMap.map.remove(hashCode());
    }
    
	void onUpdate(float delta)
	{
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
