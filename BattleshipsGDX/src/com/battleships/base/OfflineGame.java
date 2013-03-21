package com.battleships.base;

import com.badlogic.gdx.Game;

public class OfflineGame extends Game {

	@Override
	public void create() {
		Resources.init();
		// TODO Auto-generated method stub
		this.setScreen(new GameScreen());
	}
}

