package type;

import java.io.Serializable;

public enum CommandType implements Serializable{
    END, INVENTORY, NORD, SOUTH, EAST, WEST, OPEN, CLOSE, PUSH, PULL, WALK_TO, PICK_UP,
    TALK_TO, GIVE, USE, LOOK_AT, TURN_ON,TURN_OFF, UP, DOWN, POUR , SHOOT, SAVE, LOAD
}
