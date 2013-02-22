package com.battleships.base;


public class Cruiser extends Unit {
	
	private String lane;
	
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
	
	void updateVelocity()
    {
    	float velocity = DesiredVelocity.len();
    	if(velocity > 1)
    	{
    		CollisionBody.setLinearVelocity(DesiredVelocity.x*GameScreen.WORLD_TO_BOX, DesiredVelocity.y*GameScreen.WORLD_TO_BOX);
        	setVisualRotation(DesiredVelocity.x, DesiredVelocity.y);
    	}
    	else
    		CollisionBody.setLinearVelocity(0, 0);
    }
	
	void onUpdate(float delta)
    {
    	if(Health<=0)
    	{
    		GameScreen.hudStage.getRoot().removeActor(icon);
        	this.getParent().removeActor(this);
        	int fixtures = CollisionBody.getFixtureList().size();
    		for(int i=0; i<fixtures; i++)
    		{
    			CollisionBody.destroyFixture(CollisionBody.getFixtureList().get(0));
    		}
    		CollisionBody.setUserData(null);
    		bodyPool.free(CollisionBody);
        	if(team.equals(GameScreen.LocalPlayerTeam))
    		{
    			for(Unit current : VisibleEnemies)
    			{
    				current.VisibleEnemiesCount--;
    			}
    		}  
        	return;
    	}
        	gun.onUpdate(delta);
        	
        	if(gun.Enemies.size()>0)
        	{
        		setDesiredVelocity(DesiredVelocity.x/2,DesiredVelocity.y/2);
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
