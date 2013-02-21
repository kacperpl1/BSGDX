package com.battleships.base;

import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.tiled.TiledLoader;
import com.badlogic.gdx.graphics.g2d.tiled.TiledMap;
import com.badlogic.gdx.graphics.g2d.tiled.TiledObject;
import com.badlogic.gdx.graphics.g2d.tiled.TiledObjectGroup;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
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
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad.TouchpadStyle;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class BaseGame implements ApplicationListener {
	static Stage hudStage;
	static Stage gameStage;
	private Touchpad touchpad;
	private PlayerShip PlayerShip;
	private GameLoopUpdateHandler GLUH;
	private Box2DDebugRenderer debugRenderer;
	private FPSLogger fpsLogger;
	
	static World physicsWorld; 
	static final float BOX_STEP=1/60f;  
    static final int BOX_VELOCITY_ITERATIONS=6;  
    static final int BOX_POSITION_ITERATIONS=2;  
    static final float WORLD_TO_BOX=0.01f;  
    static final float BOX_WORLD_TO=100.0f; 
    
    long frameTime = System.nanoTime();
    long lastFrameTime = System.nanoTime();
	private SpriteBatch batch;
	private ShaderProgram shader;
	static OrthographicCamera camera;
    static float delta;
    
    public static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    
    private float m_fboScaler = 0.1f;
    private FrameBuffer m_fbo = null;
    private TextureRegion m_fboRegion = null;
	static int w;
	static int h;
	private GL20 gl;
	private boolean camToggle;
	private Actor Map;
	static Actor miniMap;
	static String LocalPlayerTeam;
	
	@Override
	public void create() {	
		
		Resources.init();
		
		w = 480*Gdx.graphics.getWidth()/Gdx.graphics.getHeight();// Gdx.graphics.getWidth();
		h = 480;// Gdx.graphics.getHeight();
		
		gl = Gdx.graphics.getGL20();
	    gl.glEnable(GL20.GL_TEXTURE_2D);
	    gl.glActiveTexture(GL20.GL_TEXTURE0);
	    gl.glClearColor(0f,0f,0f,1f);
		
		physicsWorld = new World(new Vector2(0, 0), true); 

		BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.StaticBody; 
        bodyDef.position.set(0,0);  
		Body WorldCollisionBody = BaseGame.physicsWorld.createBody(bodyDef);
		
		TiledMap map = TiledLoader.createMap(Gdx.files.internal("data/BattleShipsCollision.tmx"));
		TiledObjectGroup objectGroup = map.objectGroups.get(0);
		for (TiledObject current : objectGroup.objects) {
			
			
			PolygonShape staticRectangle = new PolygonShape();
			staticRectangle.setAsBox(current.width/2*BaseGame.WORLD_TO_BOX, current.height/2*BaseGame.WORLD_TO_BOX,
					new Vector2((current.x-1024 +current.width/2)*BaseGame.WORLD_TO_BOX,
							(-current.y+1024 -current.height/2)*BaseGame.WORLD_TO_BOX), 0);
	        FixtureDef fixtureDef = new FixtureDef();  
	        fixtureDef.shape = staticRectangle;  
	        fixtureDef.density = 1.0f;  
	        fixtureDef.friction = 0.0f;  
	        fixtureDef.restitution = 0.0f;
	        WorldCollisionBody.createFixture(fixtureDef);  
	    }
		
        fpsLogger = new FPSLogger();
        
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
				Math.max(Math.min((x-miniMap.getWidth()/2)*(2048/miniMap.getWidth()), 1024-w/2),-1024+w/2), 
				Math.max(Math.min((y-miniMap.getHeight()/2)*(2048/miniMap.getHeight()), 1024-h/2),-1024+h/2), 0);
	        	gameStage.getCamera().update();
	        }
		});
		miniMap.setBounds(w*0.76f, h-w*0.24f, w*0.23f, w*0.23f);
		hudStage.addActor(miniMap);
		
		TouchpadStyle style = new TouchpadStyle();
		style.background = new SpriteDrawable(new Sprite(Resources.touchpadBase));
		style.knob = new SpriteDrawable(new Sprite(Resources.touchpadKnob));
		float knobsize = w*0.25f;
		style.knob.setMinWidth(w*0.25f/2);
		style.knob.setMinHeight(w*0.25f/2);
		touchpad = new Touchpad(1, style);
		touchpad.setBounds(w-knobsize,0, knobsize, knobsize);
		hudStage.addActor(touchpad);
		
		LocalPlayerTeam = "blue";
		PlayerShip = new PlayerShip("blue", 0f,-768f, Color.CYAN);
		new Shop(PlayerShip);
		
		new PlayerShip("red", 0f,768f, Color.ORANGE);
		
		objectGroup = map.objectGroups.get(1);
		for (TiledObject current : objectGroup.objects) {
			
			if(current.y>1024)
				new Tower("blue",current.x-1024+16,-current.y+1024);
			else
				new Tower("red",current.x-1024+16,-current.y+1024);
	    }
		
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
	        batch.draw(m_fboRegion, -w/2, -h/2, w, h);               
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

	@Override
	public void render() {	
		
		frameTime = System.nanoTime();
	    delta = (frameTime - lastFrameTime) / 1000000000.0f;
	    lastFrameTime = System.nanoTime();
	    
		GLUH.onUpdate(delta);
		
		PlayerShip.setDesiredVelocity(touchpad.getKnobPercentX(), touchpad.getKnobPercentY());
		physicsWorld.step(delta, BOX_VELOCITY_ITERATIONS, BOX_POSITION_ITERATIONS);  
		
		gameStage.getRoot().getChildren().sort(new ActorPositionComparator());
		Map.toBack();

		if(camToggle)
		gameStage.getCamera().position.set(
				Math.max(Math.min(PlayerShip.getX(), 1024-w/2),-1024+w/2), 
				Math.max(Math.min(PlayerShip.getY(), 1024-h/2),-1024+h/2), 0);
		gameStage.getCamera().update();
		

	    camera.position.set(gameStage.getCamera().position);
	    camera.update();

	    gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		gameStage.act(Gdx.graphics.getDeltaTime());
		gameStage.draw();		

		renderFow(); 
		
		hudStage.act(Gdx.graphics.getDeltaTime());
		hudStage.draw();
		
        //fpsLogger.log();
        
        //debugRenderer.render(physicsWorld, gameStage.getCamera().combined); 
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
}
