package utility;

public class CommitCommandPacket extends IPacket{
    private String repoName;

    public CommitCommandPacket(){
    }

    public CommitCommandPacket(String repoName, Long zipArchiveLength){
        commandName = "commite";
        this.zipArchiveLength = zipArchiveLength;
        this.repoName = repoName;
    }

    public String getRepoName() {
        return repoName;
    }
}
