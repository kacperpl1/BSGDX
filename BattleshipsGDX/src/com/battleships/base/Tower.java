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
    	visor = new Visor(this);
    	gun = new PlayerWeapon(this,9);
    	CollisionBody.setType(BodyType.StaticBody);
	}
	
	public void draw (SpriteBatch batch, float parentAlpha) {
		this.setPosition(CollisionBody.getPosition().x*BaseGame.BOX_WORLD_TO,CollisionBody.getPosition().y*BaseGame.BOX_WORLD_TO);
		this.setZIndex(2);
        batch.draw(baseSprite, getX()-16,getY()-64,64,128);
        batch.draw(Resources.HealthbarTextureRegion[(int)Math.min(((float)Health/(float)MaxHealth)*10,9)], 
        		getX()-16,getY()+32,32,4);
        
		onUpdate(BaseGame.delta);
	}
}
