package com.battleships.base;

import java.util.Comparator;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;

public class GameScreen implements Screen {
	static Stage hudStage;
	static Stage gameStage;
	private Actor touchpad;
	static PlayerShip localPlayerShip;
	protected GameLoopUpdateHandler GLUH;
	private Box2DDebugRenderer debugRenderer;
	
	static World physicsWorld = null; 
	static final float BOX_STEP=1f/10f;
	static float box_accu=0;
    static final int BOX_VELOCITY_ITERATIONS=1;  
    static final int BOX_POSITION_ITERATIONS=1;  
    static final float WORLD_TO_BOX=0.010f;
    static final float BOX_TO_WORLD=100.0f;
    static boolean debug_mode=false;
    
	private SpriteBatch batch;
	private BitmapFont font;
	private SpriteBatch fontBatch;
	private ShaderProgram shader;
	static OrthographicCamera camera;
    
    private float m_fboScaler = 0.1f;
    private FrameBuffer m_fbo = null;
    private TextureRegion m_fboRegion = null;
	static int w;
	static int h;
	static int centerOffsetY = 0;
	private GL20 gl;
	private boolean camToggle = false;
	private Actor Map;
	private ActorPositionComparator ActorComparator;
	protected Vector2 localPlayerDirection;
	private Vector2 knobOffset = new Vector2();
	private Shop weaponShop;
	static boolean stepNow;
	static Actor miniMap;
	static String LocalPlayerTeam;
	
	static int BlueFrags = 0;
	static int RedFrags = 0;
	
	public void loadMapData()
	{
		BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.StaticBody; 
        bodyDef.position.set(0,0);  
		Body WorldCollisionBody = GameScreen.physicsWorld.createBody(bodyDef);
		
		TiledMap map = new TmxMapLoader().load("data/BattleShipsCollision.tmx");
		Iterator<MapLayer> layerIterator = map.getLayers().iterator();
		MapObjects objectGroup = layerIterator.next().getObjects();
		for (MapObject currentObject : objectGroup) {
			if((RectangleMapObject)currentObject != null)
			{
				Rectangle current = ((RectangleMapObject)currentObject).getRectangle();
				PolygonShape staticRectangle = new PolygonShape();
				staticRectangle.setAsBox(current.width/2*GameScreen.WORLD_TO_BOX, current.height/2*GameScreen.WORLD_TO_BOX,
						new Vector2((current.x-1024 +current.width/2)*GameScreen.WORLD_TO_BOX,
								(current.y-1024 +current.height/2)*GameScreen.WORLD_TO_BOX), 0);
		        FixtureDef fixtureDef = new FixtureDef();  
		        fixtureDef.shape = staticRectangle;  
		        fixtureDef.density = 1.0f;  
		        fixtureDef.friction = 0.0f;  
		        fixtureDef.restitution = 0.0f;
		        WorldCollisionBody.createFixture(fixtureDef);  
			}
	    }
		
		
			objectGroup = layerIterator.next().getObjects();
			for (MapObject currentObject : objectGroup) {
				if((RectangleMapObject)currentObject != null)
				{
					Rectangle current = ((RectangleMapObject)currentObject).getRectangle();
				
				if(current.y>1024)
					new Tower("blue",current.x-1024+16,-current.y+1024+32);
				else
					new Tower("red",current.x-1024+16,-current.y+1024+32);
				}
			}
			
		new Citadel("blue",0,-900);
		new Citadel("red",0,900);
	}
	
	public void loadPlayers()
	{
			LocalPlayerTeam = "blue";
			localPlayerShip = new PlayerShip("blue", 0f,-768f, 3);
	}
	
	public GameScreen() {	
		
		if(Gdx.graphics.getWidth() > Gdx.graphics.getHeight())
		{
			w = 480*Gdx.graphics.getWidth()/Gdx.graphics.getHeight();
			h = 480;
		}
		else
		{
			w = 480;
			h = 480*Gdx.graphics.getHeight()/Gdx.graphics.getWidth();
		}
		
		gl = Gdx.graphics.getGL20();
	    gl.glEnable(GL20.GL_TEXTURE_2D);
	    gl.glActiveTexture(GL20.GL_TEXTURE0);
	    gl.glClearColor(0f,0f,0f,1f);
	    
	    font = new BitmapFont(Gdx.files.internal("data/font.fnt"),Gdx.files.internal("data/font.png"),false);
	    font.setScale(1);
	    fontBatch = new SpriteBatch();
	    fontBatch.setProjectionMatrix(new OrthographicCamera(Gdx.graphics.getWidth(),Gdx.graphics.getHeight()).combined);
		
	    if(physicsWorld == null)
	    physicsWorld = new World(new Vector2(0, 0), true); 
		physicsWorld.setAutoClearForces(true);
		physicsWorld.setContinuousPhysics(false);
		physicsWorld.setWarmStarting(true);
		
		ActorComparator = new ActorPositionComparator();

		hudStage = new Stage(w,h,true){
			@Override
			   public boolean keyDown(int keycode) {
			        if(keycode == Keys.BACK || keycode == Keys.ESCAPE){
			        	
						BaseGame.instance.getScreen().dispose();		        	
			        	BaseGame.instance.setScreen(new MenuScreen());
			        	return true;
			        }
			        return super.keyDown(keycode);
			   }
		};
		gameStage = new Stage(w,h,true);
		Gdx.input.setInputProcessor(hudStage);
		
		Map = new Actor(){
			Texture region = Resources.mapTexture;
	        public void draw (SpriteBatch batch, float parentAlpha) {
	                batch.draw(region, -1024,-1024,2048,2048);
	        }
		};
		gameStage.addActor(Map);

		miniMap = new Actor(){
			Texture region = Resources.miniMapTexture;
	        public void draw (SpriteBatch batch, float parentAlpha) {
        		batch.setColor(1, 1, 1, 0.75f);
	            batch.draw(region,getX(),getY(),getWidth(),getHeight());
	            batch.setColor(1, 1, 1, 1);
	        }
		};
		camToggle = true;
		miniMap.setTouchable(Touchable.enabled);
		miniMap.addListener(new InputListener() {
	        public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
	        	setCamPos(x,y);
	        	camToggle = false;
	                return true;
	        }
	        
	        public void touchDragged (InputEvent event, float x, float y, int pointer) {
	        	setCamPos(x,y);
	        	camToggle = false;
	    	}
	        
	        /*public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
	        	camToggle = true;
	        }*/
	        
	        void setCamPos(float x, float y)
	        {
	        	gameStage.getCamera().position.set(
				MathUtils.clamp((x-miniMap.getWidth()/2)*(2048/miniMap.getWidth()),-1024+w/2, 1024-w/2), 
				MathUtils.clamp((y-miniMap.getHeight()/2)*(2048/miniMap.getHeight()),-1024+h/2, 1024-h/2), 0);
	        	gameStage.getCamera().update();
	        }
		});
		miniMap.setBounds(w-h*0.4f, h*0.6f, h*0.4f, h*0.4f);
		
		miniMap.toFront();
		hudStage.addActor(miniMap);
		
		float knobsize = h*0.4f;
		if(w<h)
		{
			knobsize = w*0.6f;
			centerOffsetY = (int)(w*0.25f);
			miniMap.setBounds(0, w*0.1f, w*0.4f, w*0.4f);
		}
		
		touchpad = new Actor(){
			int rotation = 0;
			int desiredRotation =0;
			Vector2 offset = new Vector2();
			
	        public void draw (SpriteBatch batch, float parentAlpha) {
	        	offset.set(localPlayerDirection);
	        	if(offset.len() > 0.1f)
	        	{
	        		offset.x /= offset.len()/0.1f;
	        		offset.y /= offset.len()/0.1f;
	        	}
	        	
        		batch.setColor(1, 1, 1, 0.75f);
        		if(offset.len()>0)
        		{
    	        	camToggle = true;
        			desiredRotation = (int) Math.toDegrees(-Math.atan2(offset.x,offset.y));
	        		float DeltaAngle = (float) (desiredRotation-rotation);
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
        			if(DeltaAngle>0)
        			{
        				rotation += Math.min(Gdx.graphics.getDeltaTime() * 180,DeltaAngle);
        			}
        			else
        			{
        				rotation += Math.max(Gdx.graphics.getDeltaTime() * -180,DeltaAngle);
        			}
        		}
        		
	            batch.draw(Resources.touchpadBase,getX(),getY(),getWidth()/2,getHeight()/2,
	            		getWidth(),getHeight(), 1, 1, rotation, 
	            		0, 0, 256,256, false, false);
	            
	            batch.draw(Resources.touchpadKnob,
	            		getX()+getWidth()*(0.25f + offset.x),
	            		getY()+getHeight()*(0.25f + offset.y),
	            		getWidth()/2,getHeight()/2);
	            batch.setColor(1, 1, 1, 1);
	        }
		};
		touchpad.setTouchable(Touchable.enabled);
		touchpad.addListener(new InputListener() {
	        public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
	        	knobOffset.x = (x-touchpad.getWidth()/2)/(touchpad.getWidth());
	        	knobOffset.y = (y-touchpad.getHeight()/2)/(touchpad.getWidth());
	        	return true;
	        }
	        
	        public void touchDragged (InputEvent event, float x, float y, int pointer) {
	        	knobOffset.x = (x-touchpad.getWidth()/2)/(touchpad.getWidth());
	        	knobOffset.y = (y-touchpad.getHeight()/2)/(touchpad.getWidth());
	    	}
	        
	        public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
	        	knobOffset.set(0,0);
	        }
		});
		
		touchpad.setBounds(w-knobsize,0, knobsize, knobsize);
		hudStage.addActor(touchpad);
		
		Actor debugToggle = new Actor();
		debugToggle.setTouchable(Touchable.enabled);
		debugToggle.addListener(new InputListener() {
	        public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
	        	debug_mode = !debug_mode;
	        	return true;
	        }
		});
		
		debugToggle.setBounds(0,h*0.9f, w*0.1f, h*0.1f);
		hudStage.addActor(debugToggle);
		
		loadPlayers();
		
		localPlayerDirection = new Vector2();
		weaponShop = new Shop(localPlayerShip);
		
		batch = new SpriteBatch();
		camera = new OrthographicCamera(w, h);
	    camera.position.set(0,0,0);
	    camera.update();
	    batch.setProjectionMatrix(camera.combined);
	    ShaderProgram.pedantic=true;
	    shader = createFowShader();
	    if(!shader.isCompiled()) {
	        Gdx.app.log("Problem loading shader:", shader.getLog());
	    }
	    batch.setShader(shader);
	    
		GLUH = new GameLoopUpdateHandler();
		
        debugRenderer = new Box2DDebugRenderer(); 
        
        loadMapData();
	}
	
	public ShaderProgram createFowShader () {
		String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
			+ "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
			+ "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
			+ "uniform mat4 u_projTrans;\n" //
			+ "varying vec4 v_color;\n" //
			+ "varying vec2 v_texCoords;\n" //
			+ "\n" //
			+ "void main()\n" //
			+ "{\n" //
			+ "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
			+ "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
			+ "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
			+ "}\n";
		String fragmentShader = "#ifdef GL_ES\n" //
			+ "#define LOWP lowp\n" //
			+ "precision mediump float;\n" //
			+ "#else\n" //
			+ "#define LOWP \n" //
			+ "#endif\n" //
			+ "varying LOWP vec4 v_color;\n" //
			+ "varying vec2 v_texCoords;\n" //
			+ "uniform sampler2D u_texture;\n" //
			+ "void main()\n"//
			+ "{\n" //
			+ "  gl_FragColor = vec4(0,0,0,(1.0-texture2D(u_texture, v_texCoords).r)*0.75);\n" //
			+ "}";

		ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
		if (shader.isCompiled() == false) throw new IllegalArgumentException("couldn't compile shader: " + shader.getLog());
		return shader;
	}
	
	void renderFow()
	{
		if(m_fbo == null)
		{
			m_fbo = new FrameBuffer(Format.RGB565, (int)(w * m_fboScaler), (int)(h * m_fboScaler), false);
		    m_fboRegion = new TextureRegion(m_fbo.getColorBufferTexture());
		    m_fboRegion.flip(false, true);
		}
		m_fbo.begin();
	    gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();
		for (Visor current : Visor.VisorList)
		{
		   current.draw(batch);
		}
		batch.end();
		
		if(m_fbo != null)
	    {
	        m_fbo.end();

		    batch.setShader(shader);
	        batch.begin();         
	        batch.draw(m_fboRegion, -w/2-1, -h/2-1, w+2, h+2);               
	        batch.end();
		    batch.setShader(null);
	    }
	}

	@Override
	public void dispose() {
		hudStage.dispose();
		gameStage.dispose();
		Unit.bodyPool.clear();
		
		Body current;
		int bodies = physicsWorld.getBodyCount();
		for(int i=0; i<bodies; i++)
		{
			current = physicsWorld.getBodies().next();
			int fixtures = current.getFixtureList().size();
			for(int j=0; j<fixtures; j++)
			{
				current.destroyFixture(current.getFixtureList().get(0));
			}
			physicsWorld.destroyBody(current);
		}
		Unit.unitSpawnNumber = 0;
		Iterator<Visor> iter = Visor.VisorList.iterator();
		while(iter.hasNext())
		{
			iter.next();
			iter.remove();
		}
		BlueFrags=0;
		RedFrags=0;
	}
	
	class ActorPositionComparator implements Comparator<Actor>{
		 
	    public strictfp int compare(Actor emp1, Actor emp2){ 
	    	if(emp1 instanceof Projectile && emp2 instanceof Projectile)
	    		return 0;
	    		
	    	if(emp1 instanceof Projectile)
	    		return 1;

	    	if(emp2 instanceof Projectile)
	    		return -1;
	    	
	    	if(emp1.getY() < emp2.getY())
		        return 1;
		    else if(emp1.getY() > emp2.getY())
		        return -1;
		    else if(emp1 instanceof Unit && emp2 instanceof Unit)
		    {
		    	if(((Unit)emp1).unitID < ((Unit)emp2).unitID)
		    		return 1;
		    	else
		    		return -1;
		    }
		    else   	return 0;
	    }
	 
	}
	
	public void worldStep(float delta)
	{
		box_accu+=Math.min(delta, BOX_STEP);
		stepNow = false;
		
		if(box_accu>BOX_STEP)
		{
			stepNow = true;
			
			if(!debug_mode)
				GLUH.onUpdate(BOX_STEP);
    		
			physicsWorld.step(BOX_STEP, BOX_VELOCITY_ITERATIONS, BOX_POSITION_ITERATIONS);
			
			localPlayerShip.setDesiredVelocity(localPlayerDirection);
			
			box_accu = 0;
		}
	}

	@Override
	public void render(float delta) {
		localPlayerDirection.set(knobOffset);
		
		if( Gdx.input.isKeyPressed( Input.Keys.UP ) || Gdx.input.isKeyPressed( Input.Keys.W ) ) localPlayerDirection.y = 1;
		else if( Gdx.input.isKeyPressed( Input.Keys.DOWN ) || Gdx.input.isKeyPressed( Input.Keys.S ) ) localPlayerDirection.y = -1;
		if( Gdx.input.isKeyPressed( Input.Keys.LEFT ) || Gdx.input.isKeyPressed( Input.Keys.A ) ) localPlayerDirection.x = -1;
		else if( Gdx.input.isKeyPressed( Input.Keys.RIGHT ) || Gdx.input.isKeyPressed( Input.Keys.D ) ) localPlayerDirection.x = 1;

		if(camToggle && localPlayerShip.Health>0)
		gameStage.getCamera().position.set(
				MathUtils.clamp(localPlayerShip.CurrentPosition.x, -1024+w/2, 1024-w/2), 
				MathUtils.clamp(localPlayerShip.CurrentPosition.y-centerOffsetY, -1024+h/2, 1024-h/2), 0);
		gameStage.getCamera().update();
		
		worldStep(delta);
		
		if(stepNow)
			gameStage.getRoot().getChildren().sort(ActorComparator);
		
		Map.toBack();		

	    camera.position.set(gameStage.getCamera().position);
	    camera.update();

	    gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		gameStage.draw();
		renderFow(); 

		if(debug_mode)
		{
			Matrix4 debugMatrix = gameStage.getCamera().combined.cpy();
			debugMatrix.scale(BOX_TO_WORLD, BOX_TO_WORLD, 1);
	        debugRenderer.render(physicsWorld, debugMatrix); 
		}
		hudStage.draw();
		
		fontBatch.begin();  
		font.setColor(Color.WHITE);
		font.draw(fontBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), -Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2); 
		font.setColor(1,0.9f,0,1);
		font.draw(fontBatch, localPlayerShip.PlayerGold+"$", -font.getBounds(localPlayerShip.PlayerGold+".").width/2, Gdx.graphics.getHeight()/2 - font.getLineHeight()); 
		
		font.setColor(Color.BLUE);
		font.draw(fontBatch, BlueFrags+" ", -font.getBounds(BlueFrags+" ").width, Gdx.graphics.getHeight()/2);
		font.setColor(Color.WHITE);
		font.draw(fontBatch, ":", 0, Gdx.graphics.getHeight()/2); 
		font.setColor(Color.RED);
		font.draw(fontBatch, " "+RedFrags, font.getSpaceWidth(), Gdx.graphics.getHeight()/2); 
		
		if(weaponShop.shop_toggle)
		{
			float lineScale = Gdx.graphics.getHeight()/480f;
			font.setColor(Color.WHITE);
			font.draw(fontBatch, "PRICE:", -64, Gdx.graphics.getHeight()/2 - font.getLineHeight()*3*lineScale); 
			font.draw(fontBatch, "DAMAGE:", -64, Gdx.graphics.getHeight()/2 - font.getLineHeight()*4*lineScale); 
			font.draw(fontBatch, "COOLDOWN:", -64, Gdx.graphics.getHeight()/2 - font.getLineHeight()*5*lineScale); 
			font.draw(fontBatch, "RANGE:", -64, Gdx.graphics.getHeight()/2 - font.getLineHeight()*6*lineScale); 
			
			font.setColor(Color.RED);
			font.draw(fontBatch, PlayerWeapon.CostData[weaponShop.selected_item]+"$", 64-font.getBounds(PlayerWeapon.CostData[weaponShop.selected_item]+"&").width, Gdx.graphics.getHeight()/2 - font.getLineHeight()*3*lineScale); 
			font.draw(fontBatch, PlayerWeapon.DamageData[weaponShop.selected_item]+"", 64-font.getBounds(PlayerWeapon.DamageData[weaponShop.selected_item]+"").width, Gdx.graphics.getHeight()/2 - font.getLineHeight()*4*lineScale); 
			font.draw(fontBatch, PlayerWeapon.FireDelayData[weaponShop.selected_item]+"s", 64-font.getBounds(PlayerWeapon.FireDelayData[weaponShop.selected_item]+"s").width, Gdx.graphics.getHeight()/2 - font.getLineHeight()*5*lineScale); 
			font.draw(fontBatch, PlayerWeapon.RangeData[weaponShop.selected_item]+"m", 64-font.getBounds(PlayerWeapon.RangeData[weaponShop.selected_item]+"m").width, Gdx.graphics.getHeight()/2 - font.getLineHeight()*6*lineScale); 
		}
		fontBatch.end();
	}

	@Override
	public void resize(int width, int height) {}

	@Override
	public void pause() {}

	@Override
	public void resume() {}

	@Override
	public void hide() {}

	@Override
	public void show() {}
}
