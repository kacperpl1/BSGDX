package com.battleships.base;

import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class PlayerShip extends Unit {
	
	int rotationRate = 360; //degrees per second
	Vector2 DesiredVelocity = new Vector2(0,0);
	Vector2 CurrentVelocity = new Vector2(0,0);
	
	public LinkedList<PlayerWeapon> Inventory = new LinkedList<PlayerWeapon>();
	
	PlayerShip(String Team, float InitialX, float InitialY, Color PlayerColor)
    {
		super(Team, InitialX, InitialY);
    	moveSpeed = 500;
    	MaxHealth = 2500;
    	Health = MaxHealth;
		colorSprite.setColor(PlayerColor);
    	visor = new Visor(this);
    }
	
	void onUpdate(float delta)
	{
    	if(Health<=0)
    	{
    		icon.setVisible(false);
        	CollisionBody.setLinearVelocity(0, 0);
    		visor.visionArea.setScale(0);
        	this.getParent().removeActor(this);
        	if(team.equals(BaseGame.LocalPlayerTeam))
    		{
    			for(Unit current : VisibleEnemies)
    			{
    				current.VisibleEnemiesCount--;
    			}
    		}  
        	BaseGame.executorService.schedule(new Callable<Projectile>() {
      		  @Override
      		  public Projectile call() {
          		icon.setVisible(true);
      			BaseGame.gameStage.addActor(PlayerShip.this);
        		visor.visionArea.setScale(1);
      			Health = MaxHealth;
      		    return null;
      		  }
      		}, 5, TimeUnit.SECONDS);
        	return;
    	}
		for(Weapon current : Inventory)
    	{
    		current.onUpdate(delta);
    	}
	}
}
