package com.battleships.base;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

public class Shop {

	boolean shop_toggle=false;
	int selected_item=0;
	float itemX;
	float itemY;
	
	int selected_inventory_item;
	PlayerShip owner;
	LinkedList<PlayerWeapon> inventory;
	private Actor shop_toggle_button;
	private Actor shop_grid;
	private int tileWidth = BaseGame.h/10;
	private Actor inventory_grid;
	
	Shop(PlayerShip own)
	{
		owner = own;
		inventory=owner.Inventory;
		
		shop_toggle_button = new Actor(){
	        public void draw (SpriteBatch batch, float parentAlpha) {
        		batch.setColor(1, 1, 1, 0.75f);
	            batch.draw(Resources.shopToggleTexture,getX(),getY(),getWidth(),getHeight());
	            batch.setColor(1, 1, 1, 1);
	        }
		};
		shop_toggle_button.setBounds(0, BaseGame.h*0.45f, tileWidth, tileWidth);

		shop_toggle_button.addListener(new InputListener() {
	        public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
	        	shop_toggle = !shop_toggle;
	        	shop_grid.setVisible(shop_toggle);
	    		inventory_grid.setVisible(shop_toggle);
	                return true;
	        }
		});
		BaseGame.hudStage.addActor(shop_toggle_button);
		
		shop_grid = new Actor(){
			
	        public void draw (SpriteBatch batch, float parentAlpha) {
        		batch.setColor(1, 1, 1, 0.5f);
	            batch.draw(Resources.shopGridTexture,getX(),getY(),getWidth(),getHeight());
	            batch.setColor(1, 1, 1, 1);
	            batch.draw(Resources.ItemTextureRegion[selected_item],itemX,itemY,tileWidth,tileWidth);
	        }
		};
		
		shop_grid.addListener(new InputListener() {
	        public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
	        	selected_item = ((int)(x)/tileWidth)%4 +(3-(((int)(y)/tileWidth)%4))*4;
	        	itemX=shop_grid.getX()+(selected_item%4)*tileWidth;
	        	itemY=shop_grid.getY()+shop_grid.getHeight()*0.75f-(selected_item/4)*tileWidth;
	        	shop_grid.toFront();
	                return true;
	        }
	        
	        public void touchDragged (InputEvent event, float x, float y, int pointer) {
	        	itemX=shop_grid.getX()+x-tileWidth/2;
	        	itemY=shop_grid.getY()+shop_grid.getHeight()*0.75f + y-shop_grid.getHeight()+tileWidth/2;
	    	}
	        
	        public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
	        	itemX=shop_grid.getX()+(selected_item%4)*tileWidth;
	        	itemY=shop_grid.getY()+shop_grid.getHeight()*0.75f -(selected_item/4)*tileWidth;
	        	shop_grid.toBack();

	        	if((shop_grid.getX()+x) > inventory_grid.getX() && (shop_grid.getX()+x)< inventory_grid.getX() + inventory_grid.getWidth()
						&& (shop_grid.getY()+y) < (inventory_grid.getY()+tileWidth) && (shop_grid.getY()+y) > inventory_grid.getY())
				{
					if(inventory.size() < 6)
					{
						PlayerWeapon newweapon = new PlayerWeapon(owner, selected_item);
						inventory.add(newweapon);
					}
	        		
				}
	        }
		});
		
		shop_grid.setBounds(BaseGame.w/2-tileWidth*2, BaseGame.h/2-tileWidth*2, 
				tileWidth*4, tileWidth*4);
    	shop_grid.setVisible(shop_toggle);
    	
    	itemX=shop_grid.getX();
    	itemY=shop_grid.getY()+shop_grid.getHeight()*0.75f;
    	
		inventory_grid = new Actor(){
        public void draw (SpriteBatch batch, float parentAlpha) {
    		batch.setColor(1, 1, 1, 0.75f);
            batch.draw(Resources.inventoryGridTexture, getX(),getY(),getWidth(),getHeight());
            int index = 0;
            for(PlayerWeapon current : inventory)
            {
            	batch.draw(Resources.ItemTextureRegion[current.weapon_id], getX()+index*tileWidth,getY(),tileWidth,tileWidth);
            	index++;
            }            
            batch.setColor(1, 1, 1, 1);
        }
	};
		inventory_grid.setVisible(shop_toggle);
		inventory_grid.setBounds(shop_grid.getX()-shop_grid.getWidth()/4,tileWidth,tileWidth*6,tileWidth);
		
		inventory_grid.addListener(new InputListener() {
	        public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
	        	if(inventory.size()==0)
	        		return false;
	        	
	        	selected_inventory_item = (int)(x)/tileWidth;
	        	selected_item = inventory.get(selected_inventory_item).weapon_id;
	        	itemX=inventory_grid.getX()+selected_inventory_item*tileWidth;
	        	itemY=inventory_grid.getY();
	        	shop_grid.toFront();
	                return true;
	        }
	        
	        public void touchDragged (InputEvent event, float x, float y, int pointer) {
	        	itemX=inventory_grid.getX()+x-tileWidth/2;
	        	itemY=inventory_grid.getY()+y-tileWidth/2;
	    	}
	        
	        public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
	        	itemX=inventory_grid.getX()+selected_inventory_item*tileWidth;
	        	itemY=inventory_grid.getY();
	        	shop_grid.toBack();

	        	if((inventory_grid.getX()+x) > shop_grid.getX() && (inventory_grid.getX()+x)< shop_grid.getX() + shop_grid.getWidth()
						&& (inventory_grid.getY()+y) < (shop_grid.getY() + shop_grid.getHeight()) && (inventory_grid.getY()+y) > shop_grid.getY())
				{
	        		inventory.get(selected_inventory_item).Destroy();
					inventory.remove(selected_inventory_item);
					
		        	itemX=shop_grid.getX()+(selected_item%4)*tileWidth;
		        	itemY=shop_grid.getY()+shop_grid.getHeight()*0.75f-(selected_item/4)*tileWidth;
				}
	        }
		});
    	
		BaseGame.hudStage.addActor(shop_grid);	
		BaseGame.hudStage.addActor(inventory_grid);
	}
}
