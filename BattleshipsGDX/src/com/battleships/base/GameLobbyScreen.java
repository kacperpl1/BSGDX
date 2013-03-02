package com.battleships.base;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;

import com.badlogic.gdx.Gdx;
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

public class GameLobbyScreen implements Screen {
	private final Screen parentScreen;
	
	private Stage stage;
	private SpriteBatch batch;
	private int w = Gdx.graphics.getWidth();
	private int h = Gdx.graphics.getHeight();
	private Table team1Table = new Table(Resources.skin);
	private Table team2Table = new Table(Resources.skin);
	private Table team1PlayerListTable = new Table(Resources.skin);
	private Table team2PlayerListTable = new Table(Resources.skin);
	private TextButton disconnectButton = new TextButton( "Disconnect", Resources.skin );
	private TextButton readyButton = new TextButton( "Ready", Resources.skin );
	
	private Label team1Label = new Label("Team 1:", Resources.skin);
	private Label team2Label = new Label("Team 2:", Resources.skin);
	private ArrayList<String> team1PlayerList = new ArrayList<String>();
	private ArrayList<String> team2PlayerList = new ArrayList<String>();
	BlockingQueue<String> msgQueue;
	private String gameId = "";
	private String gameName = "";
	
	private BSClient lobbyClient;
	private Thread messenger;
	protected volatile boolean queueFlag = true;
	private boolean gameFlag = false;
	
	public GameLobbyScreen(final Screen parentScreen, final String gameId, final String gameName) {
		this.parentScreen = parentScreen;
		this.gameId = gameId;
		this.gameName = gameName;
		lobbyClient = BSClient.getInstance();
		msgQueue = lobbyClient.getGameLobbyQueue();
		
		disconnectButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event,float x,float y ) {
            	lobbyClient.leaveGame();
            	queueFlag = false;
            	messenger.interrupt();
            	BaseGame.instance.setScreen(parentScreen);
            }
        } );
		
		readyButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event,float x,float y ) {
            	if(lobbyClient.getPlayer().getSlotNumber() >= 0) {
            		lobbyClient.readyUp(gameId);
            	}
            }
        } );
		
		batch = new SpriteBatch();
		stage = new Stage();
		
		setLayout();
		
		team1Table.add(team1Label);
		team1Table.addActor(team1PlayerListTable);
		team1Table.row();
		team1Table.add(disconnectButton).align(Align.bottom);
		
		team2Table.add(team2Label);
		team2Table.addActor(team2PlayerListTable);
		team2Table.row();
		team2Table.add(readyButton).align(Align.bottom);
		
		stage.addActor(team1Table);
		stage.addActor(team2Table);
		
		this.messenger = new Thread(new Runnable() {
            @Override
            public void run() {
                while (queueFlag) {
                    try {
                        String message = msgQueue.take();
                        handleMessage(message);
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                    }
                }
            }
        }, "Messanger");
		this.messenger.start();
		
	}
	
	public void handleMessage(String message){
		StringTokenizer part = new StringTokenizer(message);
		switch(Integer.valueOf(part.nextToken())) {
			case 0 : {
				team1PlayerListTable.clear();
				team2PlayerListTable.clear();
				final ArrayList<Label> playerList = new ArrayList<Label>();
				for(int i = 0; i < 3; i++) {
					Label auxLabel = new Label(part.nextToken(), Resources.skin);
					playerList.add(auxLabel);
					auxLabel.addListener(new ClickListener() {
						@Override
			            public void clicked(InputEvent event,float x,float y ) {
							Label temp = (Label)(event.getListenerActor());
							if(temp.getText().toString().equals("Empty")) {
								lobbyClient.takeSlot(playerList.indexOf(temp));
							}
						}
					});
					team1PlayerListTable.add(auxLabel);
					team1PlayerListTable.row();
				}
				for(int i = 0; i < 3; i++) {
					Label auxLabel = new Label(part.nextToken(), Resources.skin);
					playerList.add(auxLabel);
					auxLabel.addListener(new ClickListener() {
						@Override
			            public void clicked(InputEvent event,float x,float y ) {
							Label temp = (Label)(event.getListenerActor());
							if(temp.getText().toString().equals("Empty")) {
								lobbyClient.takeSlot(playerList.indexOf(temp));
							}
						}
					});
					team2PlayerListTable.add(auxLabel);
					team2PlayerListTable.row();
				}
				break;
			}
			case 1 : {
				System.out.println("game should have started;p");
				gameFlag = true;
				
				break;
			}
			default : {
				break;
			}
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
		if(gameFlag){
			BaseGame.instance.setScreen( new GameScreen() );
		}
	}
	
	public void setLayout(){
		team1Table.setHeight(stage.getHeight() - 20);
		team1Table.setWidth(stage.getWidth()/2 - 10);
		team1Table.align(Align.top);
		team1PlayerListTable.setFillParent(true);
		
		
		team2Table.setHeight(stage.getHeight() - 20);
		team2Table.setWidth(stage.getWidth()/2 - 10);
		team2Table.setPosition(stage.getWidth()/2 - 10, 1);
		team2Table.align(Align.top);
		team2PlayerListTable.setFillParent(true);
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
		lobbyClient.leaveGame();
		
	}

}
