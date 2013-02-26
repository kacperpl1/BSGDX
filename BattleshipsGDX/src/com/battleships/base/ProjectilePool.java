package com.battleships.base;

import com.badlogic.gdx.utils.Pool;


public class ProjectilePool extends Pool<Projectile>{

	@Override
	protected Projectile newObject() {
		return new Projectile();
	}

}