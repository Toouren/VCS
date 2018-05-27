package utility;

public class AnswerUpdateCommandPacket extends IPacket {
    private String answer;
    private String newVersion;
    public AnswerUpdateCommandPacket(){

    }
    public AnswerUpdateCommandPacket(String answer, String newVersion, long zipArchiveLength){
        this.answer = answer;
        this.zipArchiveLength = zipArchiveLength;
        this.newVersion = newVersion;
    }

    public String getNewVersion() {
        return newVersion;
    }

    public String getAnswer() {
        return answer;
    }
}