package com.battleships.base;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Tower extends Unit {

	Tower(String Team, float InitialX, float InitialY)
	{
		super(Team, InitialX, InitialY);
		if(team == "red")
			baseSprite.setRegion(Resources.TowerTextureRegion[0]);
		else
			baseSprite.setRegion(Resources.TowerTextureRegion[1]);
    	MaxHealth = 5000;
    	Health = MaxHealth;
    	goldworth = 500;
    	visor = new Visor(this);
    	gun = new TowerCannon(this);
    	CollisionBody.setType(BodyType.StaticBody);
    	this.setPosition(CollisionBody.getPosition().x*GameScreen.BOX_TO_WORLD,CollisionBody.getPosition().y*GameScreen.BOX_TO_WORLD);
	}
	
	public void draw (SpriteBatch batch, float parentAlpha) {
		if(GameScreen.stepNow)
		{
			onUpdate(GameScreen.BOX_STEP);
		
			if(Health<=0)
			{
				Destroy();
			}
		}
        
        if(Health>0)
        {
            batch.draw(baseSprite, getX()-16,getY()-64,64,128);
            
        	batch.draw(Resources.HealthbarTextureRegion[0], 
        			getX()-16,getY()+32,32,4);
        	batch.draw(Resources.HealthbarTextureRegion[1], 
        			getX()-16+1,getY()+32,32*((float)Health/(float)MaxHealth),4);
        }
	}
	
	private class TowerCannon extends Weapon {	
	public TowerCannon(Unit o) {
			super(o, 1);
			// TODO Auto-generated constructor stub
		}	
		void defaultProperties()
		{
			FireDelay = 1;
			Range = 1200/6;
			Damage = 50;
		}
	}
}
