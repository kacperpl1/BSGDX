package com.battleships.base;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class OptionsScreen implements Screen {

	private Stage stage;
	private SpriteBatch batch;
	private int w = Gdx.graphics.getWidth();
	private int h = Gdx.graphics.getHeight();
	private BSPreferences prefs = new BSPreferences();
	
	private Table table = new Table(Resources.skin);
	private Label nameLabel = new Label("Name:", Resources.skin);
	private TextField nameText = new TextField(prefs.getUserName(), Resources.skin);
	private Label serverIPLabel = new Label("Server address:", Resources.skin);
	private TextField serverIPText = new TextField(prefs.getServerIp(), Resources.skin);
	private TextButton backButton = new TextButton( "Save & back", Resources.skin );
	
	public OptionsScreen(final Screen parentScreen) {
		batch = new SpriteBatch();
		stage = new Stage();
		Gdx.input.setInputProcessor( stage );
		
		backButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event,float x,float y )
            {
            	if(nameText.getText().length() > 0) {
            		prefs.setUserName(nameText.getText());
            	}
            	if(serverIPText.getText().length() > 0) {
            		prefs.setServerIp(serverIPText.getText());
            	}
            	dispose();
            	BaseGame.instance.setScreen(parentScreen);
            }
        } );
		
		table.defaults().width(200);
		table.setFillParent(true);
		table.add(nameLabel);
		table.add(nameText);
		table.row();
		table.add(serverIPLabel);
		table.add(serverIPText);
		table.row();
		table.row();
		table.add(backButton);
		table.row();
		stage.addActor(table);
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
		table.setFillParent(true);
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
		// TODO Auto-generated method stub
		
	}

}
