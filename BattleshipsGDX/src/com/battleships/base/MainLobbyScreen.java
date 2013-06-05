package com.battleships.base;

import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.battleships.network.BSClient;

public class MainLobbyScreen implements Screen {
	private Screen mainLobbyScreen;
	
	private Stage stage;
	private SpriteBatch batch;
	private int w = Gdx.graphics.getWidth();
	private int h = Gdx.graphics.getHeight();
	private Table leftTable = new Table(Resources.skin);
	private Table rightTable = new Table(Resources.skin);
	private Table playerListTable = new Table(Resources.skin);
	private Table gameListTable = new Table(Resources.skin);
	private TextButton disconnectButton = new TextButton( "Disconnect", Resources.skin );
	private TextButton createGameButton = new TextButton( "Create Game", Resources.skin );
	
	private Label playerListLabel = new Label("Player List:", Resources.skin);
	private Label gameListLabel = new Label("Game List:", Resources.skin);
	private BlockingQueue<String> msgQueue;
	private String gameName = "";
	
	private BSClient lobbyClient;
	
	private BSPreferences prefs = new BSPreferences();
	
	public MainLobbyScreen(final Screen parentScreen){
		// Get instance of BSClient and start it
		lobbyClient = BSClient.getInstance();
	    lobbyClient.init(prefs.getUserName());
		try{
			lobbyClient.start();
		}catch(Exception e){
			e.printStackTrace();
		}
		msgQueue = lobbyClient.getMainLobbyQueue();
		
		this.mainLobbyScreen = this;
		
		// Initialize graphics, buttons, set layout
		batch = new SpriteBatch();
		stage = new Stage();
		Gdx.input.setInputProcessor( stage );
		
		disconnectButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event,float x,float y )
            {
            	dispose();
            	BaseGame.instance.setScreen(parentScreen);
            }
        } );
		
		createGameButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event,float x,float y )
            {
            	Gdx.input.getTextInput(new TextInputListener() {
					@Override
					public void input (String text) {
						gameName = text;
						if(gameName.length() > 0) {
		            		lobbyClient.createGame(gameName.replace(" ", "_"));
		            		while(true){
		            			if(lobbyClient.getPlayer().getGameId().length() > 0){
		            				BaseGame.instance.setScreen(new GameLobbyScreen(mainLobbyScreen, lobbyClient.getPlayer().getGameId(), gameName));
		            				break;
		            			}
		            		}
		            	}
					}

					@Override
					public void canceled () {
						gameName = "";
					}
				}, "Enter name of game", prefs.getUserName()+"\'s game");
            }
        } );
		
		playerListTable.clear();
		gameListTable.clear();
		setLayout();

        leftTable.add(disconnectButton);
		leftTable.row();
        leftTable.add(playerListLabel).align(Align.bottom);

		leftTable.addActor(playerListTable);

        rightTable.add(createGameButton);
		rightTable.row();
        rightTable.add(gameListLabel).align(Align.bottom);
		rightTable.addActor(gameListTable);
		
		stage.addActor(leftTable);
		stage.addActor(rightTable);
	}
	
	public void handleMessage(String message){
		StringTokenizer part = new StringTokenizer(message);
		switch(Integer.valueOf(part.nextToken())) {
			// Message containing list of players
			case 0 : {
				playerListTable.clear();
				while(part.hasMoreTokens()){
					Label auxLabel = new Label(part.nextToken(), Resources.skin);
					playerListTable.add(auxLabel);
					playerListTable.row();
					playerListTable.validate();
				}
				break;
			}
			// Message containing list of games
			case 1 : {
				gameListTable.clear();
				while(part.hasMoreTokens()){
					Label auxLabel = new Label(part.nextToken(), Resources.skin);
					final String gId = part.nextToken();
					auxLabel.addListener(new ClickListener() {
						@Override
			            public void clicked(InputEvent event,float x,float y ) {
							Label temp = (Label)(event.getListenerActor());
							BaseGame.instance.setScreen(new GameLobbyScreen(mainLobbyScreen, gId, temp.getText().toString()));
							lobbyClient.joinGame(gId);
						}
					});
					gameListTable.add(auxLabel).spaceTop(10);
					gameListTable.row();
				}
			}
			default : break;
		}
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor( 0f, 0f, 0f, 1f );
        Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT );
        
        try {
        	if(!msgQueue.isEmpty()) {
	            String message = msgQueue.take();
	            handleMessage(message);
        	}
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
        
        batch.begin();
        batch.draw( Resources.splashTexture, 0, 0, w, h );
        batch.end();
		stage.draw();
	}

	public void setLayout() {
		leftTable.setHeight(stage.getHeight() - 20);
		leftTable.setWidth(stage.getWidth()/2 - 10);
		leftTable.align(Align.top);
		
		playerListTable.setFillParent(true);
		
		rightTable.setHeight(stage.getHeight() - 20);
		rightTable.setWidth(stage.getWidth()/2 - 10);
		rightTable.setPosition(stage.getWidth()/2 - 10, 1);
		rightTable.align(Align.top);
		
		gameListTable.setFillParent(true);
	}
	
	@Override
	public void resize(int width, int height) {
		setLayout();
	}

	@Override
	public void show() {
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
