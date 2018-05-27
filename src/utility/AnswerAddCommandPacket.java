package utility;

public class AnswerAddCommandPacket extends IPacket{
    private String answer;
    private int answerCode;

    public AnswerAddCommandPacket(){
    }

    public AnswerAddCommandPacket(String answer, int answerCode){
        this.answer = answer;
        this.answerCode = answerCode;
    }

    public int getAnswerCode() {
        return answerCode;
    }

    public String getAnswer() {
        return answer;
    }

    @Override
    public String toString() {
        return answer + " : " + answerCode;
    }
}
