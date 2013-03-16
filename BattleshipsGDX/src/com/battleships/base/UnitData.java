package com.battleships.base;

import java.io.Serializable;

import com.badlogic.gdx.math.Vector2;

public class UnitData implements Serializable {
	private static final long serialVersionUID = -340273071479558278L;
	
	public Vector2 position = new Vector2(0,0);
	public short health = -1;
	public short  slot = -1;
	public short type = -1;
	public String gameID = "";
	public int unitKey = -1;
}
