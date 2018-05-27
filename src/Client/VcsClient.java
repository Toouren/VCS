package Client;

import Zipper.zipWorker;
import utility.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VcsClient {
    private int serverPort;
    private Socket socket;
    private String address;
    private String workingRepoName;
    private String workingLocalPath;

    public VcsClient() throws IOException {
        serverPort = 9090;
        address = "127.0.0.1";
        socket = generateSocket(serverPort, address);
    }

    public String getWorkingLocalPath() {
        return workingLocalPath;
    }

    public String getWorkingRepoName() {
        return workingRepoName;
    }

    public void setWorkingLocalPath(String workingLocalPath) {
        this.workingLocalPath = workingLocalPath;
        System.out.println("Working local path setted" + " : " + this.workingLocalPath);
    }

    public void setWorkingRepoName(String workingRepoName) {
        this.workingRepoName = workingRepoName;
        System.out.println("Working repository setted" + " : " + this.workingRepoName);
    }

    private Socket generateSocket(int port, String ip) throws IOException {
        InetAddress ipAddress = InetAddress.getByName(ip);
        Socket socket = new Socket(ipAddress, port);
        return socket;
    }

    public void doAddCommand(String repoName) throws IOException {
        AddCommandPacket addCommandPacket = new AddCommandPacket(repoName);
        SocketWorker.sendResponse(socket, addCommandPacket);
        AnswerAddCommandPacket answerAddCommandPacket = (AnswerAddCommandPacket) SocketWorker.getRequest(socket);
        System.out.println(answerAddCommandPacket.getAnswerCode() + " : " + answerAddCommandPacket.getAnswer());
    }

    public void doRevertCommand(String requestinVersion) throws IOException {
        if (workingLocalPath != null && workingRepoName != null) {
            RevertCommandPacket revertCommandPacket = new RevertCommandPacket(requestinVersion, workingRepoName);
            SocketWorker.sendResponse(socket, revertCommandPacket);
            AnswerRevertCommandPacket revertRequest = (AnswerRevertCommandPacket) SocketWorker.getRequest(socket);
            ArrayList<File> versionFiles = zipWorker.unzippingFiles(SocketWorker.getZipFile(socket, revertRequest.getZipArchiveLength()));
            if (revertRequest.getAnserCode().equals("100")) {
                for (File file : versionFiles) {
                    FileOutputStream stream = new FileOutputStream(workingLocalPath + File.separator + file.getName());
                    try {
                        stream.write(Files.readAllBytes(file.toPath()));
                    } finally {
                        stream.close();
                    }
                    file.delete();
                }
                changeCfgFile(requestinVersion, new File(workingLocalPath + File.separator + "repository.cfg"));
                System.out.println(revertRequest.getAnserCode() + " : " + revertRequest.getAnswerMessage());
            }
        }
        else {
            System.out.println("407" + " : " + "do clone first");
        }
    }

    public String getConnectedRepoName(String path) throws IOException {
        File cfgFile = new File(workingLocalPath + File.separator + "repository.cfg");
        HashMap<String, String> cfgInfo = parseCfgFile(cfgFile);
        return cfgInfo.get("repoName");
    }

    public void doUpdateCommand() throws IOException{
        if (workingLocalPath != null && workingRepoName != null){
            File cfgFile = new File(workingLocalPath + File.separator + "repository.cfg");
            HashMap<String, String> cfgInfo = parseCfgFile(cfgFile);
            UpdateCommandPacket updateCommandPacket = new UpdateCommandPacket(cfgInfo.get("repoName"), cfgInfo.get("currentVersion"));
            SocketWorker.sendResponse(socket, updateCommandPacket);
            AnswerUpdateCommandPacket updateRequest = (AnswerUpdateCommandPacket) SocketWorker.getRequest(socket);
            ArrayList<File> versionFiles = zipWorker.unzippingFiles(SocketWorker.getZipFile(socket, updateRequest.getZipArchiveLength()));

            if (updateRequest.getAnswer().equals("101")){
                System.out.println("You have already had last version");
            }
            else {
                for(File file: Objects.requireNonNull(new File(workingLocalPath).listFiles()))
                    if (!file.getName().equals("repository.cfg"))
                        file.delete();

                for (File workingFile : versionFiles) {
                    File file = new File(workingLocalPath + File.separator + workingFile.getName());
                    FileOutputStream stream = new FileOutputStream(file.getAbsolutePath());
                    try {
                        stream.write(Files.readAllBytes(workingFile.toPath()));
                    } finally {
                        stream.close();
                    }
                    workingFile.delete();
                }
                changeCfgFile(updateRequest.getNewVersion(), cfgFile);
            }
            System.out.println("100" + " : " + "ok");
        }
        else{
            System.out.println("407" + " : " + "do clone first");
        }
    }

    private void changeCfgFile(String newVersion, File oldCfgFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(oldCfgFile.getAbsolutePath()));
        String cfgText;
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            cfgText = sb.toString();
        } finally {
            br.close();
        }

        cfgText = cfgText.replaceAll("currentVersion = (.*)", String.format("currentVersion = %s", newVersion));
        File newCfgFile = writeFile(oldCfgFile, cfgText);
    }

    private File writeFile(File file, String text) throws IOException {
        try(FileWriter writer = new FileWriter(file.getAbsolutePath(), false))
        {
            writer.write(text);
            writer.flush();
        }
        return file;
    }

    private HashMap<String, String> parseCfgFile(File cfgFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(cfgFile));
        String cfgFileText;
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            cfgFileText = sb.toString();
        } finally {
            br.close();
        }

        HashMap<String, String> result = new HashMap<String, String>();
        Pattern repoNamePattern = Pattern.compile("repoName = (.*);");
        Pattern versionPattern = Pattern.compile("currentVersion = (.*)");
        Matcher rnpMatcher = repoNamePattern.matcher(cfgFileText);
        Matcher vMatcher = versionPattern.matcher(cfgFileText);
        if (rnpMatcher.find()){
            result.put("repoName", rnpMatcher.group(1));
        }
        if (vMatcher.find()){
            result.put("currentVersion", vMatcher.group(1));
        }
        return result;
    }

    public void doCommiteCommand() throws IOException {
        if (workingRepoName != null && workingLocalPath != null) {
            File localDirFolder = new File(workingLocalPath);
            ArrayList<File> localDir = new ArrayList<>();
            for (File file : Objects.requireNonNull(new File(workingLocalPath).listFiles())) {
                if (!file.getName().equals("repository.cfg")) {
                    localDir.add(file);
                }
            }
            File zipArchive = zipWorker.zipFiles(localDir);
            CommitCommandPacket commitCommandPacket = new CommitCommandPacket(workingRepoName, zipArchive.length());
            SocketWorker.sendResponse(socket, commitCommandPacket);
            SocketWorker.sendZipFile(socket, zipArchive);
            AnswerCommiteCommandPacket answerCommiteCommandPacket = (AnswerCommiteCommandPacket) SocketWorker.getRequest(socket);
            changeCfgFile(answerCommiteCommandPacket.getNewVersion(), new File(localDirFolder + File.separator + "repository.cfg"));
            System.out.println(answerCommiteCommandPacket.getAnswerCode() + " : " + answerCommiteCommandPacket.getAnswer());
        }
        else {
            System.out.println("407" + " : " + "do clone first");
        }
    }

    public void doCloneCommand() throws IOException {
        CloneCommandPacket cloneCommandPacket = new CloneCommandPacket(workingRepoName, workingLocalPath);
        for(File file: Objects.requireNonNull(new File(cloneCommandPacket.getPath()).listFiles()))
            file.delete();

        SocketWorker.sendResponse(socket, cloneCommandPacket);
        AnswerCloneCommandPacket cloneRequest = (AnswerCloneCommandPacket) SocketWorker.getRequest(socket);
        if (cloneRequest.getAnswerCode().equals("100")) {
            File zipFile = SocketWorker.getZipFile(socket, cloneRequest.getZipArchiveLength());
            ArrayList<File> unzippingFiles = zipWorker.unzippingFiles(zipFile);
            for (File file : unzippingFiles) {
                byte[] content = Files.readAllBytes(file.toPath());
                FileOutputStream stream = new FileOutputStream(cloneCommandPacket.getPath().concat(file.getName()));
                try {
                    stream.write(content);
                } finally {
                    stream.close();
                }
                file.delete();
            }
        }
    }
}
