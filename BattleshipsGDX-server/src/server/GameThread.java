package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import shared.UnitData;
import shared.UnitMap;

import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.graphics.g2d.tiled.TiledLoader;
import com.badlogic.gdx.graphics.g2d.tiled.TiledMap;
import com.badlogic.gdx.graphics.g2d.tiled.TiledObject;
import com.badlogic.gdx.graphics.g2d.tiled.TiledObjectGroup;
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
	List<Stack<UnitData>> stackArray = new ArrayList<Stack<UnitData>>();
	private server.GameLoopUpdateHandler GLUH;
	
	private Map<Integer, Unit> playerShipMap = new HashMap<Integer, Unit>();
	
	public GameThread(ServerGame game){
		this.game = game;
		this.running = true;
		this.setName(game.getId());
		
		for(int i = 0; i < 6; i++) {
			stackArray.add(null);
		}
		for(int i = 0; i < game.getPlayerList().size(); i++) {
			stackArray.add(game.getPlayerList().getServerPlayer(i).getSlotNumber(), new Stack<UnitData>());
		}
		
		physicsWorld = new World(new Vector2(0, 0), true);
		contactListener = createContactListener();
		physicsWorld.setContactListener(contactListener);
		GLUH = new GameLoopUpdateHandler(this);
		
		BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.StaticBody; 
        bodyDef.position.set(0,0);  
		Body WorldCollisionBody = physicsWorld.createBody(bodyDef);
		
		LwjglFiles files = new LwjglFiles();
		TiledMap map = TiledLoader.createMap(files.internal("data/BattleShipsCollision.tmx"));
		TiledObjectGroup objectGroup = map.objectGroups.get(0);
		for (TiledObject current : objectGroup.objects) {
			
			PolygonShape staticRectangle = new PolygonShape();
			staticRectangle.setAsBox(current.width/2*WORLD_TO_BOX, current.height/2*WORLD_TO_BOX,
					new Vector2((current.x-1024 +current.width/2)*WORLD_TO_BOX,
							(-current.y+1024 -current.height/2)*WORLD_TO_BOX), 0);
	        FixtureDef fixtureDef = new FixtureDef();  
	        fixtureDef.shape = staticRectangle;  
	        fixtureDef.density = 1.0f;  
	        fixtureDef.friction = 0.0f;  
	        fixtureDef.restitution = 0.0f;
	        WorldCollisionBody.createFixture(fixtureDef);  
	    }
		
		objectGroup = map.objectGroups.get(1);
		for (TiledObject current : objectGroup.objects) {
			if(current.y>1024) {
				new Tower("blue",current.x-1024+16,-current.y+1024, this);
			}
			else {
				new Tower("red",current.x-1024+16,-current.y+1024, this);
			}
	    }
		short slot;
		for(int i = 0; i < game.getPlayerList().size(); i++) {
			slot = game.getPlayerList().getServerPlayer(i).getSlotNumber();
			if( slot < 3) {
				Unit player = new PlayerShip("red", 0f, 768f, this, slot);
				playerShipMap.put(player.hashCode(), player);
			} else {
				Unit player = new PlayerShip("blue", 0f, -768f, this, slot);
				playerShipMap.put(player.hashCode(), player);
			}
		}
		
		for(int i = 0; i < game.getPlayerList().size(); i++) {
			game.getPlayerList().getServerPlayer(i).getConnection().sendUDP(unitMap);
		}
	}
	
	public void run(){
		System.out.println("game " + game.getName() + " started");
		while(running){
			if(playersAreConnected()) {
				try {
					physicsWorld.step(BOX_STEP, BOX_VELOCITY_ITERATIONS, BOX_POSITION_ITERATIONS); 
					
					// TODO Fixed step accumulator needed1
					
					GLUH.onUpdate(0.1f);
					for (Iterator<Body> iter = physicsWorld.getBodies(); iter.hasNext();) {
						Unit aux = (Unit) iter.next().getUserData();
						if(aux != null) {
							aux.onUpdate(0.1f);
							aux.updateUnitData();
						}
					}
					
					for(int i = 0 ; i < game.getPlayerList().size(); i++) {
						int slot = game.getPlayerList().getServerPlayer(i).getSlotNumber(); 
						if(!stackArray.get(slot).isEmpty()) {
							UnitData message = stackArray.get(slot).pop();
							playerShipMap.get(message.unitKey).CollisionBody.setTransform(message.position, 0);
							stackArray.get(slot).clear();
						}
					}
//					if(!msgStack.isEmpty()) {
//			            UnitMap message = msgStack.pop(); 
//			            for(Entry<Integer, UnitData> entry : message.map.entrySet()) {
//			            	playerShipMap.get(entry.getKey()).CollisionBody.setTransform(entry.getValue().position, 0);
////			            	System.out.println(this.unitMap.map.get(entry.getKey()).slot + ": " + playerShipMap.get(entry.getKey()).CollisionBody.getPosition().x + " " + playerShipMap.get(entry.getKey()).CollisionBody.getPosition().y);
////			            	System.out.println(this.unitMap.map.get(entry.getKey()).slot + ": " + this.unitMap.map.get(entry.getKey()).position.x + " " + this.unitMap.map.get(entry.getKey()).position.y);
//			            }
//			            msgStack.clear();
//					}
					
//					ByteArrayOutputStream bOut = new ByteArrayOutputStream();  
//					ObjectOutputStream oOut = new ObjectOutputStream(bOut);  
//					oOut.writeObject(unitMap);  
//					oOut.close();  
//					System.out.println("The size of the object is: "+bOut.toByteArray().length);  
					
//					for(Entry<Integer, UnitData> entry : unitMap.map.entrySet()) {
//						if(entry.getValue().type.equals(UnitData.Type.SHIP)) {
//							System.out.println(entry.getValue().slot + ": " + entry.getValue().position.x + " " + entry.getValue().position.y);
//						}
//					}
					for(int i = 0; i < game.getPlayerList().size(); i++) {
						game.getPlayerList().getServerPlayer(i).getConnection().sendUDP(unitMap);
					}
					for (Iterator<Body> iter = physicsWorld.getBodies(); iter.hasNext();) {
						Unit aux = (Unit) iter.next().getUserData();
						if(aux != null && aux.Health<=0)
						{
								aux.Destroy();
						}
					}
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
	
	public void movePlayer(UnitMap unitMap) {
		for(Entry<Integer, UnitData> entry : unitMap.map.entrySet()) {
			for (Iterator<Body> iter = physicsWorld.getBodies(); iter.hasNext();) {
				Unit aux = (Unit) iter.next().getUserData();
				if(aux != null && aux.hashCode() == entry.getKey()) {
					aux.CollisionBody.setTransform(entry.getValue().position, 0);
				}
			}
			//System.out.println(this.unitMap.map.get(entry.getKey()).position.x + " " +this.unitMap.map.get(entry.getKey()).position.y);
		}
	}
	
	public Stack<UnitData> getMsgStack(int slot) {
		return this.stackArray.get(slot);
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
