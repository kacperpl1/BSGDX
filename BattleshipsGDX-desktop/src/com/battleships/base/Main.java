package com.battleships.base;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "BattleshipsGDX";
		cfg.useGL20 = true;
		cfg.width = 1280;
		cfg.height = 800;
		
		if(args.length>0 && args[0].equals("debug"))
			BaseGame.debug_mode=true;
		
		new LwjglApplication(new BaseGame(), cfg);
	}
}
