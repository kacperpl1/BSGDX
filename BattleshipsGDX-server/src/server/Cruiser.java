package server;

import com.badlogic.gdx.math.Vector2;

import shared.Common;

public class Cruiser extends Unit {
	
	private String lane;
	private Unit Target = null;
	private Vector2 vecToTarget = new Vector2();
	
	Cruiser(String Team, float InitialX, float InitialY, String Lane, GameThread owner)
    {
		super(Team, InitialX, InitialY, owner);
		lane = Lane;
    	MaxHealth = 500;
    	Health = MaxHealth;
    	gun = new Weapon(this,0);
    	createUnitData(Common.CRUISER);
    }	
	
	void updateVelocity()
    {
    	float velocity = DesiredVelocity.len();
    	if(velocity > 0)
    	{
    		CollisionBody.setLinearVelocity(DesiredVelocity.x*GameThread.WORLD_TO_BOX, DesiredVelocity.y*GameThread.WORLD_TO_BOX);
    	}
    	else
    		CollisionBody.setLinearVelocity(0, 0);
    }
	
    void TakeDamage(int Damage, Unit Instigator)
    {
    	super.TakeDamage(Damage, Instigator);
    	
    	if(gun.Enemies.size()==0 && Target == null)
    	{
    		Target = Instigator;
    	}
    }
	
	void onUpdate(float delta)
    {
		updateVelocity();
    	gun.onUpdate(delta);
    	
    	if(gun.Enemies.size()>0)
    	{
    		setDesiredVelocity(DesiredVelocity.x/2,DesiredVelocity.y/2);
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
	    		{
	    			setDesiredVelocity(0,50);
	    		}
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
	    			setDesiredVelocity(0,-50);
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
