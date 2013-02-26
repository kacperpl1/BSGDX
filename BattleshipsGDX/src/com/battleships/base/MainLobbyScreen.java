package com.battleships.base;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.battleships.network.BSClient;

public class MainLobbyScreen implements Screen {
	private Stage stage;
	private SpriteBatch batch;
	private BitmapFont font;
	private int w = Gdx.graphics.getWidth();
	private int h = Gdx.graphics.getHeight();
	
	private Label playerListLabel;
	
	private BSClient lobbyClient;
	
	public MainLobbyScreen(){
		lobbyClient = BSClient.getInstance();
	    lobbyClient.init("Battleship");
		try{
			lobbyClient.start();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		playerListLabel = new Label("Player List:", Resources.skin);
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
        Table table = new Table(Resources.skin);
        // 100% width and 100% height on the table (fills the stage)
        table.setFillParent(true);
        // add the table to the stage
        stage.addActor(table);
        table.add(playerListLabel);
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
