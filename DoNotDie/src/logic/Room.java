package logic;

import java.util.Random;

public class Room
{
	public Room (int numenem, int numtre, int level, int back, int l, int c, int r, int u, int d)
	{
		Random rand = new Random ();
		int seed = rand.nextInt (3), race;
		Boolean g = rand.nextBoolean ();
		
		if (numenem > 0)
		{
			enemies = new Character [numenem];
			
			for (int i = 0; i < numenem; i++)
			{
				race = rand.nextInt (10);
				enemies [i] = new Character (null, race, g, level - rand.nextInt (4),
						level - rand.nextInt (4), level - rand.nextInt (4),
						level - rand.nextInt (4), level - rand.nextInt (4),
						level - rand.nextInt (4), level - rand.nextInt (4), level);
				enemies [i].setInd (i);
			}
		}
		
		if (numtre > 0)
		{
			treasures = new Treasure [numtre];
			
			for (int i = 0; i < numtre; i++)
			{
				if (seed == 0)
					treasures [i] = new Misc ((level * 10) + 1);
				else if (seed == 1)
					treasures [i] = new Weapon (level);
				else
					treasures [i] = new Armor (level);
			}
		}
		
		
		connections [0] = back;
		connections [1] = l;
		connections [2] = c;
		connections [3] = r;
		connections [4] = u;
		connections [5] = d;

		numenemies = numenem;
		numtreasures = numtre;
	}
	
	public String toString ()
	{
		String ret = "Connections:";
		if (connections [0] != -1)
			ret += "\nBack: " + connections [0];
		if (connections [1] != -1)
			ret += "\nLeft: " + connections [1];
		if (connections [2] != -1)
			ret += "\nCenter: " + connections [2];
		if (connections [3] != -1)
			ret += "\nRight: " + connections [3];
		if (connections [4] != -1)
			ret += "\nUp: " + connections [4];
		if (connections [5] != -1)
			ret += "\nDown: " + connections [5];
		
		return ret;
	}
	
	// Methods to deal with enemies
	public Boolean roomCleared ()
	{
		for (int i = 0; i < numenemies; i++)
		{
			if (!enemyDead (enemies [i]))
				return false;
		}
		return true;
	}
	public Boolean enemyDead (Character c) { return (c == null || c.getHealth () == 0)? true : false; }
	public int hasEnemy (String e)
	{
		for (int i = 0; i < numenemies; i++)
			if (e.equals (enemies [i].getRace ().toLowerCase ()))
				return enemies [i].getInd ();
		return -1;
	}
	
	// Methods to deal with treasures
	public Boolean hasTreasures () { return numtreasures > 0; }
	public Boolean hasTreasure (String item)
	{
		for (int i = 0; i < numtreasures; i++)
			if (treasures [i].getName().toLowerCase().equals (item.toLowerCase ()))
				return true;
		return false;
	}
	public Boolean hasTreasure (int item)
	{
		return item < numtreasures && item >= 0;
	}
	public Treasure getTreasure (String item)
	{
		Treasure hold;
		for (int i = 0; i < numtreasures; i++)
		{
			if (treasures [i].getName ().toLowerCase().equals (item.toLowerCase()))
			{
				hold = treasures [i];
				removeTreasure (i);
				return hold;
			}
		}
		return null;
	}
	public Treasure getTreasure (int item)
	{
		Treasure hold = treasures [item];
		removeTreasure (item);
		return hold;
	}
	public void listTreasures ()
	{
		for (int i = 0; i < numtreasures; i++)
			System.out.println (i + ": " + treasures [i].getName ());
	}
	private void removeTreasure (int ind)
	{
		for (int i = ind; i < numtreasures - 1; i++)
			treasures [i] = treasures [i + 1];
		treasures [numtreasures - 1] = null;
		numtreasures--;
	}

	
	public String description;
	public Character [] enemies;
	public Treasure [] treasures;
	public int numenemies, numtreasures;
	// There will be a back, left, center, right, up, down, in that order
	public int [] connections = {-1, -1, -1, -1, -1, -1};
}
