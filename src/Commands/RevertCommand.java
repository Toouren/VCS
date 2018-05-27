package Commands;

import Zipper.zipWorker;
import javafx.util.Pair;
import utility.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RevertCommand implements ICommand {
    RevertCommandPacket workingPack;
    public RevertCommand(IPacket workingPack) {
        this.workingPack = (RevertCommandPacket)workingPack;
    }

    @Override
    public Pair doCommand() throws IOException {
        ArrayList<File> requestingVersion = reposWorker.buildRequestingVersion(workingPack.getRepoName(), workingPack.getRevertinVersion());
        File zipArchive = zipWorker.zipFiles(requestingVersion);
        return new Pair<>(new AnswerRevertCommandPacket("100", "ok", zipArchive.length()), zipArchive);
    }
}
