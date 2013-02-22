package com.battleships.base;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class SplashScreen implements Screen {

    private Texture splashTexture;
	private SpriteBatch batch;
	private float timer = 0;
    
	@Override
	public void render(float delta) {
        batch.begin();
        batch.draw( splashTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() );
        batch.end();
        
        timer+=delta;
        if(timer>3)
        	BaseGame.instance.setScreen(new GameScreen());
    }

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void show() {	    
        splashTexture = new Texture( "data/splash.png" );
        splashTexture.setFilter( TextureFilter.Linear, TextureFilter.Linear );
        batch = new SpriteBatch();
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
        splashTexture.dispose();
	}

}
