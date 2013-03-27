package com.battleships.base;

import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class PlayerShip extends Unit {
	
	float rotationRate = 360; //degrees per second
	Vector2 CurrentVelocity = new Vector2(0,0);
	int PlayerGold = 200;
	
	public LinkedList<PlayerWeapon> Inventory = new LinkedList<PlayerWeapon>();
	private int deathcounter;
	
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
    	Inventory.add(new PlayerWeapon(this, 8));
    	Inventory.add(new PlayerWeapon(this, 8));
    }
	
	void setVelocity()
    {
    	float velocity = CurrentVelocity.len();
    	CollisionBody.setLinearVelocity(CurrentVelocity.x*moveSpeed/velocity*GameScreen.WORLD_TO_BOX, CurrentVelocity.y*moveSpeed/velocity*GameScreen.WORLD_TO_BOX);
        setVisualRotation(CurrentVelocity.x, CurrentVelocity.y);
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
	
	public void draw (SpriteBatch batch, float parentAlpha) {
		if(Health >0)
		{
			super.draw(batch, parentAlpha);	
			updateVelocity();
		}
	}
	
	void Destroy()
	{
		switch(deathcounter)
		{
			case 1: hide(); break;
			case 10: Health = MaxHealth; IncomingDamage = 0; deathcounter = 0; break;
			default: break;
		}
		deathcounter++;
	}
	
	void buyItem(int itemType)
	{
		PlayerWeapon newweapon = new PlayerWeapon(this, itemType);
		PlayerGold -= PlayerWeapon.CostData[itemType];
		Inventory.add(newweapon);
	}
	
	void sellItem(int slotNumber)
	{
		Inventory.get(slotNumber).Destroy();
		PlayerGold += PlayerWeapon.CostData[Inventory.get(slotNumber).weapon_id];
		Inventory.remove(slotNumber);
	}
	
	void TakeDamage(int Damage, Unit Instigator)
    {
    	IncomingDamage += Damage;
    	if(Health - Damage <= 0)
    	{
    		this.goldworth = 100 + (int) (this.PlayerGold * 0.2f);
    		this.PlayerGold *= 0.8f;
    		
    		if(Instigator instanceof PlayerShip)
    		{
    			((PlayerShip)Instigator).PlayerGold += this.goldworth;
    		}
    		else
    		{
    			//TODO - handle computer kills to share gold
    			Instigator.goldworth += this.goldworth/2;
    		}
    	}
    }
	
	void hide()
	{
    	CollisionBody.setLinearVelocity(0, 0);
    	if(team.equals(GameScreen.LocalPlayerTeam))
		{
			for(Unit current : VisibleEnemies)
			{
				if(current.gun != null && current.gun.Enemies.contains(this))
					current.gun.Enemies.remove(this);
				current.VisibleEnemiesCount--;
			}
			VisibleEnemies.clear();
		}  

    	if(team == "red")
    	{
    		CollisionBody.setTransform(0, 768*GameScreen.WORLD_TO_BOX, 0);
    		this.setPosition(0, 768);
    	}
    	else
    	{
    		CollisionBody.setTransform(0, -768*GameScreen.WORLD_TO_BOX, 0);
    		this.setPosition(0, -768);
    	}
	}
	
	void onUpdate(float delta)
	{
    	Health -= IncomingDamage;
    	IncomingDamage = 0;
		for(Weapon current : Inventory)
    	{
    		current.onUpdate(delta);
    	}
	}
}
