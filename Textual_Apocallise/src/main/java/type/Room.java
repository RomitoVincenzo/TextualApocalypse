/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package type;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/*abbiamo bisogno di una descrizione completa + oggetti stanza per la prima volta in 
cui entra in stanza e per comando guarda 
Quando il giocatore entrerà per la seconda volta nella stessa stanza verrà visualizzata
una descizione corta + oggetti presenti in stanza con evenutali stati
La descrizione corta può essere pensata come
print("Sei nella" + room.getname() + print oggetti in stanza );
*/

/*
* getListObject usato per la stampa degli oggetti in stanza.
* */
public class Room implements Serializable{

    private final int id;

    private String name;

    private String description;
    
    private boolean visible = true;

    private int south;

    private int north;

    private int east;

    private int west;
    
    private int up;

    private int down;
    
    private int visited; //count visite in stanza
        
    private final List<AdvObject> objects = new ArrayList();

    public Room(int id) {
        this.id = id;
    }

    public Room(int id, String name, String description,boolean visible) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.visible = visible;
        visited = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getSouth() {
        return south;
    }

    public void setSouth(int south) {
        this.south = south;
    }

    public int getNorth() {
        return north;
    }

    public void setNorth(int north) {
        this.north = north;
    }

    public int getEast() {
        return east;
    }

    public void setEast(int east) {
        this.east = east;
    }

    public int getWest() {
        return west;
    }

    public void setWest(int west) {
        this.west = west;
    }

    public void setUp(int up) {
        this.up = up;
    }

    public int getUp() {
        return up;
    }
    
    public void setDown(int down) {
        this.down = down;
    }

    public int getDown() {
        return down;
    }
    
    public void setVisited(int visited) {
        this.visited = visited;
    }

    public int getVisited() {
        return visited;
    }
    public List<AdvObject> getObjects() {
        return objects;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Room other = (Room) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }
    
    
    public boolean objectInRoom (AdvObject object) {
    	boolean flag = false;
    	for(AdvObject obj : getObjects()) {
    		if(obj.getId() == object.getId())
    			flag=true;
    		}
    	return flag;
    }
    
    public AdvObjectContainer objectContainer (AdvObject object) {
    	AdvObjectContainer container = null;
    	for(AdvObject obj : getObjects()) {
    		if(obj instanceof AdvObjectContainer) {
    		 	for(AdvObject obj2 : ((AdvObjectContainer) obj).getList()) {
    		 		if(obj2.getId() ==  object.getId()) {
    		 			container = (AdvObjectContainer) obj;
    		 		}
    		 	}
    		}
    	}
    	return container;
    }
    
    public List<AdvObject> getContainedObjects() {
    	List<AdvObject> list = new ArrayList();
    	for(AdvObject obj : getObjects()) {
    		if(obj instanceof AdvObjectContainer) {
    			for(AdvObject obj2 : ((AdvObjectContainer) obj).getList()) {
    				list.add(obj2);
    			}
    		}
    	}
    	return list;
    }
    
    public List<AdvObject> interactiveObjects() {
    	List<AdvObject> list = new ArrayList();
    	for(AdvObject obj : getObjects()) {
    		if(obj.isOpenable()==true || obj.isPickupable()==true || obj.isPushable()==true || obj.getSpecificState()!=null) {
    			list.add(obj);
    		}
    	     if(obj instanceof AdvObjectContainer && obj.isOpen()==true) {
    			for(AdvObject obj2 : ((AdvObjectContainer) obj).getList()) {
    	    		if(obj2.isOpenable()==true || obj2.isPickupable()==true || obj2.isPushable()==true || obj2.getSpecificState()!=null) {
    	    			list.add(obj2);
    	    		}
    			}
    		}
    	}
    	return list;
    }
    
    public AdvObject objectById(int Id)
    {
    	AdvObject ao = null;
    	for(AdvObject obj : getObjects()) {
    		if(obj.getId() == Id)
    			ao=obj;
    		}
    	return ao;
    } 
    
    public AdvObject objectByName(String Name)
    {
    	AdvObject ao = null;
    	for(AdvObject obj : getObjects()) {
    		if(obj.getName().equals(Name))
    			ao=obj;
    		}
    	return ao;
    }  
}
