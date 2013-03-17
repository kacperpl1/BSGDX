package server;

import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import shared.Common;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class PlayerShip extends Unit {
	
	float rotationRate = 360; //degrees per second
	Vector2 CurrentVelocity = new Vector2(0,0);
	
	public LinkedList<PlayerWeapon> Inventory = new LinkedList<PlayerWeapon>();
	
	PlayerShip(String Team, float InitialX, float InitialY, GameThread owner, short slot)
    {
		super(Team, InitialX, InitialY, owner);
    	moveSpeed = 150;
    	MaxHealth = 2500;
    	Health = MaxHealth;
    	createUnitData(Common.PLAYER_SHIP, slot, owner.getName());
    	Inventory.add(new PlayerWeapon(this, 8));
    	Inventory.add(new PlayerWeapon(this, 8));
    	
    }

	void setVelocity()
    {
    	float velocity = CurrentVelocity.len();
    	CollisionBody.setLinearVelocity(CurrentVelocity.x*moveSpeed/velocity*GameThread.WORLD_TO_BOX, CurrentVelocity.y*moveSpeed/velocity*GameThread.WORLD_TO_BOX);
    }
	
	void updateVelocity()
    {

    	if(DesiredVelocity.len()<=0)
    	{
    		CollisionBody.setLinearVelocity(0, 0);
    		return;
    	}
    		
    	
    	float CurrentAngle = (float) Math.toDegrees(MathUtils.atan2(CurrentVelocity.x, CurrentVelocity.y));
		float DesiredAngle = (float) Math.toDegrees(MathUtils.atan2(DesiredVelocity.x, DesiredVelocity.y));
		float DeltaAngle = (float) (DesiredAngle-CurrentAngle);
		if(Math.abs(DeltaAngle) < rotationRate *  Gdx.graphics.getDeltaTime())
		{
	        CurrentVelocity.set(DesiredVelocity);
		}
		else
		{
    		if(Math.abs(DeltaAngle)>180)
    		{
    			if(DeltaAngle >0)
    			{
    				DeltaAngle -= 360;
    			}
    			else
    			{
    				DeltaAngle += 360;    				
    			}
    		}
			float TempAngle;
			if(DeltaAngle>0)
			{
				TempAngle = CurrentAngle + (rotationRate * Gdx.graphics.getDeltaTime());
			}
			else
			{
				TempAngle = CurrentAngle - (rotationRate * Gdx.graphics.getDeltaTime());
			}
	        CurrentVelocity.set((float)Math.sin(Math.toRadians(TempAngle)), (float)Math.cos(Math.toRadians(TempAngle)));
		}
		setVelocity();
    }
	
	void Destroy()
	{

    	CollisionBody.setLinearVelocity(0, 0);
    	GameThread.executorService.schedule(new Callable<PlayerShip>() {
  		  @Override
  		  public PlayerShip call() {
  			Health = MaxHealth;

        	if(team == "red")
        	{
        		CollisionBody.setTransform(0, 768*GameThread.WORLD_TO_BOX, 0);
        	}
        	else
        	{
        		CollisionBody.setTransform(0, -768*GameThread.WORLD_TO_BOX, 0);
        	}
  		    return PlayerShip.this;
  		  }
  		}, 5, TimeUnit.SECONDS);
	}
	
	void onUpdate(float delta)
	{
		for(Weapon current : Inventory)
    	{
    		current.onUpdate(delta);
    	}
	}
}
