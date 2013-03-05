package server;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.math.Vector2;

public class Projectile{
	
	static void Launch(Unit inst, Unit targ, int dmg, int type)
	{
		new Projectile().Init(inst, targ, dmg, type);
	}

	void Init(final Unit Instigator, final Unit Target, final int Damage, int type)
	{   
		GameThread.executorService.schedule(new Callable<Projectile>() {
  		  @Override
  		  public Projectile call() {
  			Target.TakeDamage(Damage, Instigator);
  		    return Projectile.this;
  		  }
  		}, (long)(new Vector2( Target.getX() - Instigator.getX(), Target.getY() - Instigator.getY()).len() 
  				/ PlayerWeapon.Speed[type] * 1000), TimeUnit.MILLISECONDS);
	}
}
