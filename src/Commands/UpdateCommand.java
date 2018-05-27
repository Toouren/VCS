package Commands;

import Zipper.zipWorker;
import javafx.util.Pair;
import utility.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class UpdateCommand implements ICommand {
    private UpdateCommandPacket workingPack;

    public UpdateCommand(IPacket packet) {
            workingPack = (UpdateCommandPacket) packet;
    }

    @Override
    public Pair doCommand() throws IOException {
        if (workingPack.getUserVersion().equals(reposWorker.getLastVersionNumber(workingPack.getRepoName()))){
            return new Pair<>(new AnswerUpdateCommandPacket("101", null, 0), null);
        }
        else {
            ArrayList<File> currentVersion = reposWorker.buildRequestingVersion(workingPack.getRepoName(), reposWorker.getLastVersionNumber(workingPack.getRepoName()));
            File zipArchive = zipWorker.zipFiles(currentVersion);
            return new Pair<>(new AnswerUpdateCommandPacket("100", reposWorker.getLastVersionNumber(workingPack.getRepoName()), zipArchive.length()), zipArchive);
        }
    }
}
