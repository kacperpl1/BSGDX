package server;

public class PlayerWeapon extends Weapon {
	
	static float FireDelayData[] = 	{1, 0.5f, 1.5f, 3, 0.5f, 0.2f, 0.5f, 1, 0.3f, 1, 2, 2, 0.5f, 0.5f, 1.3f, 0.3f};
	static int RangeData[] = 		{1250, 1000, 1000, 1000, 1000, 1000, 1000, 1500, 1250, 1500, 1000, 1500, 1000, 1000, 1250, 750};
	static int DamageData[] = 		{5, 10, 20, 50, 30, 15, 20, 40, 10, 35, 100, 175, 30, 50, 140, 30};
	static int CostData[] =			{225, 600, 320, 450, 600, 700, 1750, 1250, 2050, 1500, 1600, 1500, 2350, 4500, 4200, 3400};
	static int Speed[] =			{200, 200, 200, 200, 500, 500, 200, 200, 200, 300, 200, 200, 500, 500, 500, 500};

	public PlayerWeapon(Unit o, int type) {
		super(o, type);
		// TODO Auto-generated constructor stub
	}
	
	void defaultProperties()
	{
		FireDelay = FireDelayData[weapon_id];
		Range = RangeData[weapon_id]/6;
		Damage = DamageData[weapon_id];
	}
}
