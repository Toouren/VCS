package utility;

public class UpdateCommandPacket extends IPacket{

    private String repoName;
    private String userVersion;

    public UpdateCommandPacket(){
    }

    public UpdateCommandPacket(String repoName, String userVersion){
        commandName = "update";
        this.repoName = repoName;
        this.userVersion = userVersion;
    }

    public String getRepoName() {
        return repoName;
    }

    public String getUserVersion() {
        return userVersion;
    }
}
