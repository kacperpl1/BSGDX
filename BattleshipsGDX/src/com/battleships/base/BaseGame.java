package com.battleships.base;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;

public class BaseGame extends Game {

	static BaseGame instance;
	static Controller gamepad = null; 
	@Override
	public void create() {
		instance = this;
		Resources.init();

		if(Controllers.getControllers().size>0)
			gamepad = Controllers.getControllers().iterator().next();
		// TODO Auto-generated method stub
		this.setScreen(new MenuScreen());
	}

}
