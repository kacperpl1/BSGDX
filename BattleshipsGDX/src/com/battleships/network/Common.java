package com.battleships.network;

import com.battleships.base.BSPreferences;
import com.esotericsoftware.kryo.Kryo;


/**
 * The resources shared by both the client and server
 * 
 * @author ASH
 */
public class Common {
    
	private static BSPreferences prefs = new BSPreferences();
    public static final String DEFAULT_IP = prefs.getServerIp(); //192.168.198.185
    
    public static final int DEFAULT_PORT_TCP=6456;
    public static final int DEFAULT_PORT_UDP=6466;
    
   
    /**
     * Registers all messages used in the game with JGN which optimizes them
     */
    public static void registerMessages(Kryo kryo)
    {        
    	kryo.register(ResponseMessage.class);
    	kryo.register(UnitMap.class);
    	kryo.register(java.util.HashMap.class);
    	kryo.register(UnitData.class);
    	kryo.register(com.badlogic.gdx.math.Vector2.class);
    	kryo.register(UnitData.Type.class);
    }
}