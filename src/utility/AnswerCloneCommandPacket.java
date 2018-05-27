package utility;

import java.io.File;

public class AnswerCloneCommandPacket extends IPacket {

    private String answer;
    private String answerCode;

    public AnswerCloneCommandPacket(){

    }
    public AnswerCloneCommandPacket(long zipArchiveLength, String answer, String answerCode){
        this.zipArchiveLength = zipArchiveLength;
        this.answer = answer;
        this.answerCode = answerCode;
    }

    public String getAnswerCode() {
        return answerCode;
    }

    public String getAnswer() {
        return answer;
    }

    public long getZipArchiveLength() {
        return zipArchiveLength;
    }
}
