package CommandFactory;

import Commands.*;
import utility.IPacket;

import java.io.File;

public class factory {

    public static ICommand createCommand(IPacket messagePacket, File zipArchive) {
        String command = messagePacket.getCommandName();
        switch (command) {
            case "add":
                return new AddCommand(messagePacket);
            case "clone":
                return new CloneCommand(messagePacket);
            case "update":
                return new UpdateCommand(messagePacket);
            case "commite":
                return new CommitCommand(messagePacket, zipArchive);
            case "revert":
                return new RevertCommand(messagePacket);
            case "log":
                return new CloneCommand(messagePacket);
            default:
                return null;
        }
    }
}
