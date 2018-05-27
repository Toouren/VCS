package Commands;

import javafx.util.Pair;
import utility.*;

import java.io.*;
import java.util.ArrayList;
import Zipper.*;

public class CloneCommand implements ICommand {
    private CloneCommandPacket workingPack;

    public CloneCommand(IPacket packet) {
        workingPack = (CloneCommandPacket) packet;
    }

    @Override
    public Pair<IPacket, File> doCommand() throws IOException {
        String currentVersionNumber = reposWorker.getLastVersionNumber(workingPack.getRepoName());
        ArrayList<File> currentVersion = reposWorker.buildRequestingVersion(workingPack.getRepoName(), currentVersionNumber);
        File cfgFile = createCfgFileClass(currentVersionNumber);
        currentVersion.add(cfgFile);
        File zipCurrentVersion = zipWorker.zipFiles(currentVersion);
        return new Pair<>(new AnswerCloneCommandPacket(zipCurrentVersion.length(), "ok", "100"), zipCurrentVersion);
    }

    private File createCfgFileClass(String versionNumber) throws IOException {
        File cfgFile = new File("tmp\\repository.cfg");
        PrintWriter writer = new PrintWriter(cfgFile.getName(), "UTF-8");
        writer.println(String.format("repoName = %s;", workingPack.getRepoName()));
        writer.println(String.format("currentVersion = %s", versionNumber));
        writer.close();
        return cfgFile;
    }
}
