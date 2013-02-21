package com.battleships.base;

import java.util.LinkedList;
import java.util.Random;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;

public class Weapon {
	static ContactListener contactListener;
	
	Fixture SensorFixture;
	LinkedList<Unit> Enemies;
	Unit Owner;
	float FireDelayTimer;
	float FireDelay;
	int Range;
	int Damage;
	int cost;
	int weapon_id;
	
	public Weapon(Unit o, int type)
	{
		weapon_id=type;
		if(contactListener == null)
			createContactListener();
			
		defaultProperties();
		Enemies=new LinkedList<Unit>();
		Owner = o;
		cost = 0;
		FireDelayTimer = 0;
		createBody();
	}
	
	void defaultProperties()
	{
		FireDelay = 0.25f;
		Range = 100;
		Damage = 10;
	}

	void createBody()
	{
		CircleShape dynamicCircle = new CircleShape();  
        dynamicCircle.setRadius(Range*BaseGame.WORLD_TO_BOX);  
        FixtureDef fixtureDef = new FixtureDef();  
        fixtureDef.shape = dynamicCircle;  
        fixtureDef.density = 1.0f;  
        fixtureDef.friction = 0.0f;  
        fixtureDef.restitution = 0.0f;
        fixtureDef.isSensor = true;
		
	    SensorFixture = Owner.CollisionBody.createFixture(fixtureDef);
	    SensorFixture.setUserData(this);
	    SensorFixture.setSensor(true);
	}
	
	void Destroy()
	{
		SensorFixture.setUserData(null);
		Owner.CollisionBody.destroyFixture(SensorFixture);
	}

	void flushTargetList(){
		Enemies.clear();
	};
	
    void onUpdate(float Delta){
    	if(FireDelayTimer>0)
    		FireDelayTimer -= Delta;
    	else if(FireDelayTimer<=0)
    	{
	    		if(Enemies.size()>0)
	    		{
	    			Unit target = Enemies.get(new Random().nextInt(Enemies.size()));
	    			if(target.Health<=0)//HACK!!
	    			{
	    				Enemies.remove(target);
	    			}
	    			else
	    			{
	    				Projectile.Launch(Owner,target,Damage, weapon_id);
	    			}
	    		}
				FireDelayTimer = MathUtils.random(FireDelay * 0.9f,FireDelay * 1.1f);
    	}
    };
    
    private static void createContactListener()
    {
        contactListener = new ContactListener()
        {
            @Override
            public void beginContact(Contact contact)
            {
                final Fixture x1 = contact.getFixtureA();
                final Fixture x2 = contact.getFixtureB();
                if(x2.getUserData() instanceof Unit && x1.getUserData() instanceof Weapon)
                {
                	if(!((Weapon)x1.getUserData()).Owner.team.equals(((Unit)(x2.getUserData())).team))
                		((Weapon)x1.getUserData()).Enemies.add(((Unit)(x2.getUserData())));
                }
                if(x1.getUserData() instanceof Unit && x2.getUserData() instanceof Weapon)
                {
                	if(!((Weapon)x2.getUserData()).Owner.team.equals(((Unit)(x1.getUserData())).team))
                		((Weapon)x2.getUserData()).Enemies.add(((Unit)(x1.getUserData())));
                }
                if(x2.getUserData() instanceof Unit && x1.getUserData() instanceof Visor)
                {
                	Visor current = ((Visor)x1.getUserData());
                	Unit target = ((Unit)(x2.getUserData()));
                	if(!(target instanceof Tower) && !target.team.equals(BaseGame.LocalPlayerTeam))
                	{
                		current.Owner.VisibleEnemies.add(target);
                		target.VisibleEnemiesCount++;
                	}
                }  
                if(x1.getUserData() instanceof Unit && x2.getUserData() instanceof Visor)
                {
                	Visor current = ((Visor)x2.getUserData());
                	Unit target = ((Unit)(x1.getUserData()));
                	if(!(target instanceof Tower) && !target.team.equals(BaseGame.LocalPlayerTeam))
                	{
                		current.Owner.VisibleEnemies.add(target);
                		target.VisibleEnemiesCount++;
                	}
                }         
            }
 
            @Override
            public void endContact(Contact contact)
            {
                final Fixture x1 = contact.getFixtureA();
                final Fixture x2 = contact.getFixtureB();
                
                if(x2.getUserData() instanceof Unit && x1.getUserData() instanceof Weapon)
                {
                	if(!((Weapon)x1.getUserData()).Owner.team.equals(((Unit)(x2.getUserData())).team))
                		((Weapon)x1.getUserData()).Enemies.remove(((Unit)(x2.getUserData())));
                }
                if(x1.getUserData() instanceof Unit && x2.getUserData() instanceof Weapon)
                {
                	if(!((Weapon)x2.getUserData()).Owner.team.equals(((Unit)(x1.getUserData())).team))
                		((Weapon)x2.getUserData()).Enemies.remove(((Unit)(x1.getUserData())));
                } 
                if(x2.getUserData() instanceof Unit && x1.getUserData() instanceof Visor)
                {
                	Visor current = ((Visor)x1.getUserData());
                	Unit target = ((Unit)(x2.getUserData()));
                	if(!(target instanceof Tower) && !target.team.equals(BaseGame.LocalPlayerTeam))
                	{
                		current.Owner.VisibleEnemies.remove(target);
                		target.VisibleEnemiesCount--;
                		if(target.VisibleEnemiesCount<=0)
                		{
                			target.VisibleEnemiesCount=0;
                		}
                	}
                }  
                if(x1.getUserData() instanceof Unit && x2.getUserData() instanceof Visor)
                {
                	Visor current = ((Visor)x2.getUserData());
                	Unit target = ((Unit)(x1.getUserData()));
                	if(!(target instanceof Tower) && !target.team.equals(BaseGame.LocalPlayerTeam))
                	{
                		current.Owner.VisibleEnemies.remove(target);
                		target.VisibleEnemiesCount--;
                		if(target.VisibleEnemiesCount<=0)
                		{
                			target.VisibleEnemiesCount=0;
                		}
                	}
                }  
            }  
 
            @Override
            public void preSolve(Contact contact, Manifold oldManifold)
            {
                   
            }
 
            @Override
            public void postSolve(Contact contact, ContactImpulse impulse)
            {
                   
            }
        };
        BaseGame.physicsWorld.setContactListener(contactListener);
    } 
}
