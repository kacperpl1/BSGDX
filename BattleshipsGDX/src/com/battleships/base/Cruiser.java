package com.battleships.base;

import com.badlogic.gdx.math.Vector2;


public strictfp class Cruiser extends Unit {
	
	Vector2 networkPosition = new Vector2(0,0);

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
    }	
	
	void updateVelocity(){
    	float velocity = DesiredVelocity.len();
    	if(velocity > 1)
    	{
    		CollisionBody.setLinearVelocity(DesiredVelocity.x*moveSpeed/velocity, DesiredVelocity.y*moveSpeed/velocity);
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
	
	void onUpdate(float delta)
    {
		//checksum += Math.abs(this.getX()) + Math.abs(this.getY());
		//checkHealth += Health;
		
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
