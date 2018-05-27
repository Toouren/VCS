package utility;

import Commands.AddCommand;

import java.io.Serializable;
import java.util.HashMap;

public class AddCommandPacket extends IPacket{
    private String repoName;

    public AddCommandPacket(){
    }

    public AddCommandPacket(String repoName) {
        commandName = "add";
        this.repoName = repoName;
    }

    public String getRepoName() {
        return repoName;
    }
}