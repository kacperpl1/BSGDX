package com.battleships.base;


import java.util.Iterator;
import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.Actor;

public strictfp abstract class Unit extends Actor {
	static long unitSpawnNumber = 0;
	final long unitID = unitSpawnNumber;
	
	String team;
	short MaxHealth = 1000;
    short Health = MaxHealth;
    Sprite baseSprite;
	Sprite colorSprite;
	float moveSpeed = 50 * GameScreen.WORLD_TO_BOX;
	int goldworth = 50;
	Body CollisionBody;
	
	Vector2 DesiredVelocity = new Vector2(0,0);
	
	Weapon gun = null;
	Visor visor;
	protected LinkedList<Unit> VisibleEnemies = new LinkedList<Unit>();
	Actor icon;
	protected Vector2 CurrentPosition;
	static BodyPool bodyPool = new BodyPool();
	
	//static float checksum = 0;
	//static float checkHealth = 0;
	
	Unit(String Team, float InitialX, float InitialY)
	{
		unitSpawnNumber++;
		
		team = Team;
				
		baseSprite = new Sprite();
		baseSprite.setRegion(Resources.BaseTextureRegion[0]);
		
		baseSprite.setSize(64, 64);
		baseSprite.setOrigin(baseSprite.getWidth()/2, baseSprite.getHeight()/2);
		baseSprite.setPosition(-baseSprite.getWidth()/2, -baseSprite.getHeight()/2);
		
		colorSprite = new Sprite();
		colorSprite.setRegion(Resources.ColorTextureRegion[0]);
		
		colorSprite.setSize(64, 64);
		colorSprite.setOrigin(colorSprite.getWidth()/2, colorSprite.getHeight()/2);
		colorSprite.setPosition(-colorSprite.getWidth()/2, -colorSprite.getHeight()/2);
		
		if(team == "red")
			colorSprite.setColor(Color.RED);
		else
			colorSprite.setColor(Color.BLUE);

		createBody(InitialX,InitialY);
		GameScreen.gameStage.addActor(this);
		
		icon = new Actor(){
			float scale = GameScreen.miniMap.getWidth()/2048;
	        public void draw (SpriteBatch batch, float parentAlpha) {
	    		if(team != GameScreen.LocalPlayerTeam && VisibleEnemies.size()==0 && !(Unit.this instanceof Tower || Unit.this instanceof Citadel) || Health <= 0 )
	    			return;
	    		
        		batch.setColor(colorSprite.getColor());
	            batch.draw(Resources.iconTexture,
	            		GameScreen.miniMap.getX() + GameScreen.miniMap.getWidth()/2 + Unit.this.CurrentPosition.x*scale -2,
	            		GameScreen.miniMap.getY() + GameScreen.miniMap.getHeight()/2 + Unit.this.CurrentPosition.y*scale -2,4,4);
	            batch.setColor(Color.WHITE);
	        }
		};
		GameScreen.hudStage.addActor(icon);
	}
	
	void createBody(float initialX, float initialY)
	{ 
		CollisionBody = bodyPool.obtain();
		CollisionBody.setType(BodyType.DynamicBody);			
		CollisionBody.setTransform(initialX*GameScreen.WORLD_TO_BOX,initialY*GameScreen.WORLD_TO_BOX, 0);
		
		CircleShape dynamicCircle = new CircleShape();  
        dynamicCircle.setRadius(16f*GameScreen.WORLD_TO_BOX);  
        FixtureDef fixtureDef = new FixtureDef();  
        fixtureDef.shape = dynamicCircle;  
        if(this instanceof PlayerShip)
        {
        	fixtureDef.density = 1.0f;  
        }
        else
        {
        	fixtureDef.density = Integer.MAX_VALUE;
        }
        fixtureDef.friction = 0.0f;  
        fixtureDef.restitution = 0.0f;
        CollisionBody.createFixture(fixtureDef);  
        CollisionBody.getFixtureList().get(0).setUserData(this);
        
        CurrentPosition = new Vector2(CollisionBody.getPosition().x*GameScreen.BOX_TO_WORLD,CollisionBody.getPosition().y*GameScreen.BOX_TO_WORLD);
        this.setPosition(initialX, initialY);
	}  
	
	public void draw(SpriteBatch batch, float parentAlpha) {
		
		if(GameScreen.stepNow)
		{
			this.setPosition(CollisionBody.getPosition().x*GameScreen.BOX_TO_WORLD, CollisionBody.getPosition().y*GameScreen.BOX_TO_WORLD);
			onUpdate(GameScreen.BOX_STEP);
		
			if(Health<=0)
			{
				Destroy();
				return;
			}
		}
		if(team == GameScreen.LocalPlayerTeam || VisibleEnemies.size()>0 && Health>0)
		{
		
			float widthScaled = baseSprite.getWidth()*baseSprite.getScaleX();
			
	        batch.draw(baseSprite, CurrentPosition.x-widthScaled/2,CurrentPosition.y-widthScaled/2,widthScaled,widthScaled);
	        batch.setColor(colorSprite.getColor());
	        batch.draw(colorSprite, CurrentPosition.x-widthScaled/2,CurrentPosition.y-widthScaled/2,widthScaled,widthScaled);
	        batch.setColor(Color.WHITE);
	        
	        batch.draw(Resources.HealthbarTextureRegion[0], CurrentPosition.x-widthScaled/4,CurrentPosition.y+widthScaled/4,widthScaled/2,4);
		
		    batch.draw(Resources.HealthbarTextureRegion[1], CurrentPosition.x-widthScaled/4+1,CurrentPosition.y+widthScaled/4,widthScaled/2*((float)Health/(float)MaxHealth),4);
		}
		if(GameScreen.box_accu < GameScreen.BOX_STEP)
		{
			if(DesiredVelocity.len()>0)
				lerp(Gdx.graphics.getDeltaTime()/(GameScreen.BOX_STEP-GameScreen.box_accu+Gdx.graphics.getDeltaTime()));
			else
				lerp(GameScreen.BOX_STEP);
		}
	}
	
	public void lerp (float alpha) {
		final float invAlpha = 1.0f - alpha;
		CurrentPosition.x = (CurrentPosition.x * invAlpha) + (getX() * alpha);
		CurrentPosition.y = (CurrentPosition.y * invAlpha) + (getY() * alpha);
	}
	
	void onUpdate(float delta)
	{
		gun.onUpdate(delta);
		updateVelocity();
	}
	
    void TakeDamage(int Damage, Unit Instigator)
    {
    	if(Health > 0 && Health - Damage <= 0 && Instigator instanceof PlayerShip)
    	{
    		((PlayerShip)Instigator).PlayerGold += this.goldworth;
    	}
    	Health -= Damage;
    }
	
	void Destroy()
	{
		GameScreen.hudStage.getRoot().removeActor(icon);
    	this.getParent().removeActor(this);
    	int fixtures = CollisionBody.getFixtureList().size();
		for(int i=0; i<fixtures; i++)
		{
			CollisionBody.destroyFixture(CollisionBody.getFixtureList().get(0));
		}
		CollisionBody.setUserData(null);
		bodyPool.free(CollisionBody);
		Unit current;
		Iterator<Unit> iter = VisibleEnemies.iterator();
		while(iter.hasNext())
		{
			current = iter.next();
			current.VisibleEnemies.remove(this);
			iter.remove();
		}
	}
    
    void setDesiredVelocity(float X, float Y)
    {
    	DesiredVelocity.x = X;
    	DesiredVelocity.y = Y;
    }
    
    void updateVelocity()
    {
    	float velocity = DesiredVelocity.len();
    	if(velocity > 0)
    	{
    		CollisionBody.setLinearVelocity(DesiredVelocity.x*moveSpeed/velocity*GameScreen.WORLD_TO_BOX, DesiredVelocity.y*moveSpeed/velocity*GameScreen.WORLD_TO_BOX);
        	setVisualRotation(DesiredVelocity.x, DesiredVelocity.y);
    	}
    	else
    		CollisionBody.setLinearVelocity(0, 0);
    }
    
    void setVisualRotation(float X, float Y)
    {
    	int Angle = (int) Math.toDegrees(-Math.atan2(X+0.0001f, -Y));
    	if(Angle < 0) 
    		Angle += 360;

    		Angle = Angle/10;
    		if(Angle >17)
    		{
    			Angle = 36 - Angle;
    			Angle *= 0.833333f;
    			baseSprite.setRegion(Resources.BaseTextureRegion[Angle]);
    			colorSprite.setRegion(Resources.ColorTextureRegion[Angle]);
    			baseSprite.flip(true, false);
    			colorSprite.flip(true, false);
    		}
    		else
    		{
    			Angle *= 0.833333f;
    			baseSprite.setRegion(Resources.BaseTextureRegion[Angle]);
    			colorSprite.setRegion(Resources.ColorTextureRegion[Angle]);
    			baseSprite.flip(false, false);
    			colorSprite.flip(false, false);
    		}
    } 
}
