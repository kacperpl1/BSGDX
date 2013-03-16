package com.battleships.base.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.battleships.base.OfflineGame;

public class GwtLauncher extends GwtApplication {
	@Override
	public GwtApplicationConfiguration getConfig () {
		GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(1366, 768);
		return cfg;
	}

	@Override
	public ApplicationListener getApplicationListener () {
		return new OfflineGame();
	}
}