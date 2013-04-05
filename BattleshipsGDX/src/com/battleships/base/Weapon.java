package com.battleships.base;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Random;

import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;

public class Weapon {
	static ContactListener contactListener;
	static UnitIdComparator EnemySorter;
	
	Fixture SensorFixture;
	LinkedList<Unit> Enemies;
	Unit Owner;
	float FireDelayTimer;
	float FireDelay;
	int Range;
	int Damage;
	int cost;
	int weapon_id;
	public static Random RNG = new Random(1182370352);
	
	public Weapon(Unit o, int type)
	{
		weapon_id=type;
		if(contactListener == null)
		{
			EnemySorter = new UnitIdComparator();
			createContactListener();
		}
			
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
        dynamicCircle.setRadius(Range*GameScreen.WORLD_TO_BOX);  
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
	
    strictfp void onUpdate(float Delta){
    	if(FireDelayTimer>0)
    		FireDelayTimer -= Delta;
    	else if(FireDelayTimer<=0)
    	{
	    		if(Enemies.size()>0)
	    		{
	    			Unit target;
	    			while(Enemies.size()>0)
	    			{
	    				target = Enemies.get(RNG.nextInt(Enemies.size()));
		    			if(target.Health<=0)
		    			{
		    				Enemies.remove(target);
		    			}
		    			else
		    			{
		    				Projectile.Launch(Owner,target,Damage, weapon_id);
		    				//FireDelayTimer = FireDelay; 
		    				FireDelayTimer = FireDelay * (0.9f+RNG.nextFloat()/5f);
		    				break;
		    			}
	    			}
	    		}
    	}
    };
    
    class UnitIdComparator implements Comparator<Unit>{
		 
	    public int compare(Unit emp1, Unit emp2){ 
	    	if(emp1.unitID > emp2.unitID)
	    		return 1;
	        else if(emp1.unitID < emp2.unitID)
	        	return -1;
	        else
	        	return 0;
	    }
	 
	}
    
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
                	{
                		((Weapon)x1.getUserData()).Enemies.add(((Unit)(x2.getUserData())));
                		Collections.sort(((Weapon)x1.getUserData()).Enemies,EnemySorter);
                	}
                }
                if(x1.getUserData() instanceof Unit && x2.getUserData() instanceof Weapon)
                {
                	if(!((Weapon)x2.getUserData()).Owner.team.equals(((Unit)(x1.getUserData())).team))
                	{
                		((Weapon)x2.getUserData()).Enemies.add(((Unit)(x1.getUserData())));
                		Collections.sort(((Weapon)x2.getUserData()).Enemies,EnemySorter);
                	}
                }
                if(x2.getUserData() instanceof Unit && x1.getUserData() instanceof Visor)
                {
                	Visor current = ((Visor)x1.getUserData());
                	Unit target = ((Unit)(x2.getUserData()));
                	if(!(target instanceof Tower) && !target.team.equals(GameScreen.LocalPlayerTeam))
                	{
                		current.Owner.VisibleEnemies.add(target);
                		target.VisibleEnemiesCount++;
                	}
                }  
                if(x1.getUserData() instanceof Unit && x2.getUserData() instanceof Visor)
                {
                	Visor current = ((Visor)x2.getUserData());
                	Unit target = ((Unit)(x1.getUserData()));
                	if(!(target instanceof Tower) && !target.team.equals(GameScreen.LocalPlayerTeam))
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
                	if(!(target instanceof Tower) && !target.team.equals(GameScreen.LocalPlayerTeam))
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
                	if(!(target instanceof Tower) && !target.team.equals(GameScreen.LocalPlayerTeam))
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
        GameScreen.physicsWorld.setContactListener(contactListener);
    } 
}
