package com.battleships.base;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;


public strictfp class Cruiser extends Unit {
	
	Vector2 networkPosition = new Vector2(0,0);
	private Body BlockerBody;

	private String lane;
	private Unit Target = null;
	private Vector2 vecToTarget = new Vector2(0,0);
	private PlayerShip lastPlayerAttacker = null;
	private float lastPlayerAttack = 0;
	
	Cruiser(String Team, float InitialX, float InitialY, String Lane)
    {
		super(Team, InitialX, InitialY);
		lane = Lane;
    	baseSprite.setScale(0.75f);
    	MaxHealth = 500;
    	Health = MaxHealth;
    	visor = new Visor(this);
    	gun = new Weapon(this,0);
    	createBlocker();
    	CollisionBody.getFixtureList().get(0).setSensor(true);
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
		BlockerBody.setTransform(CollisionBody.getPosition(), 0);
    	float velocity = DesiredVelocity.len();
    	if(velocity > 1)
    	{
    		CollisionBody.setLinearVelocity(DesiredVelocity.x*moveSpeed/velocity*GameScreen.WORLD_TO_BOX, DesiredVelocity.y*moveSpeed/velocity*GameScreen.WORLD_TO_BOX);
        	setVisualRotation(DesiredVelocity.x, DesiredVelocity.y);
    	}
    	else
    		CollisionBody.setLinearVelocity(0, 0);
	}
	
    void TakeDamage(int Damage, Unit Instigator)
    {
    	if(Instigator instanceof PlayerShip)
    	{
    		lastPlayerAttacker = ((PlayerShip)Instigator);
    		lastPlayerAttack = 1;
    	}

    	if(Health > 0 && Health - Damage <= 0 && lastPlayerAttacker != null)
    	{
    		lastPlayerAttacker.PlayerGold += this.goldworth;
    	}
    	Health -= Damage;
    	
    	if(gun.Enemies.size()==0 && Target == null)
    	{
    		Target = Instigator;
    	}
    }
	
	void Destroy()
	{
		super.Destroy();
		BlockerBody.destroyFixture(BlockerBody.getFixtureList().get(0));
		bodyPool.free(BlockerBody);
	}
	
	void onUpdate(float delta)
    {
		if(lastPlayerAttack>0)
			lastPlayerAttack -= delta;
		else
			lastPlayerAttacker = null;
		
    	gun.onUpdate(delta);
		updateVelocity();

    	if(gun.Enemies.size()>0)
    	{
    		setDesiredVelocity(DesiredVelocity.x/4,DesiredVelocity.y/4);
    	}
    	else if (Target != null)
    	{
    		vecToTarget.set(Target.getX() - this.getX(), Target.getY() - this.getY());
    		if(Target.Health <= 0 || vecToTarget.len() > gun.Range*3)
    		{
    			Target = null;
    		}
    		else
    		{
    			setDesiredVelocity(vecToTarget.x/4,vecToTarget.y/4);
    		}
    	}
    	else
    	{
	    	if(team == "blue")
	    	{
	    		if(lane == "MID")
	    			setDesiredVelocity(0,50);
	    		else if(lane == "LEFT")
	    		{
	    			if(getY()<-192)
	    				setDesiredVelocity(-50,50);
	    			else if(getY()<192)
	    				setDesiredVelocity(0,50);
	    			else
	    				setDesiredVelocity(50,50);
	    		}
	    		else if(lane == "RIGHT")
	    		{
	    			if(getY()<-192)
	    				setDesiredVelocity(50,50);
	    			else if(getY()<192)
	    				setDesiredVelocity(0,50);
	    			else
	    				setDesiredVelocity(-50,50);
	    		}
	    	}
	    	else
	    	{
	    		if(lane == "MID")
	    		{
	    			setDesiredVelocity(0,-50);
	    		}
	    		else if(lane == "LEFT")
	    		{
	    			if(getY()>192)
	    				setDesiredVelocity(-50,-50);
	    			else if(getY()>-192)
	    				setDesiredVelocity(0,-50);
	    			else
	    				setDesiredVelocity(50,-50);
	    		}
	    		else if(lane == "RIGHT")
	    		{
	    			if(getY()>192)
	    				setDesiredVelocity(50,-50);
	    			else if(getY()>-192)
	    				setDesiredVelocity(0,-50);
	    			else
	    				setDesiredVelocity(-50,-50);
	    		}
	    	}
    	}
    }
}
