package type;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AdvObject implements Serializable{

    private final int id;
    private String name;
    private String description;  
    private Set<String> alias;
    private boolean openable;
    private boolean pickupable;
    private boolean pushable;
    private boolean open;
    private boolean push;          
    private String specificState;   
    private String article;

    public AdvObject(int id) {
        this.id = id;
    }

    public AdvObject(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public AdvObject(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public AdvObject(int id, String name, String description, Set<String> alias,boolean openable,boolean pickupable,
    				boolean pushable,boolean open,boolean push,String specificState,String article) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.alias = alias;
        this.openable = openable;
        this.pickupable = pickupable;
        this.pushable = pushable;
        this.open = open;
        this.push = push;
        this.specificState = specificState;
        this.article = article;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isOpenable() {
        return openable;
    }

    public void setOpenable(boolean openable) {
        this.openable = openable;
    }

    public boolean isPickupable() {
        return pickupable;
    }

    public void setPickupable(boolean pickupable) {
        this.pickupable = pickupable;
    }

    public boolean isPushable() {
        return pushable;
    }

    public void setPushable(boolean pushable) {
        this.pushable = pushable;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isPush() {
        return push;
    }

    public void setPush(boolean push) {
        this.push = push;
    }

    public Set<String> getAlias() {
        return alias;
    }

    public void setAlias(Set<String> alias) {
        this.alias = alias;
    }
    
    public void setAlias(String[] alias) {
        this.alias = new HashSet(Arrays.asList(alias));
    }
    
    public void setSpecificState(String SpecificState) {
        this.specificState = SpecificState;
    }

    public String getSpecificState() {
        return specificState;
    }

    public int getId() {
        return id;
    }
    
    public String getArticle() {
        return article;
    }


    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.id;
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
        final AdvObject other = (AdvObject) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

}
