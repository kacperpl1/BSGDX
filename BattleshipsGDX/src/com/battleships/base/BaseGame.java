package com.battleships.base;

import com.badlogic.gdx.Game;

public class BaseGame extends Game {

	static BaseGame instance;
	@Override
	public void create() {
		instance = this;
		Resources.init();
		this.setScreen(new MenuScreen());
	}
}
