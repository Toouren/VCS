package Commands;

import javafx.util.Pair;
import utility.IPacket;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import utility.*;

public class AddCommand implements ICommand {
    AddCommandPacket workingPack;
    public AddCommand(IPacket packet) {
        workingPack = (AddCommandPacket) packet;
    }

    @Override
    public Pair doCommand() throws FileNotFoundException, UnsupportedEncodingException {
        String repoName = workingPack.getRepoName();
        int result = reposWorker.createRepos(repoName);
        if (result == 0)
            return new Pair<>(new AnswerAddCommandPacket("ok", 100), null);
        else
            return new Pair<>(new AnswerAddCommandPacket("cannot create", 400), null);
    }
}
