package ch.bbw.zork;import java.util.ArrayList;import java.util.Random;import java.util.Stack;/** * Class Game - the main class of the "Zork" game. * * Author:  Michael Kolling * Version: 1.1 * Date:    March 2000 *  *  This class is the main class of the "Zork" application. Zork is a very *  simple, text based adventure game.  Users can walk around some scenery. *  That's all. It should really be extended to make it more interesting! *  *  To play this game, create an instance of this class and call the "play" *  routine. *  *  This main class creates and initialises all the others: it creates all *  rooms, creates the parser and starts the game.  It also evaluates the *  commands that the parser returns. */public class Game {		private Parser parser;	private Room currentRoom;	private Room outside, lab, tavern, gblock, office, garden;	private Backpack backpack = new Backpack();	private ArrayList<Ghost> ghosts = new ArrayList<>();	private int score;		public Room getGarden() {		return garden;	}	public void setGarden(Room garden) {		this.garden = garden;	}	private Item hammer, key, zauberumhang;	private ArrayList<Room> map;	private Stack<Room> previousRooms;	/**	 * Create the game and initialise its internal map.	 */	public Game() {				parser = new Parser(System.in);				// Create all the rooms and link their exits together.		outside = new Room("outside G block on Peninsula campus");		lab = new Room("lab, a lecture theatre in A block");		tavern = new Room("the Seahorse Tavern (the campus pub)");		gblock = new Room("the G Block");		office = new Room("the computing admin office");		garden = new Room("the garden with trees");		// initialise room exits		outside.setExits(garden, lab, gblock, tavern);		lab.setExits(null, null, null, outside);		tavern.setExits(null, outside, null, null);		gblock.setExits(outside, office, null, garden);		office.setExits(null, null, null, gblock);		garden.setExits(null, gblock, outside, null);		backpack.setMaxCarryWeight(100.0);		backpack.setCurrentCarryWeight(100.0);		currentRoom = outside; // start game outside		previousRooms = new Stack<>();				map = new ArrayList<>();		map.add(outside);		map.add(lab);		map.add(tavern);		map.add(gblock);		map.add(office);		map.add(garden);				hammer = new Item();		hammer.setName("Hammer");		hammer.setWeightKg(20);				key = new Item();		key.setName("Key");		key.setWeightKg(0.05);				zauberumhang = new Item();		zauberumhang.setName("Zauberumhang");		zauberumhang.setWeightKg(3);				lab.addItem(hammer);		lab.addItem(key);		garden.addItem(zauberumhang);				Ghost ghost1 = new Ghost(false, office);		Ghost ghost2 = new Ghost(true, garden);		Ghost ghost3 = new Ghost(true, gblock);		ghosts.add(ghost1);		ghosts.add(ghost2);		ghosts.add(ghost3);	}	/**	 *  Main play routine.  Loops until end of play.	 */	public void play() {		printWelcome();		// Enter the main command loop.  Here we repeatedly read commands and		// execute them until the game is over.		boolean finished = false;		while (!finished) {			Command command = parser.getCommand();			finished = processCommand(command);		}		System.out.println("Thank you for playing.  Good bye.");	}	/**	 * Print out the opening message for the player.	 */	private void printWelcome() {		System.out.println();		System.out.println("Welcome to Zork!");		System.out.println("Zork is a simple adventure game.");		System.out.println("Type 'help' if you need help.");		System.out.println();		System.out.println(currentRoom.longDescription());	}	/**	 * Given a command, process (that is: execute) the command.	 * If this command ends the game, true is returned, otherwise false is	 * returned.	 */	private boolean processCommand(Command command) {		if (command.isUnknown()) {			System.out.println("I don't know what you mean...");			return false;		}		String commandWord = command.getCommandWord();		if (commandWord.equals("help")) {			printHelp();		} else if (commandWord.equals("go")) {			if (currentRoom==outside && command.getSecondWord().equals("west")) {				if(this.backpack.getItem("Key") != null) {					score++;					goRoom(command);				} else {					System.out.println("You need the key to open the door.");				}			} else {				score++;				goRoom(command);			}						// Gewonnen?			if (currentRoom==tavern  && this.backpack.getItem("Key") != null) {				System.out.println("Sie sind in der Taverne und haben gewonnen!");				System.out.println("Du hast "+this.score+" mal den Raum gewechselt");				return true;			}						for (Ghost ghost : ghosts) {
				if (ghost.getRoom() == currentRoom) {
					if (ghost.isFriendly()) {
						System.out.println("A friendly ghost is in this room.");
					} else {
						if(this.backpack.getItem("Zauberumhang") != null) {
							System.out.println("There is a evil ghost in the room... But you are still alive because you have the magic cape.");
						}else {
							System.out.println("A evil ghost is in this room. You are Dead");
							return true;
						}
					}
				}
			}								} else if (commandWord.equals("get")) {			if (command.hasSecondWord()) {				Item item = this.currentRoom.getItem(command.getSecondWord());				if (item != null) {					if(item.getWeightKg() > this.backpack.getCurrentCarryWeight()) {						System.out.println("Your too weak to carry this at the moment, try putting some item/s down first");						this.currentRoom.addItem(item);					}					else {						this.backpack.setCurrentCarryWeight(this.backpack.getCurrentCarryWeight() - item.getWeightKg());						this.backpack.addItem(item);						this.currentRoom.removeItem(item);						System.out.println("Added "+item.getName()+" to backpack");						System.out.println("Your current carry weight ist:" + this.backpack.getCurrentCarryWeight());					}				} else {					System.out.println("This item is not in this room");				}				}		} else if (commandWord.equals("put")) {			if (command.hasSecondWord()) {				Item item = this.backpack.getItem(command.getSecondWord());				if (item != null) {					this.backpack.removeItem(item);					this.backpack.setCurrentCarryWeight(this.backpack.getCurrentCarryWeight() + item.getWeightKg());					this.currentRoom.addItem(item);					System.out.println("Removed "+item.getName()+" from backpack");					System.out.println("Your current carry weight ist:" + this.backpack.getCurrentCarryWeight());				} else {					System.out.println("You don't have this item in your backpack");				}				}		} else if (commandWord.equals("show")) {			// Gegenstände die im Rucksack sind zeigen.					} else if (commandWord.equals("back")) {			if (!previousRooms.isEmpty()) {				currentRoom = previousRooms.pop();				System.out.println(currentRoom.longDescription());			} else {				System.out.println("Don't know where to go :(");			}					} else if (commandWord.equals("quit")) {			if (command.hasSecondWord()) {				System.out.println("Quit what?");			} else {				return true; // signal that we want to quit			}		} else if (commandWord.equals("backpack")) {			if (backpack.getItems().size() == 0) {				System.out.println("Your backpack is empty");			} else {				System.out.println("All items: ");				for (Item item : backpack.getItems()) {					System.out.println(item.getName());				}			}		} else if (commandWord.equals("map")) {			System.out.println("All rooms: ");			for (Room room : this.map){				if (room == currentRoom) {					System.out.println("current room: "+room.getDescription());				} else {					System.out.println(room.getDescription());				}			}		} else if (commandWord.equals("ask")) {			for (Ghost ghost : ghosts) {				if (ghost.getRoom() == currentRoom) {					if (ghost.isFriendly()) {						System.out.println("The magic cape is in the garden.");					}					}			}		}		return false;	}	/*	 * implementations of user commands:	 */ 	/**	 * Print out some help information.	 * Here we print some stupid, cryptic message and a list of the 	 * command words.	 */	private void printHelp() {		System.out.println("You are lost. You are alone. You wander");		System.out.println("around at Monash Uni, Peninsula Campus.");		System.out.println();		System.out.println("Your command words are:");		System.out.println(parser.showCommands());	}	/** 	 * Try to go to one direction. If there is an exit, enter the new	 * room, otherwise print an error message.	 */	private void goRoom(Command command) {		// if there is no second word, we don't know where to go...		if (!command.hasSecondWord()) {			System.out.println("Go where?");		} else {						String direction = command.getSecondWord();				// Try to leave current room.			Room nextRoom = currentRoom.nextRoom(direction);				if (nextRoom == null)				System.out.println("There is no door!");			else {								Random random = new Random();								previousRooms.push(currentRoom);				currentRoom = nextRoom;				System.out.println(currentRoom.longDescription());							}		}	}}