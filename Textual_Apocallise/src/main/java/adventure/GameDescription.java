/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adventure;

import parser.ParserOutput;
import type.AdvObject;
import type.Command;
import type.Room;
import type.Inventory;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class GameDescription implements Serializable{

    private final List<Room> rooms = new ArrayList();
    private final List<Command> commands = new ArrayList();
    private final Inventory inventory = new Inventory();
    private Room currentRoom;
    private boolean dead = false;
    
    public List<Room> getRooms() {
        return rooms;
    }

    public List<Command> getCommands() {
        return commands;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public abstract void init() throws Exception;

    public abstract void nextMove(ParserOutput p, PrintStream out);
    
    public Room roomById (int id) {
    	Room room_found = null;
    	for(Room room : getRooms()) {
    		if(room.getId()==id)
    			room_found = room;
    		}
    	return room_found;
    	
    }
    
    public boolean objectInInventory (AdvObject object) {
    	boolean flag = false;
    	for(AdvObject obj : getInventory().getList()) {
    		if(obj.getId() == object.getId())
    			flag=true;
    		}
    	return flag;
    	
    }
   
    public abstract void prologue();
    
    public abstract void menu();
    
    public abstract void firstScreen();
    
    public abstract void loadGame();
    
    public abstract void instructions();
    
    public abstract void formattedString(String input);
    
    public boolean isDead() {
    	return this.dead;
    }
    
    public void setDead(boolean dead) {
    	 this.dead=dead;
    }

}
