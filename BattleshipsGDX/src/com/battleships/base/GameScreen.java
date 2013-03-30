package com.battleships.base;

import java.util.Comparator;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
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
	
	static World physicsWorld; 
	static final float BOX_STEP=1f/10f;
	static float box_accu=0;
    static final int BOX_VELOCITY_ITERATIONS=6;  
    static final int BOX_POSITION_ITERATIONS=2;  
    static final float WORLD_TO_BOX=0.01f;  
    static final float BOX_WORLD_TO=100.0f; 
    static boolean debug_mode=false;
    
	private SpriteBatch batch;
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
	private BitmapFont font;
	private Vector2 camOffset;
	private Vector2 knobOffset = new Vector2();
	static boolean stepNow;
	static Actor miniMap;
	static String LocalPlayerTeam;
	
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
	}
	
	public void loadPlayers()
	{
			LocalPlayerTeam = "blue";
			localPlayerShip = new PlayerShip("blue", 0f,-768f, 3);
			System.out.println("wtf");
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
		
		physicsWorld = new World(new Vector2(0, 0), true); 
		
		ActorComparator = new ActorPositionComparator();

		
		hudStage = new Stage(w,h,true);
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
        		batch.setColor(1, 1, 1, 0.5f);
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
	        
	        public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
	        	camToggle = true;
	        }
	        
	        void setCamPos(float x, float y)
	        {
	        	gameStage.getCamera().position.set(
				MathUtils.clamp((x-miniMap.getWidth()/2)*(2048/miniMap.getWidth()),-1024+w/2, 1024-w/2), 
				MathUtils.clamp((y-miniMap.getHeight()/2)*(2048/miniMap.getHeight()),-1024+h/2, 1024-h/2), 0);
	        	gameStage.getCamera().update();
	        }
		});
		if(w>h)
			miniMap.setBounds(w*0.76f, h-w*0.24f, w*0.23f, w*0.23f);
		
		miniMap.toFront();
		hudStage.addActor(miniMap);
		
		float knobsize = w*0.25f;
		if(w<h)
		{
			knobsize = w*0.5f;
			centerOffsetY = (int)(w*0.25f);
		}
		
		touchpad = new Actor(){
	        public void draw (SpriteBatch batch, float parentAlpha) {
        		batch.setColor(1, 1, 1, 0.5f);
	            batch.draw(Resources.touchpadBase,getX(),getY(),getWidth(),getHeight());
	            batch.draw(Resources.touchpadKnob,
	            		getX()+getWidth()*(0.25f + knobOffset.x),
	            		getY()+getHeight()*(0.25f + knobOffset.y),
	            		getWidth()/2,getHeight()/2);
	            batch.setColor(1, 1, 1, 1);
	        }
		};
		touchpad.setTouchable(Touchable.enabled);
		touchpad.addListener(new InputListener() {
	        public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
	        	knobOffset.x = (x-touchpad.getWidth()/2)/(touchpad.getWidth());
	        	knobOffset.y = (y-touchpad.getHeight()/2)/(touchpad.getWidth());
	        	if(knobOffset.len() > 0.3f)
	        	{
		        	knobOffset.x /= knobOffset.len()/0.3f;
		        	knobOffset.y /= knobOffset.len()/0.3f;
	        	}
	        	
	        	return true;
	        }
	        
	        public void touchDragged (InputEvent event, float x, float y, int pointer) {
	        	knobOffset.x = (x-touchpad.getWidth()/2)/(touchpad.getWidth());
	        	knobOffset.y = (y-touchpad.getHeight()/2)/(touchpad.getWidth());
	        	if(knobOffset.len() > 0.3f)
	        	{
		        	knobOffset.x /= knobOffset.len()/0.3f;
		        	knobOffset.y /= knobOffset.len()/0.3f;
	        	}
	    	}
	        
	        public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
	        	knobOffset.set(0,0);
	        }
		});
		
		if(w>h)
			touchpad.setBounds(w-knobsize,0, knobsize, knobsize);
		else
			touchpad.setBounds(knobsize/2,0, knobsize, knobsize);
		hudStage.addActor(touchpad);
		
		loadPlayers();
		
		localPlayerDirection = new Vector2();
		new Shop(localPlayerShip);
		
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
		physicsWorld.dispose();
	}
	
	class ActorPositionComparator implements Comparator<Actor>{
		 
	    public int compare(Actor emp1, Actor emp2){ 
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
	        else
	        	return 0;
	    }
	 
	}
	
	void update()
	{
		Unit current;
		Iterator<Unit> iter = Unit.AllUnits.iterator();
		while(iter.hasNext())
		{
			current = iter.next();
			current.onUpdate(BOX_STEP/2);
			
			if(current.Health<=0)
			{
				current.Destroy();
				if(!(current instanceof PlayerShip))
					iter.remove();
			}
		}
	}
	
	public void worldStep(float delta)
	{
		box_accu+=Math.min(delta, BOX_STEP);
		stepNow = false;
		
		if(box_accu - delta <BOX_STEP/2f && box_accu > BOX_STEP/2f)
			update();
		
		if(box_accu>BOX_STEP)
		{
			stepNow = true;
			
    		GLUH.onUpdate(BOX_STEP);
    		
    		update();
    		
			physicsWorld.step(BOX_STEP, BOX_VELOCITY_ITERATIONS, BOX_POSITION_ITERATIONS);
			
			localPlayerShip.setDesiredVelocity(localPlayerDirection.x, localPlayerDirection.y);
			
			box_accu = 0;
		}
	}

	@Override
	public void render(float delta) {
		localPlayerDirection.set(knobOffset);
		
		if(BaseGame.gamepad == null)
		{
			if( Gdx.input.isKeyPressed( Input.Keys.UP ) || Gdx.input.isKeyPressed( Input.Keys.W ) ) localPlayerDirection.y = 1;
			else if( Gdx.input.isKeyPressed( Input.Keys.DOWN ) || Gdx.input.isKeyPressed( Input.Keys.S ) ) localPlayerDirection.y = -1;
			if( Gdx.input.isKeyPressed( Input.Keys.LEFT ) || Gdx.input.isKeyPressed( Input.Keys.A ) ) localPlayerDirection.x = -1;
			else if( Gdx.input.isKeyPressed( Input.Keys.RIGHT ) || Gdx.input.isKeyPressed( Input.Keys.D ) ) localPlayerDirection.x = 1;

			if(camToggle && localPlayerShip.Health>0)
			gameStage.getCamera().position.set(
					MathUtils.clamp(localPlayerShip.getX(), -1024+w/2, 1024-w/2), 
					MathUtils.clamp(localPlayerShip.getY()-centerOffsetY, -1024-h/2, 1024+h/2), 0);
			gameStage.getCamera().update();
		}
		else
		{
			if(Math.abs(BaseGame.gamepad.getAxis(1))>0.1f)
			{
				localPlayerDirection.x = BaseGame.gamepad.getAxis(1);
				camOffset.set(camOffset.x*0.9f, camOffset.y*0.9f);
			}
			if(Math.abs(BaseGame.gamepad.getAxis(0))>0.1f)
			{
				localPlayerDirection.y = -BaseGame.gamepad.getAxis(0);
				camOffset.set(camOffset.x*0.9f, camOffset.y*0.9f);
			}

			if(Math.abs(BaseGame.gamepad.getAxis(3))>0.1f)
				camOffset.x += BaseGame.gamepad.getAxis(3) * delta * 512;

			if(Math.abs(BaseGame.gamepad.getAxis(2))>0.1f)
				camOffset.y -= BaseGame.gamepad.getAxis(2) * delta * 512;

			gameStage.getCamera().position.set(
				MathUtils.clamp(localPlayerShip.getX() + camOffset.x,-1024+w/2, 1024-w/2), 
				MathUtils.clamp(localPlayerShip.getY() + camOffset.y,-1024+h/2, 1024-h/2), 0);
			gameStage.getCamera().update();
		}
		
		worldStep(delta);
		
		gameStage.getRoot().getChildren().sort(ActorComparator);
		Map.toBack();		

	    camera.position.set(gameStage.getCamera().position);
	    camera.update();

	    gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		gameStage.act(Gdx.graphics.getDeltaTime());
		gameStage.draw();		

		renderFow(); 

		if(debug_mode)
		{
			Matrix4 debugMatrix = gameStage.getCamera().combined.cpy();
			debugMatrix.scale(BOX_WORLD_TO, BOX_WORLD_TO, 1);
	        debugRenderer.render(physicsWorld, debugMatrix); 
		}
		
		hudStage.act(Gdx.graphics.getDeltaTime());
		hudStage.draw();
		
		batch.begin();  
		font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), -w/2, h*0.5f); 
		font.draw(batch, "GOLD: " + localPlayerShip.PlayerGold, -w/2, h*0.47f); 
		batch.end();
    
	}

	@Override
	public void resize(int width, int height) {
		//hudStage.setViewport(width, height, true);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}
}
