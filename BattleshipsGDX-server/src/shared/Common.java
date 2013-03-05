package shared;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.esotericsoftware.kryo.Kryo;


/**
 * The resources shared by both the client and server
 * 
 * @author ASH
 */
public class Common {
    
    public static final String DEFAULT_IP = getIp(); //192.168.198.185
    
    public static final int DEFAULT_PORT_TCP=6456;
    public static final int DEFAULT_PORT_UDP=6466;
    
    private static String getIp() {
    	InetAddress ip;
  	  	try {
	  		ip = InetAddress.getLocalHost();
	  		return ip.getHostAddress();
  	  	} catch (UnknownHostException e) {
  	  		e.printStackTrace();
  	  	}
  	  	return "";
    }
    
    /**
     * Registers all messages used in the game with JGN which optimizes them
     */
    public static void registerMessages(Kryo kryo)
    {        
    	kryo.register(ResponseMessage.class);
    }
}
