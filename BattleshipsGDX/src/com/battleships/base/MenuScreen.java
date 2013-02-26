package com.battleships.base;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.battleships.network.BSClient;

public class MenuScreen implements Screen {
	private Stage stage;
	private SpriteBatch batch;
	private int w = Gdx.graphics.getWidth();
	private int h = Gdx.graphics.getHeight();
	
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
        // register the button "start game"
        TextButton startGameButton = new TextButton( "Start game", Resources.skin );
        startGameButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event,float x,float y )
            {
                BaseGame.instance.setScreen( new GameScreen() );
            }
        } );
        // register the button "options"
        TextButton optionsButton = new TextButton( "Options", Resources.skin );
        optionsButton.addListener( new ClickListener() {
            @Override
            public void clicked(InputEvent event,float x,float y )
            {
            	//BaseGame.instance.setScreen( game.getOptionsScreen() );
            }
        } );
        // register the button "hall of fame"
        TextButton aboutButton = new TextButton( "About", Resources.skin );
        aboutButton.addListener( new ClickListener() {
            @Override
            public void clicked(InputEvent event,float x,float y )
            {
            	BaseGame.instance.setScreen( new MainLobbyScreen() );
            }
        } );

        // creates the table actor
        Table table = new Table(Resources.skin);
        // 100% width and 100% height on the table (fills the stage)
        table.setFillParent(true);
        // add the table to the stage
        stage.addActor(table);
        // add the welcome message with a margin-bottom of 50 units
        table.add( "BattleShips" ).spaceBottom( 50 );
        // move to the next row
        table.row();
        // add the start-game button sized 300x60 with a margin-bottom of 10 units
        table.add( startGameButton ).size( 300f, 60f ).uniform().spaceBottom( 10 );
        // move to the next row
        table.row();
        // add the options button in a cell similiar to the start-game button's cell
        table.add( optionsButton ).uniform().fill().spaceBottom( 10 );
        // move to the next row
        table.row();
        // add the about button in a cell similiar to the start-game button's cell
        table.add( aboutButton ).uniform().fill();
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
		// TODO Auto-generated method stub
		
	}

}
