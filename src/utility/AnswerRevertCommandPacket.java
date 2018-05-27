package utility;

public class AnswerRevertCommandPacket extends IPacket {

    private String anserCode;
    private String answerMessage;

    public AnswerRevertCommandPacket(){
    }

    public AnswerRevertCommandPacket(String answerCode, String answerMessage, long zipArchiveLength){
        this.zipArchiveLength = zipArchiveLength;
        this.anserCode = answerCode;
        this.answerMessage = answerMessage;
    }

    public String getAnserCode() {
        return anserCode;
    }

    public String getAnswerMessage() {
        return answerMessage;
    }
}
