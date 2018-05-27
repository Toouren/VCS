package utility;

public class CloneCommandPacket extends IPacket{
    private String repoName;
    private String path;

    public CloneCommandPacket(){
    }
    public CloneCommandPacket(String repoName, String path){
        commandName = "clone";
        this.repoName = repoName;
        this.path = path;
    }

    public String getRepoName() {
        return repoName;
    }

    public String getPath() {
        return path;
    }
}
