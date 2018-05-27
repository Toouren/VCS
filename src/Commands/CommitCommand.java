package Commands;

import Zipper.zipWorker;
import fileWorker.*;
import javafx.util.Pair;
import utility.*;

import java.io.*;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class CommitCommand implements ICommand {
    private CommitCommandPacket workingPack;
    private File zipArchive;
    private fileWorker flWorker = fileWorker.getInstance();
    public CommitCommand(IPacket commiteCommandPacket, File zipArchive) {
        workingPack = (CommitCommandPacket)commiteCommandPacket;
        this.zipArchive = zipArchive;
    }

    @Override
    public Pair doCommand(){
        try {
            ArrayList<File> currentRepoVersion = reposWorker.buildRequestingVersion(workingPack.getRepoName(), reposWorker.getLastVersionNumber(workingPack.getRepoName()));
            ArrayList<File> localDir = zipWorker.unzippingFiles(zipArchive);
            ArrayList<File> newVersionFolder = new ArrayList<>();
            HashMap<String, File> currentVersionFiles = new HashMap<>();
            for (File file : currentRepoVersion) {
                currentVersionFiles.put(file.getName(), file);
            }

            HashMap<String, File> localDirFiles = new HashMap<>();
            for (File file : localDir) {
                localDirFiles.put(file.getName(), file);
            }

            String newVersionPath = reposWorker.getVersionFolder(workingPack.getRepoName());

            File versionCfgFile = new File(newVersionPath + File.separator + "version.cfg");
            PrintWriter writer = new PrintWriter(versionCfgFile, "UTF-8");

            for (String workingFileName : currentVersionFiles.keySet()) {
                if (localDirFiles.get(workingFileName) != null) {

                    byte[] check1 = flWorker.execute(new Md5Executer(), currentVersionFiles.get(workingFileName).getAbsolutePath());
                    byte[] check2 = flWorker.execute(new Md5Executer(), localDirFiles.get(workingFileName).getAbsolutePath());
                    if (!Arrays.equals(check1,check2)) {
                        writer.write(String.format("chg %s;\n", workingFileName));
                        writer.flush();
                        newVersionFolder.add(localDirFiles.get(workingFileName));
                    }
                    else
                        localDir.get(localDir.indexOf(localDirFiles.get(workingFileName))).delete();
                    localDir.remove(localDirFiles.get(workingFileName));
                    localDirFiles.remove(workingFileName);

                } else {
                    writer.write(String.format("del %s;\n", workingFileName));
                    writer.flush();
                }
            }
            for (String newFileName : localDirFiles.keySet()) {
                writer.write(String.format("add %s;\n", newFileName));
                writer.flush();
                newVersionFolder.add(localDirFiles.get(newFileName));
            }


            for (File versionFile : newVersionFolder) {
                FileOutputStream fileOutputStream = new FileOutputStream(newVersionPath + File.separator + versionFile.getName());
                fileOutputStream.write(Files.readAllBytes(versionFile.toPath()));
                fileOutputStream.close();
                versionFile.delete();
            }
            writer.flush();
            writer.close();
            return new Pair<>(new AnswerCommiteCommandPacket("100", "ok", reposWorker.getLastVersionNumber(workingPack.getRepoName())), null);
        }
        catch (IOException | NoSuchAlgorithmException e){
            return new Pair<>(new AnswerCommiteCommandPacket("400", "error", null), null);
        }
    }
}
