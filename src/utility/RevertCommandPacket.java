package utility;

public class RevertCommandPacket extends IPacket {

    String revertinVersion;
    String repoName;

    public RevertCommandPacket(){

    }
    public RevertCommandPacket(String revertingVersion, String repoName){
        commandName = "revert";
        this.repoName = repoName;
        this.revertinVersion = revertingVersion;
    }

    public String getRepoName() {
        return repoName;
    }

    public String getRevertinVersion() {
        return revertinVersion;
    }
}
