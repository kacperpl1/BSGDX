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
	static Texture iconTexture;
	static Texture shopToggleTexture;
	static Texture shopGridTexture;
	static Texture inventoryGridTexture;
	static TextureRegion[] ItemTextureRegion;
	
	static TextureRegion[] BaseTextureRegion;
	static TextureRegion[] ColorTextureRegion;
	static TextureRegion[] HealthbarTextureRegion;
	static TextureRegion[] ProjectileTextureRegion;
	static TextureRegion[] TowerTextureRegion;
	static TextureRegion VisorTextureRegion;

	static void init()
	{

		mapTexture = new Texture(Gdx.files.internal("data/TilemapLow.png"));
		mapTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		miniMapTexture = new Texture(Gdx.files.internal("data/minimap.png"));
		miniMapTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		iconTexture = new Texture(Gdx.files.internal("data/icon.png"));
		iconTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		shopToggleTexture = new Texture(Gdx.files.internal("data/shop_toggle.png"));
		shopToggleTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		shopGridTexture = new Texture(Gdx.files.internal("data/weapons.png"));
		shopGridTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		inventoryGridTexture = new Texture(Gdx.files.internal("data/inventory.png"));
		inventoryGridTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		Texture texture = new Texture(Gdx.files.internal("data/weapons.png"));
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		TextureRegion[][] tmp = TextureRegion.split(texture, texture.getWidth()/4,texture.getHeight()/4);                                // #10
		ItemTextureRegion = new TextureRegion[16];
		int index = 0;
        for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                	ItemTextureRegion[index++] = tmp[i][j];
                }
        }
		
		texture = new Texture(Gdx.files.internal("data/base.png"));
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		tmp = TextureRegion.split(texture, texture.getWidth()/4,texture.getHeight()/4);                                // #10
		BaseTextureRegion = new TextureRegion[16];
		index = 0;
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
     

		texture = new Texture(Gdx.files.internal("data/HealthBar.png"));
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		tmp = TextureRegion.split(texture, texture.getWidth(),texture.getHeight()/10);                                // #10
		HealthbarTextureRegion = new TextureRegion[10];
		index = 0;
        for (int i = 0; i < 10; i++) {
        	HealthbarTextureRegion[index++] = tmp[i][0];
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
