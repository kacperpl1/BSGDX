package com.battleships.base;


public class Cruiser extends Unit {
	
	Cruiser(String Team, float InitialX, float InitialY)
    {
		super(Team, InitialX, InitialY);
    	baseSprite.setScale(0.75f);
    	MaxHealth = 500;
    	Health = MaxHealth;
    	visor = new Visor(this);
    	gun = new Weapon(this,0);
    }	
	
	void updateVelocity()
    {
		if(Math.abs(CollisionBody.getPosition().x*GameScreen.BOX_WORLD_TO - this.getX())>1 || 
				Math.abs(CollisionBody.getPosition().y*GameScreen.BOX_WORLD_TO - this.getY())>1)
			setVisualRotation(CollisionBody.getPosition().x - this.getX()*GameScreen.WORLD_TO_BOX, 
					CollisionBody.getPosition().y - this.getY()*GameScreen.WORLD_TO_BOX);
    }
	
	void onUpdate(float delta)
    {
    	if(Health<=0)
    	{
    		Health=0;
    		Destroy();
    		return;
    	}
    	gun.onUpdate(delta);
    }
}
