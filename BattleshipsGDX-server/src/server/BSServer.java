package server;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.StringTokenizer;

import shared.Common;
import shared.ResponseMessage;
import shared.UnitData;
import shared.UnitMap;

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
					} else {
						if(object instanceof UnitMap) {
							Translator translator = new Translator(gameThreadList, (UnitMap)object);
							
//							for(Entry<Integer, UnitData> entry : map.map.entrySet()) {
//								System.out.println(entry.getValue().gameID);
//								System.out.println(entry.getValue().position.x + " " + entry.getValue().position.y);
//							}
						}
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
	private ResponseMessage reply = new ResponseMessage();
	
	public Translator(ServerPlayerList pL, ServerGameList gL, LinkedList<GameThread> gTL, String m, Connection c) {
		playerList = pL;
		gameList = gL;
		gameThreadList = gTL;
		message = m;
		connection = c;
	}
	
	public Translator(LinkedList<GameThread> gTL, UnitMap unitMap) {
		gameThreadList = gTL;
		String gId = "";
		for(Entry<Integer, UnitData> entry : unitMap.map.entrySet()) {
			gId = entry.getValue().gameID;
			//System.out.println(entry.getValue().position.x + " " + entry.getValue().position.y);
		}
		for(GameThread gThread : gameThreadList) {
			if(gThread.getName().equals(gId)) {
				Stack<UnitMap> msgStack = gThread.getMsgStack();
				msgStack.push(unitMap);
				break;
			}
		}
	}
	
	public synchronized void run() {
		StringTokenizer part = new StringTokenizer(message);
		
		switch(part.nextToken()) {
			// First keep alive packet from client:
			// add client to playerList and send new 
			// playerLists and gameList to all alive clients
			case "0" : {
				String uId = part.nextToken();
				String uName = part.nextToken();
				if(!playerList.exists(uId)){
					ServerPlayer player = new ServerPlayer(uId, uName, connection);
					playerList.add(player);
					player.resetKeys();
					
					this.sendPlayerList();
					this.sendGameList();
					
				} else {
					playerList.getById(uId).resetKeys();
					// Send new playerList only if someone has disconnected
					boolean someoneDisconnected = this.checkForDisconnections();
					if(someoneDisconnected){
						this.sendPlayerList();
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
				String uId = part.nextToken();
				
				ServerGame sGame;
				sGame = gameList.createGame(gId, gName);
				sGame.addPlayer(playerList.getById(uId));
				playerList.getById(uId).joinGame();
				System.out.println("Created game " + gId + " by user " + uId + "!");
				
				this.sendPlayerList();
				this.sendGameList();
				this.sendSlotList(sGame);
				
				break;
			}
			// Take slot and send list of players who
			// took slots in game
			case "2" : {
				String gId = part.nextToken();
				String uId = part.nextToken();
				String slotNumber = part.nextToken();
				System.out.println("Player " + uId + " took slot " + slotNumber + " in game " + gId);
				
				ServerGame sGame = gameList.getById(gId);
				sGame.getPlayerList().getById(uId).setSlotNumber(Integer.valueOf(slotNumber));
				
				this.sendSlotList(sGame);
				
				break;
			}
			// Join game and send list of players in this game
			case "3" : {
				String gId = part.nextToken();
				String uId = part.nextToken();
				
				ServerGame sGame = gameList.getById(gId);
				sGame.addPlayer(playerList.getById(uId));
				playerList.getById(uId).joinGame();
				System.out.println("Player " + uId + " joined game " + gId + "!");
				
				this.sendPlayerList();
				this.sendSlotList(sGame);
				this.sendInGamePlayerList(sGame);
				
				break;
			}
			// Leave game and send list of players in this game
			case "4" : {
				String gId = part.nextToken();
				String uId = part.nextToken();
				
				ServerGame sGame = gameList.getById(gId);
				sGame.removePlayer(playerList.getById(uId));
				playerList.getById(uId).setSlotNumber(-1);
				playerList.getById(uId).leaveGame();
				System.out.println("Player " + uId + " left game" + gId + "!");
				
				this.sendPlayerList();
				
				if(sGame.getPlayerList().size() > 0){
					this.sendSlotList(sGame);
					this.sendInGamePlayerList(sGame);
				} else {
					gameList.remove(gId);
					this.sendGameList();
				}
				
				break;
			}
			// Get ready/unready and send information to clients in game
			case "5" : {
				String gId = part.nextToken();
				String uId = part.nextToken();
				String ready = part.nextToken();
				
				ServerGame sGame = gameList.getById(gId);
				
				this.sendReadyList(sGame, uId, ready);
				
				break;
			}
			// Start game
			case "6" : {
				String gId = part.nextToken();
				
				runGameThread(gId);
				gameList.remove(gId);
				
				this.sendGameList();
				
				break;
			}
			default : System.out.println("Unhandled message from client!"); break;
		}
	}
	
	public void sendPlayerList() {
		reply.text = "0 " + playerList.mainLobbyToString();
		for(int i = 0; i < playerList.size(); i++){
			playerList.getServerPlayer(i).getConnection().sendUDP(reply);
		}
	}
	
	public void sendGameList() {
		reply.text = "1 " + gameList.toString();
		for(int i = 0; i < playerList.size(); i++){
			playerList.getServerPlayer(i).getConnection().sendUDP(reply);
		}
	}
	
	public void sendSlotList(ServerGame sGame) {
		reply.text = "2 " + sGame.getPlayerList().slotListToString();
		for(int i = 0; i < sGame.getPlayerList().size(); i++){
			sGame.getPlayerList().getServerPlayer(i).getConnection().sendUDP(reply);
		}
	}
	
	public void sendInGamePlayerList(ServerGame sGame) {
		reply.text = "3 " + sGame.getPlayerList().gameLobbyToString();
		for(int i = 0; i < sGame.getPlayerList().size(); i++){
			sGame.getPlayerList().getServerPlayer(i).getConnection().sendUDP(reply);
		}
	}
	
	public void sendReadyList(ServerGame sGame, String uId, String ready) {
		reply.text = "5 " + uId + " " + ready;
		for(int i = 0; i < sGame.getPlayerList().size(); i++){
			sGame.getPlayerList().getServerPlayer(i).getConnection().sendUDP(reply);
		}
	}
	
	public boolean checkForDisconnections() {
		for(int i = 0; i < playerList.size(); i++){
			if(!playerList.getServerPlayer(i).getConnection().isConnected()){
				playerList.remove(i);
				return true;
			}
		}
		return false;
	}
	
	public boolean runGameThread(String gId) {
		synchronized(gameThreadList) {
			for(GameThread thread : gameThreadList) {
				if(thread.getName().equals(gId)) {
					return true;
				}
			}
			ServerGame sGame = gameList.getById(gId);
			GameThread gameThread = new GameThread(sGame);
			this.gameThreadList.add(gameThread);
			gameThread.start();
			return false;
		}
	}
	
}
