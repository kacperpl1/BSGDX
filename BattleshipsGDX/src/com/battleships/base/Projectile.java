package com.battleships.base;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public strictfp class Projectile extends Actor{
	Unit Instigator;
	Unit Target;
	int Damage;
	float TravelTime;
	private Sprite sprite;
	private BalisticMoveModifier moveModifier;
	public boolean destroyed;
	private float stepTime;
	private ParticleEffect particleEffect;
	private boolean exploded;

	public static ProjectilePool projectilepool = new ProjectilePool();
	
	static void Launch(Unit inst, Unit targ, int dmg, int type)
	{
		projectilepool.obtain().Init(inst, targ, dmg, type);
	}
	
	Projectile()
	{
		sprite = new Sprite();
		moveModifier = new BalisticMoveModifier();
		GameScreen.gameStage.addActor(this);
		
		particleEffect = new ParticleEffect();
	    particleEffect.load(Gdx.files.internal("data/explosion.p"), 
	            Gdx.files.internal("data"));
	}
	
	private class BalisticMoveModifier{

		private float Duration;
		private float X;
		private float Y;
		private float tX;
		private float tY;		
		private float timer;
		private float yOffset;
		
		public void Init(final float pDuration, final float pOffset) {
			this.Duration = pDuration;
			this.yOffset = pOffset;
			this.timer = 0;
			this.X = Instigator.CurrentPosition.x;
			this.Y = Instigator.CurrentPosition.y;
			this.tX = Target.getX();
			this.tY = Target.getY();
		}
		
		protected void onManagedUpdate(SpriteBatch batch, final float pSecondsElapsed) {
			final float forwardStep = pSecondsElapsed/(this.Duration-this.timer);
			
			this.timer += pSecondsElapsed;
			
			final float percentageDone = this.timer/this.Duration;
			
			if(percentageDone >=0.95f)
			{				
				return;
			}
			
			if(!destroyed)
			{
				this.tX = Target.getX();
				this.tY = Target.getY();
			}
			
			this.X = this.X + (this.tX - this.X)*forwardStep;
			this.Y = this.Y + (this.tY - this.Y)*forwardStep;
			
			final float x = X;
			final float d=(2*percentageDone-1);
			final float y = Y + yOffset*(-(d*d)+1);
			
			setRotation((float) Math.toDegrees(-Math.atan2(x-getX(),y-getY())));
			setPosition(x, y);
			
			if(this.timer > pSecondsElapsed)
				batch.draw(sprite, getX()-8,getY()-8,8, 8, 16, 16, 1, 1, Projectile.this.getRotation());
		}
		
	}
	
	public void draw (SpriteBatch batch, float parentAlpha) {
		if(!exploded)
		{
			if(GameScreen.box_accu < GameScreen.BOX_STEP)
				moveModifier.onManagedUpdate(batch, Gdx.graphics.getDeltaTime());
			else
		        batch.draw(sprite, getX()-8,getY()-8,8, 8, 16, 16, 1, 1, Projectile.this.getRotation());
		}
		else
		{
			particleEffect.setPosition(getX(),getY());
		    particleEffect.draw(batch, Gdx.graphics.getDeltaTime());
		}
		
		if(GameScreen.stepNow)
		{
			if(Target.Health <=0)
				destroyed = true;
			
			stepTime += GameScreen.BOX_STEP;
			
			if(stepTime > TravelTime && !exploded)
				explode();
			if(exploded && particleEffect.isComplete())
			{
				this.setVisible(false);
		    	projectilepool.free(this);
			}
		}
	}
	
	void Init(Unit inst, Unit targ, int dmg, int type)
	{   
		sprite.setRegion(Resources.ProjectileTextureRegion[type]);
		this.setVisible(true); 	
		this.toFront();
		Instigator = inst;
		Target = targ;
		setPosition(Instigator.getX(), Instigator.getY());
		float x=Target.getX()-Instigator.getX();
		float y=Target.getY()-Instigator.getY();
		TravelTime = (float)Math.sqrt(x * x + y * y) / PlayerWeapon.Speed[type];
		destroyed = false;
		exploded = false;
		stepTime = 0;
		
		Damage = dmg;
		
		if(type<4){
			moveModifier.Init(TravelTime, 32);
		}
		else if(type>=6 && type<12){
			moveModifier.Init(TravelTime, 64);
		}
		else
		{
			moveModifier.Init(TravelTime,0);
		}
	}
	
    void explode()
    {
    	if(!destroyed)
    	{
    		Target.TakeDamage(Damage, Instigator);
    		exploded = true;
    		particleEffect.start();
    	}
    	else
    	{
			this.setVisible(false);
	    	projectilepool.free(this);
    	}
    }
}
