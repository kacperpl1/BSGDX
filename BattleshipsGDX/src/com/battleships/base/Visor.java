package com.battleships.base;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class Visor {
	Unit Owner;
	Sprite visionArea;
	private Fixture SensorFixture;
	static LinkedList<Visor> VisorList = new LinkedList<Visor>();	
	
	public Visor(Unit o)
	{
		Owner = o;
		if(Owner.team == GameScreen.LocalPlayerTeam)
		{
			visionArea = new Sprite(Resources.VisorTextureRegion);
			if(Owner instanceof Cruiser)
				visionArea.setScale(0.75f);
				
			VisorList.add(this);
			createVisor();
		}

	}
	void createVisor()
	{
		CircleShape dynamicCircle = new CircleShape();  
        dynamicCircle.setRadius(256*visionArea.getScaleX()*0.9f*GameScreen.WORLD_TO_BOX);  
        FixtureDef fixtureDef = new FixtureDef();  
        fixtureDef.shape = dynamicCircle;  
        fixtureDef.density = 1.0f;  
        fixtureDef.friction = 0.0f;  
        fixtureDef.restitution = 0.0f;
        fixtureDef.isSensor = true;
		
	    SensorFixture = Owner.CollisionBody.createFixture(fixtureDef);
	    SensorFixture.setUserData(this);
	    SensorFixture.setSensor(true);
	}
	
	public void checkDestroy()
	{	
		if(Owner.Health <=0)
		{
			VisorList.remove(this);
		}
	}
	public void draw(SpriteBatch batch) {
		if(Owner.Health>0)
			batch.draw(visionArea, -GameScreen.camera.position.x+Owner.getX()-256*visionArea.getScaleX(),
				-GameScreen.camera.position.y+Owner.getY()-768*visionArea.getScaleX(), 
				1024*visionArea.getScaleX(), 1024*visionArea.getScaleX());
	}
}
