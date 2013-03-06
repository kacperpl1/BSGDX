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
		if(CollisionBody.getLinearVelocity().len()>0)
			setVisualRotation(CollisionBody.getLinearVelocity().x, CollisionBody.getLinearVelocity().y);
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
