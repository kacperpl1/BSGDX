package com.battleships.network;

import java.io.Serializable;

import com.badlogic.gdx.math.Vector2;

public class UnitData implements Serializable {
	private static final long serialVersionUID = -340273071479558278L;
	public enum Type {
		SHIP, CREEP, TOWER, UNKNOWN;
	}
	public Vector2 position = new Vector2(0,0);
	public short health = -1;
	public int  slot = -1;
	public Type type = Type.UNKNOWN;
	public String gameID = "";
}
