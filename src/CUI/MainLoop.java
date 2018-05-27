package CUI;

import Client.VcsClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainLoop {
    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);
        CommandParser commandParser = new CommandParser();
        CommandExecuter commandExecuter = new CommandExecuter();
        VcsClient vcsClient = new VcsClient();
        while (true) {
            String commandString = in.nextLine();
            commandExecuter.executeCommand(commandParser.parseCommand(commandString), vcsClient);
        }
    }

}

class CommandExecuter{
    void executeCommand(HashMap<String, String> command, VcsClient vcsClient) throws IOException {
        if (command.get("name") != null) {
            switch (command.get("name")) {
                case "add":
                    vcsClient.doAddCommand(command.get("repoName"));
                    break;
                case "clone":
                    vcsClient.setWorkingLocalPath(command.get("path"));
                    vcsClient.setWorkingRepoName(command.get("repoName"));
                    vcsClient.doCloneCommand();
                    break;
                case "update":
                    vcsClient.doUpdateCommand();
                    break;
                case "commit":
                    vcsClient.doCommiteCommand();
                    break;
                case "revert":
                    vcsClient.doRevertCommand(command.get("version"));
                    break;
                case "setDir":
                    vcsClient.setWorkingLocalPath(command.get("path"));
                    vcsClient.setWorkingRepoName(vcsClient.getConnectedRepoName(command.get("path")));
                    break;
            }
        }
        else{
            System.out.println("Uncorrected command");
        }
    }
}

class CommandParser{
    HashMap<String, String> parseCommand(String commandString){
        HashMap<String, String> resultMap = new HashMap<String, String>();
        ArrayList<Pattern> patternList = new ArrayList<Pattern>();
        Pattern addCommandPattern = Pattern.compile("(?<name>add)( (?<repoName>.*))");
        patternList.add(addCommandPattern);
        Pattern cloneCommandPattern = Pattern.compile("((?<name>clone) )((?<path>.*) )(?<repoName>.*)(?<flag> \\.)?");
        patternList.add(cloneCommandPattern);
        Pattern updateCommandPattern = Pattern.compile("(?<name>update)");
        patternList.add(updateCommandPattern);
        Pattern commiteCommandPattern = Pattern.compile("(?<name>commit)");
        patternList.add(commiteCommandPattern);
        Pattern revertCommandPattern = Pattern.compile("((?<name>revert) )(?<version>.*)");
        patternList.add(revertCommandPattern);
        Pattern logCommandPattern = Pattern.compile("(?<name>log)");
        patternList.add(logCommandPattern);
        Pattern setLocalDirPattern = Pattern.compile("((?<name>setDir) )(?<path>.*)");
        patternList.add(setLocalDirPattern);

        for (Pattern aPatternList : patternList) {
            Matcher matcher = aPatternList.matcher(commandString);
            if (matcher.find()) {
                resultMap.put("name", matcher.group("name"));
                switch (matcher.group("name")) {
                    case "add": {
                        resultMap.put("repoName", matcher.group("repoName"));
                        break;
                    }
                    case "clone": {
                        resultMap.put("path", matcher.group("path"));
                        resultMap.put("repoName", matcher.group("repoName"));
                        resultMap.put("flag", matcher.group("flag"));
                        break;
                    }
                    case "revert": {
                        resultMap.put("version", matcher.group("version"));
                        break;
                    }
                    case "setDir":
                        resultMap.put("path", matcher.group("path"));
                        break;
                }
            }
        }
        return resultMap;
    }
}
