package com.battleships.base;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.battleships.network.BSClient;

public class MainLobbyScreen implements Screen {
	private Stage stage;
	private SpriteBatch batch;
	private int w = Gdx.graphics.getWidth();
	private int h = Gdx.graphics.getHeight();
	private Table leftTable = new Table(Resources.skin);
	private Table playerListTable = new Table(Resources.skin);
	
	private Label playerListLabel;
	private ArrayList<String> playerList = new ArrayList<String>();
	private ArrayList<String> gameList = new ArrayList<String>();
	BlockingQueue<String> msgQueue;
	
	private BSClient lobbyClient;
	
	public MainLobbyScreen(){
		lobbyClient = BSClient.getInstance();
	    lobbyClient.init("Battleship");
		try{
			lobbyClient.start();
		}catch(Exception e){
			e.printStackTrace();
		}
		msgQueue = lobbyClient.getQueue();
		playerListLabel = new Label("Player List:", Resources.skin);
		
		new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        String message = msgQueue.take();
                        handleMessage(message);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "Messanger").start();
	}
	
	public void handleMessage(String message){
		StringTokenizer part = new StringTokenizer(message);
		switch(Integer.valueOf(part.nextToken())) {
			case 0 : {
				playerListTable.clear();
				playerListTable.row();
				playerListTable.row();
				while(part.hasMoreTokens()){
					playerListTable.add(new Label(part.nextToken(), Resources.skin)).spaceTop(10);
					playerListTable.row();
					leftTable.validate();
				}
				break;
			}
			default : break;
		}
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor( 0f, 0f, 0f, 1f );
        Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT );
        
        batch.begin();
        batch.draw( Resources.splashTexture, 0, 0, w, h );
        batch.end();
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		
		// creates the table actor
        //Table table = new Table(Resources.skin);
        // 100% width and 100% height on the table (fills the stage)
		leftTable.setFillParent(true);
		leftTable.align(Align.top);
		//playerListTable.setFillParent(true);
        // add the table to the stage
		stage.addActor(leftTable);
		leftTable.add(playerListLabel);
		playerListTable.setFillParent(true);
		leftTable.addActor(playerListTable);
        //stage.addActor(playerListTable);
        //playerListTable.add(playerListLabel);
	}

	@Override
	public void show() {
		batch = new SpriteBatch();
		stage = new Stage();
        Gdx.input.setInputProcessor( stage );
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		lobbyClient.stopGame();
		lobbyClient.destroy();
	}

}
