package com.battleships.base;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Citadel extends Unit {

	Citadel(String Team, float InitialX, float InitialY) {
		super(Team, InitialX, InitialY);
		MaxHealth = 25000;
    	Health = MaxHealth;
    	visor = new Visor(this);
    	CollisionBody.setType(BodyType.StaticBody);
    	this.setPosition(CollisionBody.getPosition().x*GameScreen.BOX_TO_WORLD,CollisionBody.getPosition().y*GameScreen.BOX_TO_WORLD);
	}
	
	public void draw (SpriteBatch batch, float parentAlpha) {
		if(GameScreen.stepNow)
		{
			if(Health<=0)
			{
				BaseGame.instance.getScreen().dispose();
				BaseGame.instance.setScreen(new MenuScreen());
			}
		}
		this.setZIndex(2);
        batch.draw(Resources.HealthbarTextureRegion[0], 
        		getX()-64,getY()+32,128,6);
        batch.draw(Resources.HealthbarTextureRegion[1], 
        		getX()-64+1,getY()+32,134*((float)Health/(float)MaxHealth),6);
	}
}
