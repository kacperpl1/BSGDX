package com.battleships.base;

import com.badlogic.gdx.Game;

public class OfflineGame extends Game {

	@Override
	public void create() {
		Resources.init();
		// TODO Auto-generated method stub
		GameScreen.test_mode = true;
		this.setScreen(new GameScreen());
	}
}

