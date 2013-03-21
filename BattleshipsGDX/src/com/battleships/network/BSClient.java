package com.battleships.network;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.battleships.base.UnitData;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

// Client singleton
public class BSClient implements Runnable	{
	
	private static final BSClient instance = new BSClient();

	private Thread th;
	private volatile boolean gameFlag = true;
   	private PlayerList playerList = new PlayerList();
   	private GameList gameList = new GameList();
   	private BlockingQueue<String> mainLobbyMsgQueue = new LinkedBlockingQueue<String>();
   	private BlockingQueue<String> gameLobbyMsgQueue = new LinkedBlockingQueue<String>();
   	private BlockingQueue<Map<Short, UnitData>> mainGameMsgQueue = new LinkedBlockingQueue<Map<Short, UnitData>>();
   	
	private Client client;
	private Player clientPlayer;
	
	private BSClient(){}
	
	public static BSClient getInstance(){
		return instance;
	}
	
	public void init (String playerName){
		this.clientPlayer = new Player(playerName);
	}
	
	private void init()	{
		try	{
			// Connect to server
			client = new Client();
			Common.registerMessages(client.getKryo());						
			client.start();
			client.connect(10000, Common.DEFAULT_IP, Common.DEFAULT_PORT_TCP, Common.DEFAULT_PORT_UDP);
			
			// Listen for messages from server
			client.addListener(new Listener() {
				public void received (Connection connection, Object object) {
					if (object instanceof ResponseMessage) {
						ResponseMessage reply = (ResponseMessage)object;
						StringTokenizer part = new StringTokenizer(reply.text);
						
						switch(Integer.valueOf(part.nextToken())){
							// Translate message about player list in main lobby
							// and send it to lobby screen
							case 0 : {
								playerList.translateServerString(reply.text);
								playerList.checkKick();
								
								String list = "0 ";
								for(int i = 0; i< playerList.size(); i++){
									list += playerList.getPlayer(i).getName() + " ";
								}
								try {
									mainLobbyMsgQueue.put(list);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								break;
							}
							// Translate message about game list in main lobby
							// and send it to parent activity
							case 1 : {
								gameList.translateServerString(reply.text);
								
								String list = "1 ";
								for(int i = 0; i < gameList.size(); i++){
									list += gameList.getGame(i).getName() + " " + gameList.getGame(i).getId() + " ";
								}
								
								try {
									mainLobbyMsgQueue.put(list);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								break;
							}
							// Translate message about player taking slot
							// in game and send it to parent activity
							case 2 : {
								String list = "0 ";
								while(part.hasMoreTokens()){
									
									list += part.nextToken() + " " + part.nextToken() + " ";
								}
								try {
									gameLobbyMsgQueue.put(list);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}

								break;
							}
							// Translate message about players joining or 
							// leaving game lobby
							case 3 : {
								gameList.getGameById(clientPlayer.getGameId()).translateServerString(reply.text);
								
								break;
							}
							// Translate message about player getting
							// ready/unready
							case 5 : {
								String uId = part.nextToken();
								boolean ready = Boolean.valueOf(part.nextToken());
								gameList.getGameById(clientPlayer.getGameId()).getPlayer(uId).setReady(ready);
								if(gameList.getGameById(clientPlayer.getGameId()).allReady()){
									try {
										gameLobbyMsgQueue.put("1");
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									gameList.getGameById(clientPlayer.getGameId()).start();
								}
							}
							default : break;
						}
					} else {
						if(object instanceof HashMap) {
							Map<Short, UnitData> map = (HashMap<Short, UnitData>)object;
							try {
								mainGameMsgQueue.put(map);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}
			});
			
			System.out.println("Socket Created");
			
		} catch (IOException e) {
			e.printStackTrace();
			gameFlag = false;
		}
	}
	public void run() {
		init();
		
		// Main loop
		while(gameFlag)	{
			keepAlive();
			playerList.checkKick();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}		
	}

	// Send keep alive packet to server
	public void keepAlive() {
		try{
			ResponseMessage req = new ResponseMessage();
			req.text = "0 " + this.clientPlayer.getId() + " " + this.clientPlayer.getName() + " ";
			client.sendUDP(req);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to send keep alive packet.");
		}
	}
	// Send create game packet to server
	public void createGame(String gameName) {
		Game game = new Game(gameName);
		game.addPlayer(clientPlayer);
		gameList.addGame(game);
		this.clientPlayer.joinGame(game.getId());
		try{
			ResponseMessage req = new ResponseMessage();
			req.text = "1 " + game.getId() + " " + game.getName() + " " + this.clientPlayer.getId();
			client.sendUDP(req);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Failed to create a game");
		}
		this.clientPlayer.setHost(true);
	}
	// Send join game packet to server
	public void joinGame(String gameId) {
		this.clientPlayer.joinGame(gameId);
		gameList.getGameById(gameId).addPlayer(clientPlayer);
		try{
			ResponseMessage req = new ResponseMessage();
			req.text = "3 " + gameId + " " + this.clientPlayer.getId();
			client.sendUDP(req);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Failed to join game");
		}
	}
	// Send take slot packet to server
	public void takeSlot(int slotNumber) {
		try{
			ResponseMessage req = new ResponseMessage();
			req.text = "2 " + this.clientPlayer.getGameId() + " " + this.clientPlayer.getId() + " " + slotNumber;
			clientPlayer.takeSlot(slotNumber);
			client.sendUDP(req);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Failed to take slot");
		}
	}
	// Send leave game packet to server
	public void leaveGame() {
		try{
			ResponseMessage req = new ResponseMessage();
			req.text = "4 " + this.clientPlayer.getGameId() + " " + this.clientPlayer.getId();
			client.sendUDP(req);
			gameList.getGameById(this.clientPlayer.getGameId()).getPlayerList().clear();
			this.clientPlayer.leaveGame();
			this.clientPlayer.setHost(false);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Failed to leave game");
		}
	}
	// Send ready / unready packet to server
	public void readyUp(String gameId) {
		clientPlayer.setReady(!clientPlayer.isReady());
		try{
			ResponseMessage req = new ResponseMessage();
			req.text = "5 " + this.clientPlayer.getGameId() + " " + this.clientPlayer.getId() + " " + this.clientPlayer.isReady();
			client.sendUDP(req);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Failed to ready/unready");
		}
	}
	// Send start game packet to server
	public void startGame() {
		try{
			ResponseMessage req = new ResponseMessage();
			req.text = "6 " + this.clientPlayer.getGameId();
			client.sendUDP(req);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Failed to start game");
		}
	}
	// Send position update to gameThread
	public void move(UnitData unitData) {
		client.sendUDP(unitData);
	}
	// Send player directory to gameThread
	public void sendDirection(UnitData unitData) {
		client.sendUDP(unitData);
	}
	public Player getPlayer() {
		return this.clientPlayer;
	}
	public Game getGame() {
		return gameList.getGameById(clientPlayer.getGameId());
	}
	public BlockingQueue<String> getMainLobbyQueue() {
		return this.mainLobbyMsgQueue;
	}
	public BlockingQueue<String> getGameLobbyQueue() {
		return this.gameLobbyMsgQueue;
	}
	public BlockingQueue<Map<Short, UnitData>> getMainGameQueue() {
		return this.mainGameMsgQueue;
	}
	public void stopGame() {
		this.gameFlag = false;
		gameList.reset();
		playerList.reset();
		clientPlayer.isOffline();
		client.close();
	}
	public void start()	{
		gameFlag=true;
		if(th==null) {
			th = new Thread(this);
			th.start();
		}
	}
	public void stop() {
		if(th!=null) {
			th = null;
		}
		gameFlag=false;
	}
	public void destroy() {
		if(th!=null) {
			th = null;
		}
		gameFlag=false;
	}
}