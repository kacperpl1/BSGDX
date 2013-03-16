package com.battleships.network;

import java.util.HashMap;
import java.util.Map;

import com.battleships.base.UnitData;

public class UnitMap implements java.io.Serializable {
	private static final long serialVersionUID = 1580057948840257844L;
	public Map<Integer, UnitData> map = new HashMap<Integer, UnitData>();
}
