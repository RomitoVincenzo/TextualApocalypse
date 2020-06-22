package parser;

import type.AdvObject;
import type.Command;

public class ParserOutput {

    private Command command;
    private AdvObject object;
    private AdvObject invObject;

    public ParserOutput(Command command, AdvObject object) {
        this.command = command;
        this.object = object;
    }

    public ParserOutput(Command command, AdvObject object, AdvObject invObejct) {
        this.command = command;
        this.object = object;
        this.invObject = invObejct;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public AdvObject getObject() {
        return object;
    }

    public void setObject(AdvObject object) {
        this.object = object;
    }

    public AdvObject getInvObject() {
        return invObject;
    }

    public void setInvObject(AdvObject invObject) {
        this.invObject = invObject;
    }
}
