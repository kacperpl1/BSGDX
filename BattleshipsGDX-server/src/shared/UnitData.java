package shared;

import com.badlogic.gdx.math.Vector2;

public class UnitData {
	public enum Type {
		SHIP, CREEP, TOWER, UNKNOWN;
	}
	public Vector2 velocity = new Vector2(0,0);
	public Vector2 position = new Vector2(0,0);
	public int health = -1;
	public Type type = Type.UNKNOWN;
}
