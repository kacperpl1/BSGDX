package server;

import java.util.LinkedList;
import java.util.Random;


import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;

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
        dynamicCircle.setRadius(Range*GameThread.WORLD_TO_BOX);  
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
}
