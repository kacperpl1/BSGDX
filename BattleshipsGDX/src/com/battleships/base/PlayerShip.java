package com.battleships.base;

import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.battleships.network.UnitData;

public class PlayerShip extends Unit {
	
	float rotationRate = 360; //degrees per second
	Vector2 CurrentVelocity = new Vector2(0,0);
	
	public LinkedList<PlayerWeapon> Inventory = new LinkedList<PlayerWeapon>();
	
	PlayerShip(String Team, float InitialX, float InitialY, int slot)
    {
		super(Team, InitialX, InitialY);
    	moveSpeed = 150;
    	MaxHealth = 2500;
    	Health = MaxHealth;
    	switch(slot) {
			case 0 : colorSprite.setColor(com.badlogic.gdx.graphics.Color.MAGENTA); break;
			case 1 : colorSprite.setColor(com.badlogic.gdx.graphics.Color.PINK); break;
			case 2 : colorSprite.setColor(com.badlogic.gdx.graphics.Color.ORANGE); break;
			case 3 : colorSprite.setColor(com.badlogic.gdx.graphics.Color.CYAN); break;
			case 4 : colorSprite.setColor(com.badlogic.gdx.graphics.Color.GREEN); break;
			case 5 : colorSprite.setColor(com.badlogic.gdx.graphics.Color.YELLOW); break;
			default : break;
    	}
    	visor = new Visor(this);
    	setVisualRotation(CurrentVelocity.x, CurrentVelocity.y);
    }
	
	void setVelocity()
    {
    	float velocity = CurrentVelocity.len();
    	CollisionBody.setLinearVelocity(CurrentVelocity.x*moveSpeed/velocity*GameScreen.WORLD_TO_BOX, CurrentVelocity.y*moveSpeed/velocity*GameScreen.WORLD_TO_BOX);
        setVisualRotation(CurrentVelocity.x, CurrentVelocity.y);
    }
	
	void updateUnitData(UnitData data)
	{
		Health = data.health;
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
		icon.setVisible(false);
    	CollisionBody.setLinearVelocity(0, 0);
    	this.getParent().removeActor(this);
    	if(team.equals(GameScreen.LocalPlayerTeam))
		{
    		Visor.VisorList.remove(visor);
			for(Unit current : VisibleEnemies)
			{
				current.VisibleEnemiesCount--;
			}
		}  
    	GameScreen.executorService.schedule(new Callable<PlayerShip>() {
  		  @Override
  		  public PlayerShip call() {
      		icon.setVisible(true);
  			GameScreen.gameStage.addActor(PlayerShip.this);
  			if(team == GameScreen.LocalPlayerTeam)
  				Visor.VisorList.add(visor);
  			Health = MaxHealth;

        	if(team == "red")
        	{
        		CollisionBody.setTransform(0, 768*GameScreen.WORLD_TO_BOX, 0);
        	}
        	else
        	{
        		CollisionBody.setTransform(0, -768*GameScreen.WORLD_TO_BOX, 0);
        	}
  		    return PlayerShip.this;
  		  }
  		}, 5, TimeUnit.SECONDS);
	}
	
	void onUpdate(float delta)
	{
    	if(Health<=0)
    	{
    		Health=0;
    		Destroy();
    		return;
    	}
		for(Weapon current : Inventory)
    	{
    		current.onUpdate(delta);
    	}
	}
}
