package logic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

public class DND
{
	// Main game driver
	// Makes calls to all other driver methods
	public static void main (String [] args)
	{

		Map map = new Map ();
		Character p1 = null;
		String input = "";
		String prompt = "";
		String BASEMSG = "\nWhat do you want to do?\n";
		String BASEOPTIONS = "[(I)nspect]         [I(N)ventory]       [(S)ee Stats]       [(Q)uit]\n";
		String RGTMSG = "[Move (R)ight]";
		String LEFMSG = "[Move (L)eft]";
		String UPMSG = "[Move (U)p]";
		String DWNMSG = "[Move (D)own]";
		String CTRMSG = "[Move (F)orward]";
		String BCKMSG = "[Move (B)ackward]";
		String [] msgs = {BCKMSG, LEFMSG, CTRMSG, RGTMSG, UPMSG, DWNMSG};
		int choice;
		int textchoice = 3;
		int [] textspeeds = {120, 90, 40, 0};
		
		//String DIFFICULTY = "[(E)asy]   [(M)edium]   [(H)ard]\n";
		
		slowPrint ("Welcome to Do Not Die, 1st Edition!!\n" +
				"We hope you enjoy our game!! :D\n\n", textspeeds [textchoice]);
		
		// Main menu loop
		while (inputvalid (input))
		{
			slowPrint ("\nWhat would you like to do?\n", textspeeds [textchoice]);
			slowPrint (String.format ("%n%-23s%-23s%-23s%n%-23s%-23s%-23s%n> ",
					"[(C)hoose Character]", "[C(H)oose Map]", "[(S)ettings]",
					"[(P)lay]", "[(T)utorial]", "[(Q)uit]"), 0);
			
			if (!inputvalid (input = input ().toLowerCase ()))
				break;
			
			if (input.equals ("c"))
			{
				// Choose Character
				p1 = chooseChar (textspeeds [textchoice], p1);
			}
			else if (input.equals ("h"))
			{
				// Choose Map
				slowPrint ("\nCurrently unimplemented\n", 0);
			}
			else if (input.equals ("s"))
			{
				// Change Settings
				textchoice = changeSettings (textchoice);
			}
			else if (input.equals ("p"))
			{
				// Play game
				if (p1 == null)
				{
					slowPrint ("\nHey, you need to choose your character!\n", textspeeds [textchoice]);
					p1 = chooseChar (textspeeds [textchoice], p1);
					if (p1 == null)
						continue;
					break;
				}
				else
				{
					slowPrint ("All right! Ready to play?\n", textspeeds [textchoice]);
					break;
				}
			}
			else if (input.equals ("t") || input.equals ("tutorial"))
			{
				slowPrint ("\nNot yet implemented\n", textspeeds [textchoice]);
			}
			else if (input.equals ("q") || input.equals ("quit"))
				break;
			else
				slowPrint ("\nNot a choice\n", 0);
		}
		
		// Commented out until implemented or deadline is reached
		/*slowPrint ("Set Difficulty:\n" + DIFFICULTY + "\n> ", textspeeds [textchoice]);
		input = input ().toLowerCase ();*/

		// Main gameplay loop
		while (inputvalid (input) && !p1.isDead() && !map.allCleared ())
		{
			// Fight enemies in the room before you can do anything else
			if (map.current.numenemies > 0 && !map.current.roomCleared ())
			{
				if (map.current.numenemies > 1)
					slowPrint ("There are " + map.current.numenemies +
							" enemies in the room, prepare to fight.\n\n", textspeeds [textchoice]);
				else
					slowPrint ("There is an enemy in the room, prepare to fight.\n\n", textspeeds [textchoice]);
				
				battle (p1, map, textspeeds [textchoice]);
			}
			
			if (p1.isDead ())
				break;
			// Room is now cleared
			
			// Build string with all movement options and prompt user
			prompt = BASEOPTIONS;
			for (int i = 0; i < map.current.connections.length; i++)
			{
				if (map.current.connections [i] >= 0)
					prompt += String.format ("%-20s", msgs [i]);
			}
			slowPrint (BASEMSG, textspeeds [textchoice]);
			slowPrint (prompt + "\n> ", 0);
			
			if (!inputvalid (input = input ().toLowerCase()))
				break;
			
			// Check if user wants to quit. If yes, exit loop, so game ends, if no, continue as normal
			if (!inputvalid (input))
				break;
			
			switch (input)
			{
				case ("s"):
				{
					// Print player stats
					slowPrint ("Stats:\n", textspeeds [textchoice]);
					slowPrint (p1.printStats () + "\n", 0);
					break;
				}
				case ("i"):
				{
					// Inspect room
					//map.current.printDescription ();
					// If room has treasures, list them and allow player to pick one up per inspect action
					if (map.current.hasTreasures ())
					{
						slowPrint ("\nWhat do you want to pick up?\n\n", textspeeds [textchoice]);
						slowPrint ("Treasures:\n", textspeeds [textchoice]);
						map.current.listTreasures ();
						slowPrint ("\nB: Back\n> ", textspeeds [textchoice]);
						
						while (inputvalid (input = input()))
						{
							if (input.equals ("b") || input.equals ("back"))
								break;
							try
							{
								choice = Integer.parseInt (input);
								if (map.current.hasTreasure (choice))
								{
									p1.addToInventory (map.current.getTreasure (choice));
									break;
								}
								else
								{
									slowPrint ("That's not an item you can pick up.\n" +
											"Here's what you can:\n", textspeeds [textchoice]);
									map.current.listTreasures ();
									slowPrint ("\nB: Back\n> ", textspeeds [textchoice]);
									slowPrint ("What do you want to pick up?\n> ", textspeeds [textchoice]);
								}
							}
							catch (NumberFormatException e)
							{
								if (map.current.hasTreasure (input))
								{
									p1.addToInventory (map.current.getTreasure (input));
									break;
								}
								else
								{
									slowPrint ("That's not an item you can pick up.\n" +
											"Here's what you can:\n", textspeeds [textchoice]);
									map.current.listTreasures ();
									slowPrint ("\nB: Back\n> ", textspeeds [textchoice]);
									slowPrint ("What do you want to pick up?\n> ", textspeeds [textchoice]);
								}
							}
							
						}
					}
					else
						slowPrint ("Nothing in the room\n", textspeeds [textchoice]);
					break;
				}
				case ("n"):
				{
					slowPrint (BASEMSG, textspeeds [textchoice]);
					slowPrint (String.format ("%n%-20s%-20s%-20s%n%-20s%-20s%-20s%n> ",
							"[(E)quip]", "[(U)nequip]", "[(Vi)ew Bag]",
							"[(V)iew Equipped]", "[(D)rop]", "[(B)ack]"), 0);
					
					while (inputvalid (input = input ().toLowerCase ()))
					{
						if (input.equals ("e") || input.equals ("equip"))
						{
							slowPrint ("\nEquip what?\n", textspeeds [textchoice]);
							p1.inventoryCheck (0);
							slowPrint ("\nB: Back\n> ", 0);
							
							while (inputvalid (input = input ()))
							{
								if (input.equals ("b") || input.equals ("back"))
									break;
								try
								{
									choice = Integer.parseInt (input);
									if (choice <= p1.inventory.size ())
									{
										p1.equip (map.current, choice);
										break;
									}
									else
									{
										slowPrint ("\nThat's not an item in your inventory.\n" +
												"Here's your inventory:\n\n", textspeeds [textchoice]);
										p1.inventoryCheck (0);
									}
								}
								catch (NumberFormatException e)
								{
									if (p1.inInventory (input) >= 0)
									{
										p1.equip (map.current, input);
										break;
									}
									else
									{
										slowPrint ("\nThat's not an item in your inventory.\n" +
												"Here's your inventory:\n\n", textspeeds [textchoice]);
										p1.inventoryCheck (0);
									}
								}
							}
							
							if (input.equals ("b") || input.equals ("back"))
								break;
						}
						else if (input.equals ("u") || input.equals ("unequip"))
						{
							slowPrint ("\nUnequip what?\n\n", textspeeds [textchoice]);
							p1.equippedCheck (true, 0);
							slowPrint ("\nB: Back\n> ", 0);
							
							while (inputvalid (input = input ()))
							{
								if (input.equals ("b") || input.equals ("back"))
									break;
								try
								{
									choice = Integer.parseInt (input);
									if (choice <= 5)
									{
										p1.unequip (choice);
										break;
									}
									else
									{
										slowPrint ("\nThat's not an equipped item.\n" +
												"Here's what's equipped:\n\n", textspeeds [textchoice]);
										p1.equippedCheck (true, 0);
									}
								}
								catch (NumberFormatException e)
								{
									choice = -1;
									for (int i = 0; i < 5; i++)
										if (p1.equipped [i].getName ().equals (input))
										{
											choice = i;
											break;
										}
									
									if (choice >= 0)
									{
										p1.unequip (choice);
										break;
									}
									else
									{
										slowPrint ("\nThat's not an item in your inventory.\n" +
												"Here's your inventory:\n\n", textspeeds [textchoice]);
										p1.equippedCheck (true, 0);
									}
								}
							}
							
							if (input.equals ("b") || input.equals ("back"))
								break;
						}
						else if (input.equals ("vi") || input.equals ("view bag"))
						{
							p1.inventoryCheck (textspeeds [textchoice]);
						}
						else if (input.equals ("v") || input.equals ("view equipped"))
						{
							p1.equippedCheck (false, textspeeds [textchoice]);
						}
						else if (input.equals ("d") || input.equals ("drop"))
						{
							slowPrint ("\nNot implemented\n", 0);
						}
						else if (input.equals ("b") || input.equals ("back"))
							break;
						else
							slowPrint ("\nNot a valid choice", textspeeds [textchoice]);
						
						slowPrint (BASEMSG, textspeeds [textchoice]);
						slowPrint (String.format ("%n%-20s%-20s%-20s%n%-20s%-20s%-20s%n> ",
								"[(E)quip]", "[(U)nequip]", "[(Vi)ew Bag]",
								"[(V)iew Equipped]", "[(D)rop]", "[(B)ack]"), 0);
					}
					
					break;
				}
				case ("b"):
				{
					// Only allow movement if the current room has this connection
					if (map.current.connections [0] >= 0)
						map.moveBack ();
					else
						slowPrint ("There's no room behind you.\n", textspeeds [textchoice]);
					break;
				}
				case ("l"):
				{
					// Only allow movement if the current room has this connection
					if (map.current.connections [1] >= 0)
						map.moveLeft ();
					else
						slowPrint ("No room to the left to move into.\n", textspeeds [textchoice]);
					break;
				}
				case ("f"):
				{
					// Only allow movement if the current room has this connection
					if (map.current.connections [2] >= 0)
						map.moveCenter ();
					else
						slowPrint ("Stop trying to move forward.\n", textspeeds [textchoice]);
					break;
				}
				case ("r"):
				{
					// Only allow movement if the current room has this connection
					if (map.current.connections [3] >= 0)
						map.moveRight ();
					else
						slowPrint ("Can't go where there's no door.\n", textspeeds [textchoice]);
					break;
				}
				case ("u"):
				{
					// Only allow movement if the current room has this connection
					if (map.current.connections [4] >= 0)
						map.moveUp ();
					else
						slowPrint ("No moving up unless I say so!\n", textspeeds [textchoice]);
					break;
				}
				case ("d"):
				{
					// Only allow movement if the current room has this connection
					if (map.current.connections [5] >= 0)
						map.moveDown ();
					else
						slowPrint ("I'd let you go down in flames, but there's no opening down.../n" +
								"And no flames...\n", textspeeds [textchoice]);
					break;
				}
				default:
				{
					// Catch-all failsafe
					slowPrint ("Not a valid action\n", textspeeds [textchoice]);
					break;
				}
			}
		}
		
		slowPrint ("Thank you for playing!", textspeeds [textchoice]);
	}
	
	// Char selection driver
	static Character chooseChar (int len, Character p1)
	{
		String input;
		int choice = -1;
		Character player = p1;
		Character [] chars = new Character [3];

		slowPrint ("\nWhat would you like to do?\n\n", len);
		slowPrint ("[(N)ew Character]      [(C)hoose Character]\n[(D)elete Character]   [(B)ack]\n> ", 0);
		
		for (int i = 0; i < 3; i++)
			chars [i] = loadChar (i);
		
		// Main choose char loop
		while (inputvalid (input = input ().toLowerCase ()))
		{
			if (input.equals ("n") || input.equals ("new") || input.equals ("new character"))
			{
				// Only make new char if not at max already
				if (chars [2] == null)
					player = newChar (len);
				else
					slowPrint ("Sorry, our benevolent overlords have imposed a" +
							" limit of 3 saved characters.\nTry deleting one or all first. :)\n", 0);
			}
			else if (input.equals ("c") || input.equals ("choose") || input.equals ("choose character"))
			{
				// Ensure there are chars saved. If none, direct them to char creation
				if (chars [0] == null)
				{
					slowPrint ("\nSorry, there are no saved characters.\n" +
							"Let me point you in the right direction:\n\n", len);
					return newChar (len);
				}
				
				slowPrint ("\nSelect Which Character?\n", len);
				
				
				// Let them choose the char by the index or name
				while (input != null)
				{
					// Print out first 3 chars, because we decided 3 is max, but is scalable
					for (int i = 0; i < 3; i++)
					{
						if (chars [i] != null)
							slowPrint (i + ": " + chars [i] + "\n", 0);
						else
							break;
					}
					slowPrint ("> ", 0);
					
					input = input ();
					
					// Break if they want to quit, quit is after loop
					if (!inputvalid (input))
						break;
					
					// Turn input into an index, whether they put in the number or name
					try
					{
						// If input is the index
						choice = Integer.parseInt (input);
					}
					catch (NumberFormatException e)
					{
						// If input is the name
						for (int i = 0; i < 3; i++)
						{
							player = chars [i];
							if (player != null && player.is (input))
							{
								choice = i;
								break;
							}
							else if (player == null)
								break;
						}
					}
					
					// Load the corresponding char
					if (choice >= 0 && (player = chars [choice]) != null)
						break;
					else
						slowPrint ("\nThat's not a valid choice.\n", len);
				}
				// Quit here if they don't want to load
				if (!inputvalid (input))
					break;
				
				// Usability message, letting the user know their choice was confirmed
				slowPrint ("You loaded: " + player + "\n", 0);
				return player;
			}
			else if (input.equals ("d") || input.equals ("delete") || input.equals ("delete character"))
			{
				// Ensure there are chars saved. If none, return them to options
				if (chars [0] == null)
				{
					slowPrint ("Ummm, you have no characters to delete.\n" +
							"Make some first, then you can delete them.\n\n", len);
					break;
				}
				slowPrint ("\nDelete Which Character?\n", len);
				
				
				// Let them choose the char by the index or name
				while (input != null)
				{
					// Print out first 3 chars, because we decided 3 is max, but is scalable
					for (int i = 0; i < 3; i++)
					{
						if (chars [i] != null)
							slowPrint (i + ": " + chars [i] + "\n", 0);
						else
							break;
					}
					slowPrint ("> ", 0);
					input = input ();
					
					// Break if they want to quit, quit is after loop
					if (!inputvalid (input))
						break;
					
					// Turn input into an index, whether they put in the number or name
					try
					{
						// If input is the index
						choice = Integer.parseInt (input);
					}
					catch (NumberFormatException e)
					{
						// If input is the name
						for (int i = 0; i < 3; i++)
						{
							if (player != null && player.is (input))
							{
								choice = i;
								break;
							}
							else if (player == null)
								break;
						}
					}
					
					// Load the corresponding char
					if  (choice >= 0 && (player = chars [choice]) != null)
						break;
					else
						slowPrint ("\nThat's not a valid choice.\n", len);
				}
				// Quit here if they don't want to delete
				if (!inputvalid (input))
					break;

				// Usability message, letting the user know their choice was confirmed
				slowPrint ("You deleted: " + player + "\n", 0);
				delChar (player);
			}
			else if (input.equals ("b") || input.equals ("back"))
			{
				// Check if user has selected a char
				// If not, tell them they still have to, but return them anyway
				if (player == null)
				{
					slowPrint ("Um, you have not selected a character...\nEven if you don't do so now, " +
							"you will still be forced to before you play.\n", len );
				}
				
				return player;
			}
			else if (input.equals ("dev"))
			{
				// For speeding up the dev process, use a god stat char
				return new Character ("Chaos", 0, true, 200, 40, 40, 40, 40, 40, 40, 40, false);
			}
			else
			{
				// Catch-all failsafe
				slowPrint ("Invalid Input, please retry\n", len);
			}

			slowPrint ("\nWhat would you like to do?\n\n", len);
			slowPrint ("[(N)ew Character]      [(C)hoose Character]\n[(D)elete Character]   [(B)ack]\n> ", 0);
		}
		return null;
	}
	// Create new char process
	static Character newChar (int len)
	{
		String input;
		String name = "";
		String racestr = "";
		int race = 0;
		int Str = 0;
		int End = 0;
		int Int = 0;
		int Wil = 0;
		int Agl = 0;
		int Spd = 0;
		int Lck = 0;
		int statpoints = 10;
		Boolean gender = true;
		Boolean retry = true;
		Character player;
		
		// Name input
		slowPrint ("New Character, Ok.\n", len);
		slowPrint ("Let's go through the steps.\nName:\n> ", len);
		input = input ();
		if (inputvalid (input))
			name = input;
		slowPrint ("Um, " + name + "? You sure? Ok, well whatever, you're the player...\n\n", len);
		
		// Race Selection
		slowPrint ("Ok, Race:\n", len);
		slowPrint ("0:       Human\n1:         Elf\n2:         Orc\n3:       Gnome\n4:       Dwarf\n" +
		"5:  Dragonborn\n6:  Half-Troll\n7: Lizard-Folk\n8:    Cat-Folk\n9:    Tiefling\n> ", 0);
		
		if (!inputvalid (input = input ().toLowerCase ()))
			System.exit(0);
		
		while (inputvalid (input))
			if (input.equals ("human") || input.equals ("0"))
			{
				racestr = "Human";
				race = 0;
				break;
			}
			else if (input.equals ("elf") || input.equals ("1"))
			{
				racestr = "Elf";
				race = 1;
				break;
			}
			else if (input.equals ("orc") || input.equals ("2"))
			{
				racestr = "Orc";
				race = 2;
				break;
			}
			else if (input.equals ("gnome") || input.equals ("3"))
			{
				racestr = "Gnome";
				race = 3;
				break;
			}
			else if (input.equals ("dwarf") || input.equals ("4"))
			{
				racestr = "Dwarf";
				race = 4;
				break;
			}
			else if (input.equals ("dragonborn") || input.equals ("5"))
			{
				racestr = "Dragonborn";
				race = 5;
				break;
			}
			else if (input.equals ("half-troll") || input.equals ("6"))
			{
				racestr = "Half-Troll";
				race = 6;
				break;
			}
			else if (input.equals ("lizard-folk") || input.equals ("7"))
			{
				racestr = "Lizard-Folk";
				race = 7;
				break;
			}
			else if (input.equals ("cat-folk") || input.equals ("8"))
			{
				racestr = "Cat-Folk";
				race = 8;
				break;
			}
			else if (input.equals ("tiefling") || input.equals ("9"))
			{
				racestr = "Tiefling";
				race = 9;
				break;
			}
			else
			{
				slowPrint ("Nope. That's not a race. Try again:\n> ", len);
				input = input().toLowerCase();
			}
		slowPrint ("Oh, " + racestr + " huh?\nI kinda thought so, but " +
				"I wanted to make sure.\n\n", len);
		
		// Gender selection
		slowPrint ("So um, what's.... uh, what's your gender?:\n", len);
		slowPrint ("0: Male\n1: Female\n> ", 0);
		
		if (!inputvalid (input = input.toLowerCase ()))
			System.exit(0);
		
		while (inputvalid (input))
		{
			if (input.equals ("male") || input.equals ("0"))
			{
				gender = true;
				break;
			}
			else if (input.equals ("female") || input.equals ("1"))
			{
				gender = false;
				break;
			}
			else
			{
				slowPrint ("We're not that inclusive. Only male or female.\n> ", len);
				input = input ().toLowerCase ();
			}
		}
		if (gender)
			slowPrint ("You're a dude?\nOk, Ok, yes you do look manly, I didn't want to assume.\n\n", len);
		else
			slowPrint ("You're a dudette?\nNo, you don't look too manly, I just didn't want to assume.\n\n", len);
		
		// Stat point allocation
		slowPrint ("Ok, now you have to allocate stat points.\nYou get 10 points to" +
				" use across all 7 stats, be wise.\n", len);
		slowPrint ("Strength\nEndurance\nIntelligence\nWillpower\nAgility\nSpeed\nLuck\n\n", 0);
		
		while (statpoints != 0 && retry)
		{
			slowPrint ("Strength:\n> ", len);
			input = input ();
			
			try
			{
				Str = Integer.parseInt (input);
				if (statpoints >= Str)
					statpoints -= Str;
				else
				{
					statpoints = 10;
					slowPrint ("Um, that's more points than you have... START OVER!\n\n\n", len);
					continue;
				}
			}
			catch (NumberFormatException e)
			{
				slowPrint ("That's not a number, can you put a number?\nSpecifically" +
						" an integer less than or equal to the number of stat points left?\n", len);
				continue;
			}
			
			slowPrint ("Endurance:\n> ", len);
			input = input ();
			
			try
			{
				End = Integer.parseInt (input);
				if (statpoints >= End)
					statpoints -= End;
				else
				{
					statpoints = 10;
					slowPrint ("Um, that's more points than you have...\n" +
					"You spent too many points on Strength already.\nSTART OVER!\n\n\n", len);
					continue;
				}
			}
			catch (NumberFormatException e)
			{
				slowPrint ("That's not a number, can you put in a number?\nSpecifically" +
						" an integer less than or equal to the number of stat points left?\n", len);
				statpoints = 10;
				continue;
			}
			
			slowPrint ("Intelligence:\n> ", len);
			input = input ();
			
			try
			{
				Int = Integer.parseInt (input);
				if (statpoints >= Int)
					statpoints -= Int;
				else
				{
					statpoints = 10;
					slowPrint ("Um, that's more points than you have... START OVER!\n\n\n", len);
					continue;
				}
			}
			catch (NumberFormatException e)
			{
				slowPrint ("That's not a number, can you put in a number?\nSpecifically" +
						" an integer less than or equal to the number of stat points left?\n", len);
				statpoints = 10;
				continue;
			}
			
			slowPrint ("Willpower:\n> ", len);
			input = input ();
			
			try
			{
				Wil = Integer.parseInt (input);
				if (statpoints >= Wil)
					statpoints -= Wil;
				else
				{
					statpoints = 10;
					slowPrint ("Um, that's more points than you have... START OVER!\n\n\n", len);
					continue;
				}
			}
			catch (NumberFormatException e)
			{
				slowPrint ("That's not a number, can you put in a number?\nSpecifically" +
						" an integer less than or equal to the number of stat points left?\n", len);
				statpoints = 10;
				continue;
			}
			
			slowPrint ("Agility:\n> ", len);
			input = input ();
			
			try
			{
				Agl = Integer.parseInt (input);
				if (statpoints >= Agl)
					statpoints -= Agl;
				else
				{
					statpoints = 10;
					slowPrint ("Um, that's more points than you have... START OVER!\n\n\n", len);
					continue;
				}
			}
			catch (NumberFormatException e)
			{
				slowPrint ("That's not a number, can you put in a number?\nSpecifically" +
						" an integer less than or equal to the number of stat points left?\n", len);
				statpoints = 10;
				continue;
			}
			
			slowPrint ("Speed:\n> ", len);
			input = input ();
			
			try
			{
				Spd = Integer.parseInt (input);
				if (statpoints >= Spd)
					statpoints -= Spd;
				else
				{
					statpoints = 10;
					slowPrint ("Um, that's more points than you have... START OVER!\n\n\n", len);
					continue;
				}
			}
			catch (NumberFormatException e)
			{
				slowPrint ("That's not a number, can you put in a number?\nSpecifically" +
						" an integer less than or equal to the number of stat points left?\n", len);
				statpoints = 10;
				continue;
			}
			
			slowPrint ("Luck:\n> ", len);
			input = input ();
			
			try
			{
				Lck = Integer.parseInt (input);
				if (statpoints >= Lck)
					statpoints -= Lck;
				else
				{
					statpoints = 10;
					slowPrint ("Um, that's more points than you have... START OVER!\n\n\n", len);
					continue;
				}
			}
			catch (NumberFormatException e)
			{
				slowPrint ("That's not a number, can you put in a number?\nSpecifically" +
						" an integer less than or equal to the number of stat points left?\n", len);
				statpoints = 10;
				continue;
			}
			
			if (statpoints > 0)
				slowPrint ("Um, you have " + statpoints + " point(s) left... " +
						"Why didn't you use them you scrub?\nThat's what they're for!\n\n", len);
			
			// Double check the user doesn't want to retry
			slowPrint ("Are you sure you're good with this?\n", len);
			slowPrint (String.format ("%14s%2d%n", "Strength: ", Str ), 0);
			slowPrint (String.format ("%14s%2d%n", "Endurance: ", End), 0);
			slowPrint (String.format ("%14s%2d%n", "Intelligence: ", Int), 0);
			slowPrint (String.format ("%14s%2d%n", "Willpower: ", Wil), 0);
			slowPrint (String.format ("%14s%2d%n", "Agility: ", Agl), 0);
			slowPrint (String.format ("%14s%2d%n", "Speed: ", Spd), 0);
			slowPrint (String.format ("%14s%2d%n", "Luck: ", Lck), 0);
			slowPrint ("[(Y)es]   [(N)o]\n> ", 0);
			
			if ((input = input ().toLowerCase ()).equals ("y") || input.equals ("yes"))
				retry = false;
			else
				statpoints = 10;
		}
		
		// Create and save character
		player = new Character (name, race, gender, Str, End, Int, Wil, Agl, Spd, Lck, 1, true);
		
		if (!charExists (player))
			saveChar (player);
		else
			slowPrint ("\nThis character exists already. I'm not going to save them twice.\n", len);
		
		// Return character so that it will be selected for the user to simply finish
		return player;
	}
	// Output char to the text file, if the max of 3 saved chars is not met
	static void saveChar (Character c)
	{
		// Check for max chars save already
		// Redundancy for accuracy
		if (loadChar (2) != null)
			slowPrint ("Sorry, maximum of 3 saved characters.\nDelete one first.\n\n", 0);
		else
		{
			slowPrint ("Saving character\n\n", 0);
			// Append to the save file if it exists, create it if it doesn't
			try
			{ Files.write (Paths.get ("saved_chars.txt"), c.printCharacter ().getBytes (), StandardOpenOption.APPEND); }
			catch (IOException e)
			{
				PrintWriter out;
				try
				{
					out = new PrintWriter (new BufferedWriter (new FileWriter (new File ("saved_chars.txt"))));
					out.write (c.printCharacter (), 0, c.printCharacter ().length ());
					out.close ();
				}
				catch (IOException g)
				{
					slowPrint ("Error saving character, you can play, but will have to make again.\n\n", 0);
				}
			}
		}
	}
	// Will delete a char from the text file, if there
	static void delChar (Character c)
	{
		Character [] hold = new Character [3];
		int rem = 2;
		PrintWriter out = null;
		
		// Look through saved chars for the char to delete, and save the index
		for (int i = 0; i < 3; i++)
		{
			hold [i] = loadChar (i);
			if (hold [i] != null && Character.areEqual (c, hold [i]))
				rem = i;
			else if (hold [i] == null)
				break;
		}
		
		// Create new file
		try
		{
			out = new PrintWriter ("saved_chars.txt", "UTF-8");
		}
		catch (FileNotFoundException | UnsupportedEncodingException e)
		{
			slowPrint ("Error with file\n\n", 0);
			e.printStackTrace ();
			return;
		}
		
		// Write only the chars that are not the one to delete
		for (int i = 0; i < 3; i++)
		{
			if (hold [i] != null && i != rem)
				out.print (hold [i].printCharacter ());
			else if (hold [i] == null)
				break;
		}
		
		out.close ();
	}
	// Load char from the txt file and return it, if there, return null if not
	// Used by chooseChar, saveChar, and delChar
	static Character loadChar (int num)
	{
		Path save = Paths.get ("saved_chars.txt");
		Character load = null;
		
		// Change the input index to reflect the number of lines that must be skipped
		// Each char takes up 11 lines in the save file
		int i = num * 11;
		
		// Go through each set of 11 lines and build a new Char out of them, without re-editing the stats
		try
		{
			if (input (save.toFile (), i) != null)
				load = new Character (input (save.toFile (), i),
						Integer.parseInt (input (save.toFile (), i + 1)),
						Boolean.parseBoolean (input (save.toFile (), i + 2)),
						Integer.parseInt (input (save.toFile (), i + 3)),
						Integer.parseInt (input (save.toFile (), i + 4)),
						Integer.parseInt (input (save.toFile (), i + 5)),
						Integer.parseInt (input (save.toFile (), i + 6)),
						Integer.parseInt (input (save.toFile (), i + 7)),
						Integer.parseInt (input (save.toFile (), i + 8)),
						Integer.parseInt (input (save.toFile (), i + 9)),
						Integer.parseInt (input (save.toFile (), i + 10)), false);
		}
		catch (IOException e)
		{
			slowPrint ("Sorry, your computer has decided it doesn't enjoy files.\n\n", 0);
		}
		
		return load;
	}
	// Checks for the char in the text file, returns true if it's there
	// Used by newChar before saving char
	// Method to check if a char is a duplicate
	static boolean charExists (Character c)
	{
 		Character player;
 		boolean exists = false;
 		
 		for (int i = 0; i < 3; i++)
 		{
 			player = loadChar (i);
 			
 			if (player != null)
 			{
 				exists = Character.areEqual (c, player);
 				if (exists)
 					break;
 			}
 		}
 		return exists;
	}
	
	// Battle driver method
	// Takes in the player, and the current map.
	// Returns true if the player died
	static Boolean battle (Character p1, Map map, int len)
	{
		Character enemy = null;
		Character hold = null;
		Character [] order = new Character [map.current.numenemies + 1];
		Character [] ordercpy = new Character [map.current.numenemies + 1];
		Character [] enemies = new Character [map.current.numenemies];
		int turn = 0;
		int roll = 0;
		int selected;
		int maxinit;
		int dmg;
		String input = "";
		
		// Put player and enemies into an array for the turn utility
		slowPrint ("You rolled " + p1.initiative () + " for initiative.\n", len);
		order [0] = p1;
		for (int i = 0; i < map.current.numenemies; i++)
		{
			if (map.current.numenemies > 1)
				slowPrint (map.current.enemies[i].getRace() + " " + i + " rolled " +
			map.current.enemies [i].initiative () + " for initiative.\n", len);
			else
				slowPrint ("The " + map.current.enemies [i].getRace() + " rolled " +
			map.current.enemies [i].initiative () + " for initiative.\n", len);
			
			order [i + 1] = map.current.enemies [i];
		}
		
		// Reordering the array based on initiative rolls, higher rolls first, ties broken by race, then luck
		for (int i = 0; i <= map.current.numenemies; i++)
		{
			maxinit = i;
			for (int j = i; j <= map.current.numenemies && order [j] != null; j++)
			{
				// Try for initiative difference, break ties by race, then by luck
				if (order [j].compareTo (order [maxinit]) > 0 ||
						((order [j].compareTo (order [maxinit]) == 0) &&
						(order [j].getRace ().compareTo (order [maxinit].getRace ()) < 0)) ||
						((order [j].compareTo (order [maxinit]) == 0) &&
								order [j].getLuck () > order [maxinit].getLuck ()))
					maxinit = j;
			}
			hold = order [i];
			order [i] = order [maxinit];
			order [maxinit] = hold;
			ordercpy [i] = order [i];
		}
		hold = null;
		
		// Removing player from order, for target selection
		for (int i = 0; i < ordercpy.length - 1 && ordercpy [i] != null; i++)
		{
			if (!(ordercpy [i].getName () == null && hold == null))
			{
				hold = ordercpy [i];
				ordercpy [i] = ordercpy [i + 1];
				ordercpy [i + 1] = hold;
			}
			if (i < enemies.length)
				enemies [i] = ordercpy [i];
		}
		
		
		slowPrint ("\nThe order is:\n", len);
		// Print the battle order
		for (int i = 0; i < order.length && order [i] != null; i++)
			if (order [i].getName () != null)
				slowPrint (order [i].getName () + "\n", len);
			else
				slowPrint (order [i].getRace () + "\n", len);
		
		
		// Battle manager
		while (!p1.isDead () && !map.current.roomCleared ())
		{
			// Ensure no out of bounds exception or null pointer exception
			if (turn >= order.length || order [turn] == null)
			{
				slowPrint ("\nTop of the order again.\n", len);
				turn = 0;
			}
			
			input = input.toLowerCase ();
			
			// Player turn
			if (order [turn].getName () != null)
			{
				while (!input.equals ("a") && !input.equals ("attack"))
				{
					slowPrint ("\nIt's your turn, what do you want to do?", len);
					slowPrint ("\n[(A)ttack]   [(C)heck Bag]   [(P)erception Check]\n> ", 0);
					
					if (!inputvalid (input = input ().toLowerCase ()))
						break;
					
					if (input.equals ("a") || input.equals ("attack"))
					{
						slowPrint ("\nAttack who?\n", len);
						
						// Print out list of living enemies
						for (int i = 0; i < enemies.length && enemies [i] != null; i++)
							if (!enemies [i].isDead ())
								slowPrint (i + ": " + enemies [i].printEnemy () + "\n", 0);
						slowPrint (String.format ("Q: %11s%n> ", "Quit"), 0);
						
						// Loop to ensure an enemy is selected correctly
						while (inputvalid (input = input ().toLowerCase ()))
						{
							try
							{
								selected = Integer.parseInt (input);
								// Input validation for index selection
								if (selected >= enemies.length || enemies [selected] == null)
								{
									slowPrint ("Number not valid. Can you like be nice please?\n", len);
									for (int i = 0; i < enemies.length && enemies [i] != null; i++)
										if (!enemies [i].isDead ())
											slowPrint (i + ": " + enemies [i].printEnemy () + "\n", 0);
								}
								else
								{
									enemy = enemies [selected];
									break;
								}
							}
							catch (NumberFormatException e)
							{
								selected = map.current.hasEnemy (input);
								// Input validation for name selection
								if (selected < 0)
								{
									slowPrint ("That's not an enemy. Play nice.\n", len);
									for (int i = 0; i < enemies.length && enemies [i] != null; i++)
										if (!enemies [i].isDead ())
											slowPrint (i + ": " + enemies [i].printEnemy () + "\n", 0);
								}
								else
								{
									enemy  = enemies [selected];
									break;
								}
							}
						}
						if (!inputvalid (input))
							continue;
						
						// Roll vs. AC and attack if the roll is good enough
						roll = p1.rolld20 ();
						slowPrint ("\nYou rolled " + roll + " versus the " + enemy.getRace () + "'s AC,\n", len);
						
						if (roll > enemy.getAC ())
						{
							slowPrint ("\nExcellent, that's a success!\nNow roll for damage.\n", len);
							roll = p1.rolld20 ();
							if (roll == 20)
							{
								slowPrint ("\nCritical success! Double damage!\n", len);
								dmg  = p1.getDamage () * 2;
							}
							else if (roll == 1)
							{
								slowPrint ("\nAw man, critical failure! Half damage!\n", len);
								dmg  = p1.getDamage () / 2;
							}
							else
							{
								dmg = p1.getDamage ();
							}
							
							slowPrint ("Attacked for " + dmg + "\n", len);
							if (enemy.attacked (dmg))
							{
								slowPrint ("\n\nThe weapon swung true\nThe " +
										enemy.getRace () + " fell\nA fatal blow\nTo the left pinky toe.\n\n", len);
							}
							else
							{
								slowPrint (enemy.getRace () + " has " + enemy.getHealth () + " health left.\n", len);
							}
						}
						else
							slowPrint ("That ain't gonna cut it, sorry.\n", len);
						input = "";
						break;
					}
					else if (input.equals ("c") || input.equals ("check") || input.equals ("check bag"))
						// Check inventory
						p1.inventoryCheck (len);
					else if (input.equals ("p") || input.equals ("perception check"))
						// Check enemy description. Has no use other than amusement
						for (int i = 0; i < enemies.length; i++)
							enemies [i].printDescription (len);
					else
						// Catch-all failsafe
						slowPrint ("Invalid input.\n", len);
				}
			}
			else
			{
					slowPrint ("\nIt's " + order [turn].getRace () + "'s turn.\n", len);
					Character curEnemy = order[turn];
					boolean enemyHit = false;
					int enemyDmg = 0;
					
					int attackRoll = curEnemy.rolld20();
					slowPrint("The enemy rolled a " + attackRoll + " against your AC\n", len);
					
					//Handles the enemy's attack roll
					if (attackRoll > p1.getAC())
					{
						enemyHit = true;
					}
					
					//This handles an enemy's attack if they hit
					if (enemyHit)
					{
						if(attackRoll == 20)
						{
							slowPrint("Damn Son! They got you gooooooooood!\n", len);
							enemyDmg = curEnemy.getDamage() / 2;
						}
						else
						{
							enemyDmg = curEnemy.getDamage() / 3;
						}
						
						p1.attacked(enemyDmg);
						slowPrint("You've Been Struck for " + enemyDmg + "! You should get that checked!\n\n", len);
								
						//The player just died and handles what kind of enemy killed them
						if(p1.getHealth() == 0)
						{
							if(curEnemy.getRace() == "Human")
							{
								slowPrint("The Human rolls into your blind spot and swings his sword to end your life.\n"
										+ "Unfortunately for you, he has succeeded.\n", len);
							}
							else if(curEnemy.getRace() == "Elf")
							{
								slowPrint("In all the majesticness of an Elf's swiftness, this Elf raises his bow\n"
										+ "and looses two arrows straight in your heart!\n"
										+ "Today is not your day...\n", len);
							}
							else if(curEnemy.getRace() == "Orc")
							{
								slowPrint("The brute Orc rushes you, slams you against a wall, lifts you into the "
										+ "air, tears you in half, and flings your carcases across the room!\n"
										+ "The rats will feast tonight!\n", len); 
							}
							else if(curEnemy.getRace() == "Gnome")
							{
								slowPrint("This Gnome while small was quick because before you knew it, it had\n"
										+ "sliced the back of both your knees and slit your throat!\n"
										+ "You lay there bleeding out, watching as he robs you and skips away!", len);
							}
							else if(curEnemy.getRace() == "Dwarf")
							{
								slowPrint("This mighty Dwarf has gotten the best of you! Her mighty warcry offset\n"
										+ "you just long enough so that her axe throw would meet its target... your face.\n", len);
							}
							else if(curEnemy.getRace() == "DragonBorn")
							{
								slowPrint("You swear the last thing you heard was FUS-RO-DA, but you get the faint\n"
										+ "memory of also taking an arrow to the knee...\n", len);
							}
							else if(curEnemy.getRace() == "Half-Troll")
							{
								slowPrint("The Half-Troll removes its disguise to reveal three dwarfs who rush you\n"
										+ "and kick you in the shins until you die of laughter!\n", len);
							}
							else if(curEnemy.getRace() == "Lizard-Folk")
							{
								slowPrint("After slashing your guts so they fall out, it knocks you onto the ground\n"
										+ "with a tailwhip directly to the chest, and proceeds to eat your\n"
										+ "insides like a fancy afternoon snack!", len);
							}
							else if(curEnemy.getRace() == "Cat-Folk")
							{
								slowPrint("Welp she clawed your face off... Still think cats are cute?\n", len);
							}
							else if(curEnemy.getRace() == "Tiefling")
							{
								slowPrint("The Tiefling shot a giant fireball at you turning you into a pile\n"
										+ "of smouldering ashe! I told you it was a bad idea o say you were cold.\n", len);
							}
							else
							{
								slowPrint("You tripped and fell off a cliff while running away from the enemy like a little baby.\n"
										+ "On the express way down the cliffside, you broke your face, and bent just about every\n"
										+ "limb in your body in a direction it was never intended to go.\n", len);
							}
							slowPrint("You are dead. Game Over!\n\n", len);
						}
						//Print the player's health if they are not dead yet
						else
						{
							slowPrint("Your new health is " + p1.getHealth() + "\n\n", len);
						}
					}
					//The enemy's attack roll was not good enough to beat the player's AC
					else
					{
						slowPrint("The enemy ain't got nothing on your AC!\n\n", len);
					}
				}
			
			// Standard wraparound to get back to index 0, start the order over again
			if ((turn + 1) > order.length)
			{
				slowPrint ("\nTop of the order again.\n", len);
				turn = 0;
			}
			else
				turn++;
		}
		
		// Battle over. Return whether the player died.
		return p1.isDead ();
	}
	
	// Returns new text speed as that is all that is currently implemented
	// Change text speed
	// So far only functionality
	static int changeSettings (int current)
	{
		int [] speeds = {120, 90, 40, 0};
		int choice = current;
		String input;
		
		slowPrint ("\nWhat would you like to do?\n\n", speeds [choice]);
		slowPrint ("[Text (Sp)eed]   [Text (Si)ze]   [(B)ack]\n> ", 0);
		
		// Loop to ensure proper choice
		while (inputvalid (input = input ().toLowerCase ()))
		{
			if (input.equals ("sp"))
			{
				// Change text delay
				slowPrint ("\nOk, what speed do you want?\n", speeds [choice]);
				slowPrint ("[(S)low]   [[(M)edium]\n[(F)ast]   [(N)o Delay]\n[(B)ack]\n> ", 0);
				
				while (inputvalid (input))
				{
					input = input ().toLowerCase ();
					if (input.equals ("s") || input.equals ("slow"))
					{
						choice = 0;
						break;
					}
					else if (input.equals ("m") || input.equals ("medium"))
					{
						choice = 1;
						break;
					}
					else if (input.equals ("f") || input.equals ("fast"))
					{
						choice = 2;
						break;
					}
					else if (input.equals ("n") || input.equals ("no") || input.equals ("no delay"))
					{
						choice = 3;
						break;
					}
					else if (input.equals ("b") || input.equals ("back"))
						break;
					else
					{
						slowPrint ("\nNot a valid choice\n\n", speeds [choice]);
						slowPrint ("[(S)low]   [[(M)edium]\n[(F)ast]   [(N)o Delay]\n[(B)ack]\n> ", 0);
					}
				}
				
			}
			else if (input.equals ("si"))
			{
				// Change text size
				slowPrint ("\nNot implemented yet\n", 0);
			}
			else if (input.equals ("b") || input.equals ("back"))
				// Finish and return
				break;
			else
			{
				// Catch-all failsafe
				slowPrint ("\nNot a valid choice\n\n", speeds [choice]);
			}
			
			slowPrint ("\nWhat would you like to do?\n\n", speeds [choice]);
			slowPrint ("[Text (Sp)eed]   [Text (Si)ze]   [(B)ack]\n> ", 0);
		}
		
		
		return choice;
	}
	
	// Print str out char by char with a delay in milliseconds given by len
	// Also can print str without delay
	// Used also in order to facilitate outputting to the UI
	public static void slowPrint (String str, int len)
	{
		for (int i = 0; i < str.length (); i++)
		{
			System.out.print (str.charAt (i));
			
			try { TimeUnit.MILLISECONDS.sleep (len); }
			catch (InterruptedException e) { 
				System.out.println ("\nOops, Java messed up.\n"); 
				Thread.currentThread().interrupt();
			}
		}
	}
	
	// Methods for input
	// Gets input from the user, and returns it as a String
 	static String input ()
	{
 		
		BufferedReader br = new BufferedReader (new InputStreamReader (System.in));
		String ret = "";
		
		try { ret = br.readLine (); }
		catch (IOException e)
		{
			System.out.println ("\nOops, Java messed up.\n");
			e.printStackTrace ();
		}
		
		return ret;
	}
 	// Reads from file
 	// Gets input from a file, skipping a number of lines indicated by toskip
 	// Used by loadChar to read from save file
 	static String input (File f, int toskip) throws IOException
 	{
 		String ret = "";
 		
 		try (BufferedReader br = new BufferedReader (new FileReader (f)))
 		{
 			ret = br.readLine ();
 		
 			for (int i = 0; i < toskip; i++)
 				ret = br.readLine ();
 			br.close();
 		}
 		catch (Exception e)
 		{
			System.out.println ("\nOops, Java messed up.\n");
			e.printStackTrace ();
 		}

 		return ret;
 	}
 	// Checks if input is any exit keyword, even ones not listed for player options
 	// returns true if not
 	static Boolean inputvalid (String input)
 	{
 		if (input == null)
 			return false;
 		input = input.toLowerCase ();
 		return !(input.equals ("no") || input.equals ("nothing") || input.equals ("q") ||
 				input.equals ("quit") || input.equals ("cancel") || input.equals ("exit"));
 	}
}