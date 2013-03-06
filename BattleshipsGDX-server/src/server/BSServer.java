package server;

import java.io.IOException;
import java.util.LinkedList;
import java.util.StringTokenizer;

import shared.Common;
import shared.ResponseMessage;

import com.badlogic.gdx.utils.GdxNativesLoader;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

// GUI class for server
public class BSServer {	
		//the server class
	@SuppressWarnings("unused")
	private static ServerThread server;
	
    public static void main(String[] args) {
    	GdxNativesLoader.load();
    	server = new ServerThread();
    }

}

// Server thread that is responsible for communication
// with all clients
class ServerThread {	
	private  ServerPlayerList playerList = new ServerPlayerList();
	private ServerGameList gameList = new ServerGameList();
	private LinkedList<GameThread> gameThreadList = new LinkedList<GameThread>();
	
	public ServerThread()
	{		
		System.out.println("ServerThread start");
		try
		{			
			final Server server = new Server();
			Common.registerMessages(server.getKryo());
			server.start();
			server.bind(Common.DEFAULT_PORT_TCP, Common.DEFAULT_PORT_UDP);
			
			// Listen for messages from clients
			server.addListener(new Listener() 
			{
				public void received (Connection connection, Object object) {
					if (object instanceof ResponseMessage) {	
						ResponseMessage request = (ResponseMessage)object;
						
						Translator translator = new Translator(playerList, gameList, gameThreadList, request.text, connection);
						translator.start();
					}
				}
			});			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}	
}

// Thread that translates messages from clients
class Translator extends Thread {
	protected ServerPlayerList playerList;
	protected ServerGameList gameList;
	protected LinkedList<GameThread> gameThreadList;
	protected String message;
	protected Connection connection;
	private ResponseMessage reply;
	
	public Translator(ServerPlayerList pL, ServerGameList gL, LinkedList<GameThread> gTL, String m, Connection c) {
		playerList = pL;
		gameList = gL;
		gameThreadList = gTL;
		message = m;
		connection = c;
	}
	public synchronized void run() {
		StringTokenizer part = new StringTokenizer(message);
		
		switch(part.nextToken()) {
			// First keep alive packet from client:
			// add client to playerList and send new 
			// playerLists and gameList to all alive clients
			case "0" : {
				String id = part.nextToken();
				String name = part.nextToken();
				if(!playerList.exists(id)){
					ServerPlayer player = new ServerPlayer(id, name, connection);
					playerList.add(player);
					player.resetKeys();
					
					reply = new ResponseMessage();
					
					reply.text = "0 " + playerList.toString();
					for(int i = 0; i < playerList.size(); i++){
						playerList.getServerPlayer(i).getConnection().sendUDP(reply);
					}
					
					reply.text = "1 " + gameList.toString();
					for(int i = 0; i < playerList.size(); i++){
						playerList.getServerPlayer(i).getConnection().sendUDP(reply);
					}
				} else {
					playerList.getById(id).resetKeys();
					boolean flag = false;
					// Send new playerList only if someone has disconnected
					for(int i = 0; i < playerList.size(); i++){
						if(!playerList.getServerPlayer(i).getConnection().isConnected()){
							synchronized(playerList){
								playerList.remove(i);
							}
							flag = true;
						}
					}
					if(flag){
						reply = new ResponseMessage();
						reply.text = "0 " + playerList.toString();
						for(int i = 0; i < playerList.size(); i++){
							playerList.getServerPlayer(i).getConnection().sendUDP(reply);
						}
					}
				}
				break;
			}
			// Request to create game:
			// create game on server and send updated
			// playerList and gameList to all alive clients
			case "1" : {
				String gId = part.nextToken();
				String gName = part.nextToken();
				String id = part.nextToken();
				reply = new ResponseMessage();
				ServerGame sGame;
				synchronized(gameList) {
					sGame = gameList.createGame(gId, gName);
					sGame.addPlayer(playerList.getById(id));
				}
				System.out.println("Created game " + gId + " by user " + id + "!");
				
				reply.text = "2 " + sGame.getPlayerList().toString(1);
				for(int i = 0; i < sGame.getPlayerList().size(); i++){
					sGame.getPlayerList().getServerPlayer(i).getConnection().sendUDP(reply);
				}
				
//				GameThread game = new GameThread(sGame);
//				
//				gameThreadList.add(game);
//				game.start();
				
				reply.text = "0 " + playerList.toString();
				for(int i = 0; i < playerList.size(); i++){
					playerList.getServerPlayer(i).getConnection().sendUDP(reply);
				}
				
				reply.text = "1 " + gameList.toString();
				for(int i = 0; i < playerList.size(); i++){
					playerList.getServerPlayer(i).getConnection().sendUDP(reply);
				}
				break;
			}
			// Take slot and send list of players who
			// took slots in game
			case "2" : {
				String gId = part.nextToken();
				String uId = part.nextToken();
				String slotNumber = part.nextToken();
				reply = new ResponseMessage();
				System.out.println("Player " + uId + " took slot " + slotNumber + " in game " + gId);
				
				ServerGame sGame = gameList.getById(gId);
				
				sGame.getPlayerList().getById(uId).setSlotNumber(Integer.valueOf(slotNumber));
				
				reply.text = "2 " + sGame.getPlayerList().toString(1);
				for(int i = 0; i < sGame.getPlayerList().size(); i++){
					sGame.getPlayerList().getServerPlayer(i).getConnection().sendUDP(reply);
				}
				
				break;
			}
			// Join game and send list of players in this game
			case "3" : {
				String gId = part.nextToken();
				String uId = part.nextToken();
				ServerGame sGame;
				synchronized(gameList) {
					sGame = gameList.getById(gId);
					sGame.addPlayer(playerList.getById(uId));
				}
				
				System.out.println("Player " + uId + " joined game " + gId + "!");
				
				reply = new ResponseMessage();
				reply.text = "0 " + playerList.toString();
				for(int i = 0; i < playerList.size(); i++){
					playerList.getServerPlayer(i).getConnection().sendUDP(reply);
				}
				
				reply.text = "2 " + sGame.getPlayerList().toString(1);
				for(int i = 0; i < sGame.getPlayerList().size(); i++){
					sGame.getPlayerList().getServerPlayer(i).getConnection().sendUDP(reply);
				}
				
				reply.text = "3 " + sGame.getPlayerList().toString();
				for(int i = 0; i < sGame.getPlayerList().size(); i++){
					sGame.getPlayerList().getServerPlayer(i).getConnection().sendUDP(reply);
				}
				
				break;
			}
			// Leave game and send list of players in this game
			case "4" : {
				String gId = part.nextToken();
				String uId = part.nextToken();
				reply = new ResponseMessage();
				
				ServerGame sGame;
				synchronized(gameList) {
					sGame = gameList.getById(gId);
					sGame.removePlayer(playerList.getById(uId));
				}
				System.out.println("Player " + uId + " left game" + gId + "!");
				
				playerList.getById(uId).setSlotNumber(-1);
				
				reply.text = "0 " + playerList.toString();
				for(int i = 0; i < playerList.size(); i++){
					playerList.getServerPlayer(i).getConnection().sendUDP(reply);
				}
				
				System.out.println("playerlist size " + sGame.getPlayerList().size());
				if(sGame.getPlayerList().size() > 0){
					reply.text = "2 " + sGame.getPlayerList().toString(1);
					for(int i = 0; i < sGame.getPlayerList().size(); i++){
						sGame.getPlayerList().getServerPlayer(i).getConnection().sendUDP(reply);
					}
					
					reply.text = "3 " + sGame.getPlayerList().toString();
					for(int i = 0; i < sGame.getPlayerList().size(); i++){
						System.out.println("send to player " + sGame.getPlayerList().getServerPlayer(i).getId());
						sGame.getPlayerList().getServerPlayer(i).getConnection().sendUDP(reply);
					}
					
				} //else {
//					for(int i = 0; i < gameThreadList.size(); i++){
//						if(gameThreadList.get(i).getName().equals(gName)){
//							gameThreadList.get(i).stopGame();
//							gameThreadList.get(i).interrupt();
//							gameThreadList.remove(i);
//							
//							break;
//						}
//					}
//					gameList.remove(gName);
//					replyStr = "1 " + gameList.toString();
//					reply.text = replyStr;
//					for(int i = 0; i < playerList.size(); i++){
//						playerList.getServerPlayer(i).getConnection().sendUDP(reply);
//					}
//				}
				
				break;
			}
			// Get ready/unready and send information to clients in game
			case "5" : {
				String gId = part.nextToken();
				String uId = part.nextToken();
				String ready = part.nextToken();
				reply = new ResponseMessage();
				reply.text = "5 " + uId + " " + ready;
				ServerGame sGame = gameList.getById(gId);
				for(int i = 0; i < sGame.getPlayerList().size(); i++){
					sGame.getPlayerList().getServerPlayer(i).getConnection().sendUDP(reply);
				}
				
				break;
			}
			case "6" : {
				String gId = part.nextToken();
				ServerGame sGame = gameList.getById(gId);
				GameThread gameThread = new GameThread(sGame);
				this.gameThreadList.add(gameThread);
				gameThread.start();
				break;
			}
			default : System.out.println("Unhandled message from client!"); break;
		}
	}

}
