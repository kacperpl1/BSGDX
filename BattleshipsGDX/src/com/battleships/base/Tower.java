package com.battleships.base;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Tower extends Unit {
	
	public static TextureRegion[] TowerTextureRegion;

	Tower(String Team, float InitialX, float InitialY)
	{
		super(Team, InitialX, InitialY);
		if(team == "red")
			baseSprite.setRegion(TowerTextureRegion[0]);
		else
			baseSprite.setRegion(TowerTextureRegion[1]);
    	MaxHealth = 5000;
    	Health = MaxHealth;
    	visor = new Visor(this);
    	gun = new PlayerWeapon(this,9);
    	CollisionBody.setType(BodyType.StaticBody);
	}
	
	public void draw (SpriteBatch batch, float parentAlpha) {
		this.setPosition(CollisionBody.getPosition().x,CollisionBody.getPosition().y);
		this.setZIndex(2);
        batch.draw(baseSprite, getX()-16,getY()-64,64,128);
		onUpdate(BaseGame.delta);
	}
}
