package com.battleships.base;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;


public class Cruiser extends Unit {
	
	Vector2 networkPosition = new Vector2(0,0);
	private Body BlockerBody;
	
	Cruiser(String Team, float InitialX, float InitialY)
    {
		super(Team, InitialX, InitialY);
    	baseSprite.setScale(0.75f);
    	MaxHealth = 100;
    	Health = MaxHealth;
    	visor = new Visor(this);
    	gun = new Weapon(this,0);
    	CollisionBody.getFixtureList().get(0).setSensor(true);
    	createBlocker();
    }	
	
	void createBlocker()
	{ 
		BlockerBody = bodyPool.obtain();
		BlockerBody.setType(BodyType.StaticBody);
		BlockerBody.setTransform(CollisionBody.getPosition(), 0);
		
		CircleShape dynamicCircle = new CircleShape();  
        dynamicCircle.setRadius(16f*GameScreen.WORLD_TO_BOX);  
        FixtureDef fixtureDef = new FixtureDef();  
        fixtureDef.shape = dynamicCircle;  
        fixtureDef.density = 1.0f;  
        fixtureDef.friction = 0.0f;  
        fixtureDef.restitution = 0.0f;
        BlockerBody.createFixture(fixtureDef);  
	} 
	
	void updateVelocity(){
		BlockerBody.setTransform(this.getX()*GameScreen.WORLD_TO_BOX,this.getY()*GameScreen.WORLD_TO_BOX,0);
	}
	
//	void updateUnitData(UnitData data)
//	{
//		CollisionBody.setTransform(data.position, 0);
//		//Health = data.health;
//		
//		if(Math.abs(data.position.x - networkPosition.x) > GameScreen.WORLD_TO_BOX || Math.abs(data.position.y - networkPosition.y) > GameScreen.WORLD_TO_BOX)
//			setVisualRotation(data.position.x - networkPosition.x, data.position.y - networkPosition.y);
//		
//		networkPosition.set(data.position);
//	}
	
	void onUpdate(float delta)
    {
    	if(Health<=0)
    	{
    		Health=0;
    		Destroy();
    		BlockerBody.destroyFixture(BlockerBody.getFixtureList().get(0));
    		bodyPool.free(BlockerBody);
    		return;
    	}
    	gun.onUpdate(delta);
    }
}
