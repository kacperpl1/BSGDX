package server;

import shared.Common;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Tower extends Unit {

	Tower(String Team, float InitialX, float InitialY, GameThread owner)
	{
		super(Team, InitialX, InitialY, owner);
    	MaxHealth = 5000;
    	Health = MaxHealth;
    	gun = new PlayerWeapon(this,9);
    	CollisionBody.setType(BodyType.StaticBody);
    	createUnitData(Common.TOWER);
	}
}
