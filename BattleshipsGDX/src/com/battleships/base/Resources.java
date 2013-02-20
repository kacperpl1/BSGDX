package com.battleships.base;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Resources {
	
	
	static Texture touchpadBase;
	static Texture touchpadKnob;
	static Texture mapTexture;
	static Texture miniMapTexture;
	static TextureRegion[] BaseTextureRegion;
	static TextureRegion[] ColorTextureRegion;
	static TextureRegion[] ProjectileTextureRegion;
	static TextureRegion[] TowerTextureRegion;
	static TextureRegion VisorTextureRegion;

	static void init()
	{

		mapTexture = new Texture(Gdx.files.internal("data/TilemapLow.png"));
		mapTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		miniMapTexture = new Texture(Gdx.files.internal("data/minimap.png"));
		miniMapTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		Texture texture = new Texture(Gdx.files.internal("data/base.png"));
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		TextureRegion[][] tmp = TextureRegion.split(texture, texture.getWidth()/4,texture.getHeight()/4);                                // #10
		BaseTextureRegion = new TextureRegion[16];
		int index = 0;
        for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                        BaseTextureRegion[index++] = tmp[i][j];
                }
        }

		texture = new Texture(Gdx.files.internal("data/color.png"));
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		tmp = TextureRegion.split(texture, texture.getWidth()/4,texture.getHeight()/4);                                // #10
		ColorTextureRegion = new TextureRegion[16];
		index = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
            	ColorTextureRegion[index++] = tmp[i][j];
            }
        }
      
        texture = new Texture(Gdx.files.internal("data/projectiles.png"));
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		tmp = TextureRegion.split(texture, texture.getWidth()/4,texture.getHeight()/4);                                // #10
		ProjectileTextureRegion = new TextureRegion[16];
		index = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
            	ProjectileTextureRegion[index++] = tmp[i][j];
            }
        }
        
        texture = new Texture(Gdx.files.internal("data/Towers.png"));
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		tmp = TextureRegion.split(texture, texture.getWidth()/2,texture.getHeight());                                // #10
		TowerTextureRegion = new TextureRegion[2];
		TowerTextureRegion[0]=tmp[0][0];
		TowerTextureRegion[1]=tmp[0][1];
        
        texture = new Texture(Gdx.files.internal("data/vision.png")); 
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        VisorTextureRegion = new TextureRegion(texture, 512,512);
		
		touchpadBase = new Texture(Gdx.files.internal("data/onscreen_control_base.png"));
		touchpadBase.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		touchpadKnob = new Texture(Gdx.files.internal("data/onscreen_control_knob.png"));
		touchpadKnob.setFilter(TextureFilter.Linear, TextureFilter.Linear);
	}
}
