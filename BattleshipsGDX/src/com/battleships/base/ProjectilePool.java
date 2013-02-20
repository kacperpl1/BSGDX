package com.battleships.base;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Pool;


public class ProjectilePool extends Pool<Projectile>{

	@Override
	protected Projectile newObject() {
		BodyDef bodyDef = new BodyDef();  
        bodyDef.type = BodyType.DynamicBody;
		return new Projectile();
	}

}