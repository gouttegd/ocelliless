package org.incenp.obofoundry.ocelliless;

import java.util.ArrayList;
import java.util.List;

import org.obolibrary.robot.Command;
import org.obolibrary.robot.ICommandProvider;

public class RobotProvider implements ICommandProvider {

    public List<Command> getCommands() {
        ArrayList<Command> cmds = new ArrayList<Command>();

        cmds.add(new RobotCommand());

        return cmds;
    }

}
