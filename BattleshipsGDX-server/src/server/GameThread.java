package server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import shared.UnitData;
import shared.UnitMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

class GameThread extends Thread{
	private ServerGame game;
	private volatile boolean running;
	
	World physicsWorld;
	private ContactListener contactListener;
	
	static final float BOX_STEP=1f/10f;
    static final int BOX_VELOCITY_ITERATIONS=6;  
    static final int BOX_POSITION_ITERATIONS=2;  
    static final float WORLD_TO_BOX=0.01f;  
    static final float BOX_WORLD_TO=100.0f; 
    
	static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
	
	public UnitMap unitMap = new UnitMap();
	Map<Short, Stack<UnitData>> stackMap = new HashMap<Short, Stack<UnitData>>();
	private server.GameLoopUpdateHandler GLUH;
	
	private Map<Short, UnitData> playerShipMap = new HashMap<Short, UnitData>();
	
	public GameThread(ServerGame game){
		this.game = game;
		this.running = true;
		this.setName(game.getId());
		
		
		
		for(int i = 0; i < game.getPlayerList().size(); i++) {
			stackMap.put(game.getPlayerList().getServerPlayer(i).getSlotNumber(), new Stack<UnitData>());
			UnitData data = new UnitData();
			data.gameID = this.getName();
			data.unitKey = game.getPlayerList().getServerPlayer(i).getSlotNumber();
			playerShipMap.put(game.getPlayerList().getServerPlayer(i).getSlotNumber(), data);
		}
		
//		physicsWorld = new World(new Vector2(0, 0), true);
//		contactListener = createContactListener();
//		physicsWorld.setContactListener(contactListener);
//		GLUH = new GameLoopUpdateHandler(this);
//		
//		BodyDef bodyDef = new BodyDef();
//        bodyDef.type = BodyType.StaticBody; 
//        bodyDef.position.set(0,0);  
//		Body WorldCollisionBody = physicsWorld.createBody(bodyDef);
//		
//		Gdx.files = new LwjglFiles();
//		TiledMap map = new TmxMapLoader().load("data/BattleShipsCollision.tmx");
//		MapObjects objectGroup = map.getLayers().get("Collision").getObjects();
//		for (MapObject currentObject : objectGroup) {
//			if((RectangleMapObject)currentObject != null)
//			{
//				Rectangle current = ((RectangleMapObject)currentObject).getRectangle();
//			PolygonShape staticRectangle = new PolygonShape();
//				staticRectangle.setAsBox(current.width/2*WORLD_TO_BOX, current.height/2*WORLD_TO_BOX,
//						new Vector2((current.x-1024 +current.width/2)*WORLD_TO_BOX,
//								(current.y-1024 +current.height/2)*WORLD_TO_BOX), 0);
//		        FixtureDef fixtureDef = new FixtureDef();  
//		        fixtureDef.shape = staticRectangle;  
//		        fixtureDef.density = 1.0f;  
//		        fixtureDef.friction = 0.0f;  
//		        fixtureDef.restitution = 0.0f;
//		        WorldCollisionBody.createFixture(fixtureDef);  
//			}
//	    }
//		
//		objectGroup = map.getLayers().get("Towers").getObjects();
//		for (MapObject currentObject : objectGroup) {
//			if((RectangleMapObject)currentObject != null)
//			{
//				Rectangle current = ((RectangleMapObject)currentObject).getRectangle();
//			
//				if(current.y>1024) {
//					new Tower("blue",current.x-1024+16,-current.y+1024+32, this);
//				}
//				else {
//					new Tower("red",current.x-1024+16,-current.y+1024+32, this);
//				}
//			}
//	    }
//		short slot;
//		for(int i = 0; i < game.getPlayerList().size(); i++) {
//			slot = game.getPlayerList().getServerPlayer(i).getSlotNumber();
//			if( slot < 3) {
//				Unit player = new PlayerShip("red", 0f, 768f, this, slot);
//				playerShipMap.put(player.hashCode(), player);
//			} else {
//				Unit player = new PlayerShip("blue", 0f, -768f, this, slot);
//				playerShipMap.put(player.hashCode(), player);
//			}
//		}
//		
//		for(int i = 0; i < game.getPlayerList().size(); i++) {
//			game.getPlayerList().getServerPlayer(i).getConnection().sendUDP(unitMap);
//		}
	}
	
	public void run(){
		System.out.println("game " + game.getName() + " started");
		while(running){
			if(playersAreConnected()) {
				try {
					//physicsWorld.step(BOX_STEP, BOX_VELOCITY_ITERATIONS, BOX_POSITION_ITERATIONS); 
					
					// TODO Fixed step accumulator needed1
					
					//GLUH.onUpdate(0.1f);
//					for (Iterator<Body> iter = physicsWorld.getBodies(); iter.hasNext();) {
//						Unit aux = (Unit) iter.next().getUserData();
//						if(aux != null) {
//							aux.onUpdate(0.1f);
//							aux.updateUnitData();
//						}
//					}
					
					int counter = game.getPlayerList().size();
					
					while(counter > 0) {
						for(int i = 0 ; i < game.getPlayerList().size(); i++) {
							short slot = game.getPlayerList().getServerPlayer(i).getSlotNumber(); 
							if(!stackMap.get(slot).isEmpty()) {
								UnitData message = stackMap.get(slot).pop();
								//System.out.println("got msg from " + slot);
								playerShipMap.get(message.unitKey).direction = message.direction;
								stackMap.get(slot).clear();
								--counter;
							}
						}
					}
					
					for(int i = 0; i < game.getPlayerList().size(); i++) {
						game.getPlayerList().getServerPlayer(i).getConnection().sendUDP(playerShipMap);
					}
					
//					ByteArrayOutputStream bOut = new ByteArrayOutputStream();  
//					ObjectOutputStream oOut = new ObjectOutputStream(bOut);  
//					oOut.writeObject(unitMap);  
//					oOut.close();  
//					System.out.println("The size of the object is: "+bOut.toByteArray().length);  
					
//					for(int i = 0; i < game.getPlayerList().size(); i++) {
//						game.getPlayerList().getServerPlayer(i).getConnection().sendUDP(unitMap);
//					}
//					for (Iterator<Body> iter = physicsWorld.getBodies(); iter.hasNext();) {
//						Unit aux = (Unit) iter.next().getUserData();
//						if(aux != null && aux.Health<=0)
//						{
//								aux.Destroy();
//						}
//					}
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
				}
			} else {
				this.running = false;
			}
		}
		System.out.println("game " + game.getName() + " stopped");
	}
	
	public void stopGame(){
		this.running = false;
	}
	
//	public void movePlayer(UnitMap unitMap) {
//		for(Entry<Integer, UnitData> entry : unitMap.map.entrySet()) {
//			for (Iterator<Body> iter = physicsWorld.getBodies(); iter.hasNext();) {
//				Unit aux = (Unit) iter.next().getUserData();
//				if(aux != null && aux.hashCode() == entry.getKey()) {
//					aux.CollisionBody.setTransform(entry.getValue().position, 0);
//				}
//			}
//		}
//	}
	
	public Stack<UnitData> getMsgStack(short slot) {
		return this.stackMap.get(slot);
		//return this.stackArray.get(slot);
	}
	
	public boolean playersAreConnected() {
		for(int i = 0; i < game.getPlayerList().size(); i++) {
			if(game.getPlayerList().getServerPlayer(i).getConnection().isConnected()) {
				return true;
			}
		}
		return false;
	}
	
	private ContactListener createContactListener() {
	     return new ContactListener()
	     {
	         @Override
	         public void beginContact(Contact contact)
	         {
	             final Fixture x1 = contact.getFixtureA();
	             final Fixture x2 = contact.getFixtureB();
	             if(x2.getUserData() instanceof Unit && x1.getUserData() instanceof Weapon)
	             {
	             	if(!((Weapon)x1.getUserData()).Owner.team.equals(((Unit)(x2.getUserData())).team))
	             		((Weapon)x1.getUserData()).Enemies.add(((Unit)(x2.getUserData())));
	             }
	             if(x1.getUserData() instanceof Unit && x2.getUserData() instanceof Weapon)
	             {
	             	if(!((Weapon)x2.getUserData()).Owner.team.equals(((Unit)(x1.getUserData())).team))
	             		((Weapon)x2.getUserData()).Enemies.add(((Unit)(x1.getUserData())));
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
	 } 
	
}
