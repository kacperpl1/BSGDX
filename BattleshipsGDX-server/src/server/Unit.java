package server;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.battleships.base.GameScreen;
import com.battleships.base.Resources;
import com.battleships.base.Visor;
import com.battleships.base.Weapon;

public class Unit {

	String team;
    int Health = 1000;
	int MaxHealth = 1000;
	int moveSpeed = 50;
	int goldworth = 50;
	Body CollisionBody;
	
	Vector2 DesiredVelocity = new Vector2(0,0);
	Weapon gun;
	static BodyPool bodyPool = new BodyPool();
	
	Unit(String Team, float InitialX, float InitialY)
	{
		team = Team;
		createBody(InitialX,InitialY);
	}

	void createBody(float initialX, float initialY)
	{ 
		CollisionBody = bodyPool.obtain();
		CollisionBody.setType(BodyType.DynamicBody);
		CollisionBody.setTransform(initialX*GameScreen.WORLD_TO_BOX,initialY*GameScreen.WORLD_TO_BOX, 0);
		
		CircleShape dynamicCircle = new CircleShape();  
	    dynamicCircle.setRadius(16f*GameScreen.WORLD_TO_BOX);  
	    FixtureDef fixtureDef = new FixtureDef();  
	    fixtureDef.shape = dynamicCircle;  
	    fixtureDef.density = 1.0f;  
	    fixtureDef.friction = 0.0f;  
	    fixtureDef.restitution = 0.0f;
	    CollisionBody.createFixture(fixtureDef);  
	    CollisionBody.getFixtureList().get(0).setUserData(this);
	} 
	
}
