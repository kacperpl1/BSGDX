package com.battleships.base;

import com.badlogic.gdx.math.Vector2;

public class PracticeCruiser extends Cruiser {

	private String lane;
	private Unit Target = null;
	private Vector2 vecToTarget = new Vector2(0,0);
	
	PracticeCruiser(String Team, float InitialX, float InitialY, String Lane) {
		super(Team, InitialX, InitialY);
		lane = Lane;
	}
	
	void updateVelocity()
    {
		super.updateVelocity();
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
    	super.TakeDamage(Damage, Instigator);
    	
    	if(gun.Enemies.size()==0 && Target == null)
    	{
    		Target = Instigator;
    	}
    }
	
	void onUpdate(float delta)
    {
		super.onUpdate(delta);

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
