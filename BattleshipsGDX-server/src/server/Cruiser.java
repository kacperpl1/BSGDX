package server;

public class Cruiser extends Unit {
	
	private String lane;
	
	Cruiser(String Team, float InitialX, float InitialY, String Lane, GameThread owner)
    {
		super(Team, InitialX, InitialY, owner);
		lane = Lane;
    	MaxHealth = 500;
    	Health = MaxHealth;
    	gun = new Weapon(this,0);
    }	
	
	void updateVelocity()
    {
    	float velocity = DesiredVelocity.len();
    	if(velocity > 1)
    	{
    		CollisionBody.setLinearVelocity(DesiredVelocity.x*GameThread.WORLD_TO_BOX, DesiredVelocity.y*GameThread.WORLD_TO_BOX);
    	}
    	else
    		CollisionBody.setLinearVelocity(0, 0);
    }
	
	void onUpdate(float delta)
    {
    	if(Health<=0)
    	{
    		Health = 0;
    		Destroy();
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
