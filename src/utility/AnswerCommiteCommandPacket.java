package utility;

public class AnswerCommiteCommandPacket extends IPacket{
    private String answerCode;
    private String answer;
    private String newVersion;

    public AnswerCommiteCommandPacket(){

    }
    public AnswerCommiteCommandPacket(String answerCode, String answer, String newVersion){
        this.answer = answer;
        this.answerCode = answerCode;
        this.newVersion = newVersion;
    }

    public String getNewVersion() {
        return newVersion;
    }

    public String getAnswer() {
        return answer;
    }

    public String getAnswerCode() {
        return answerCode;
    }

    @Override
    public String toString() {
        return answerCode + " : " + answer;
    }
}
