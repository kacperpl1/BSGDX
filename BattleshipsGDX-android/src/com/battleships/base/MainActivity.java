package com.battleships.base;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class MainActivity extends AndroidApplication {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.useGL20 = true;
        
        initialize(new BaseGame(), cfg);
    }
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());
	}
}