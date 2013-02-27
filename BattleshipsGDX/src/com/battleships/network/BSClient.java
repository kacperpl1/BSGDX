package com.battleships.network;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
   	private PlayerList gameLobbyPlayerList = new PlayerList();
   	private BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>();
	
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
									msgQueue.put(list);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								break;
							}
							// Translate message about game list in main lobby
							// and send it to parent activity
							case 1 : {
								gameList.translateServerString(reply.text);
								
								String list = "";
								for(int i = 0; i < gameList.size(); i++){
									list += gameList.getGame(i).getName() + " ";
								}
								
//								Message messageToParent = new Message();
//								Bundle messageData = new Bundle();
//								messageToParent.what = 1;
//								messageData.putString("gamelist", list);
//								messageToParent.setData(messageData);
//								
//								mainLobbyHandler.sendMessage(messageToParent);
								break;
							}
							// Translate message about player taking slot
							// in game and send it to parent activity
							case 2 : {
								gameLobbyPlayerList.translateServerString(reply.text);
								String list = "";
//								for(int i = 0; i< gameLobbyPlayerList.size(); i++){
//									list += gameLobbyPlayerList.getPlayer(i).getName() + " ";
//								}
								int i = 0;
								while(part.hasMoreTokens()){
									list += part.nextToken() + " ";
								}
								
//								Message messageToParent = new Message();
//								Bundle messageData = new Bundle();
//								messageToParent.what = 2;
//								messageData.putString("gameLobbyPlayerList", list);
//								messageToParent.setData(messageData);
//								
//								gameLobbyHandler.sendMessage(messageToParent);
								break;
							}
							default : break;
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
		
		// Start thread that listens for messages from
		// other activities
		Thread listen = new Thread(){
			public void run(){
//				Looper.prepare();
//				clientHandler = new Handler() {
//					public void handleMessage(Message msg) {
//						switch(msg.what){
//							case 1 : createGame(msg.obj.toString()); break;
//							case 2 : takeSlot(msg.obj.toString()); break;
//							case 3 : joinGame(msg.obj.toString()); break;
//							case 4 : leaveGame(msg.obj.toString()); break;
//							case 5 : readyUp(msg.obj.toString()); break;
//							default : break;
//						}
//						
//					}
//				};
//				Looper.loop();
			}
		};
		listen.start();
		
		// Main loop
		while(gameFlag)	{
			keepAlive();
			playerList.checkKick();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}

	// Send keep alive packet to server
	public void keepAlive(){
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
	public void createGame(String gameName){
		try{
			ResponseMessage req = new ResponseMessage();
			req.text = "1 " + gameName + " " + this.clientPlayer.getName();
			client.sendUDP(req);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Failed to create a game");
		}
		playerList.getByName(this.clientPlayer.getName()).joinGame(gameName);
		playerList.getByName(this.clientPlayer.getName()).setHost(true);
	}
	// Send join game packet to server
	public void joinGame(String gameName){
		try{
			ResponseMessage req = new ResponseMessage();
			req.text = "3 " + gameName + " " + this.clientPlayer.getName();
			client.sendUDP(req);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Failed to join game");
		}
		playerList.getByName(this.clientPlayer.getName()).joinGame(gameName);
	}
	// Send take slot packet to server
	public void takeSlot(String message){
		StringTokenizer part = new StringTokenizer(message);
		String slotNumber = part.nextToken();
		String gameName = part.nextToken();
		try{
			ResponseMessage req = new ResponseMessage();
			req.text = "2 " + gameName + " " + this.clientPlayer.getName() + " " + slotNumber;
			client.sendUDP(req);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Failed to take slot");
		}
	}
	// Send leave game packet to server
	public void leaveGame(String gameName){
		try{
			ResponseMessage req = new ResponseMessage();
			req.text = "4 " + gameName + " " + this.clientPlayer.getName();
			client.sendUDP(req);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Failed to leave game");
		}
		playerList.getByName(this.clientPlayer.getName()).leaveGame();
		playerList.getByName(this.clientPlayer.getName()).setHost(false);
	}
	// check if all players are ready
	// if true, start game
	public void readyUp(String gameName){
		
//		if(gameList.getByName(gameName).allReady()){
//			Message messageToParent = new Message();
//			Bundle messageData = new Bundle();
//			messageToParent.what = 5;
//			messageData.putString("start", "start");
//			messageToParent.setData(messageData);
//			
//			gameLobbyHandler.sendMessage(messageToParent);
//			gameList.getByName(gameName).start();
//		}
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
	public void stopGame(){
		this.gameFlag = false;
		gameList.reset();
		playerList.reset();
		client.close();
	}
	public Player getPlayer(){
		return this.clientPlayer;
	}
	public BlockingQueue<String> getQueue(){
		return this.msgQueue;
	}
}