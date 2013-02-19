package com.battleships.base;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class Visor {
	Unit Owner;
	Sprite visionArea;
	private Fixture SensorFixture;
	static LinkedList<Visor> VisorList = new LinkedList<Visor>();
	public static TextureRegion VisorTextureRegion;
	
	
	public Visor(Unit o)
	{
		Owner = o;
		if(Owner.team == BaseGame.LocalPlayerTeam)
		{
			visionArea = new Sprite(VisorTextureRegion);
			if(Owner instanceof Cruiser)
				visionArea.setScale(0.75f);
				
			VisorList.add(this);
			createVisor();
		}

	}
	void createVisor()
	{
		CircleShape dynamicCircle = new CircleShape();  
        dynamicCircle.setRadius(256*visionArea.getScaleX()*0.9f);  
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
	
	public void destroy()
	{	
		if(Owner.team == BaseGame.LocalPlayerTeam)
		{
			VisorList.remove(this);
			SensorFixture.setUserData(null);
			Owner.CollisionBody.destroyFixture(SensorFixture);
		}
	}
	public void draw(SpriteBatch batch) {
		batch.draw(visionArea, -BaseGame.camera.position.x+Owner.CollisionBody.getPosition().x-256*visionArea.getScaleX(),
				-BaseGame.camera.position.y+Owner.CollisionBody.getPosition().y-768*visionArea.getScaleX(), 
				1024*visionArea.getScaleX(), 1024*visionArea.getScaleX());
	}
}
