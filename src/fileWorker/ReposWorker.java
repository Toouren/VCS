package fileWorker;

import com.oracle.tools.packager.IOUtils;
import threadDispatcher.ThreadedTask;
import utility.FileClass;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReposWorker extends ThreadedTask {
    private static ReposWorker instance;


    private boolean isRecursive;
    private boolean workFlag;
    private File repos;
    private String repoFolder;
    private volatile byte[] resultHash = "".getBytes();
    private ReposWorker() {
        repoFolder = "C:\\Users\\Василий\\Desktop\\webServer\\repos";
        repos = new File(repoFolder);
        name = "ReposWorker";
        workFlag = true;
    }


    public static synchronized ReposWorker getInstance() {
        if (instance == null) {
            instance = new ReposWorker();
        }
        return instance;
    }

    public int createRepos(String repoName) throws FileNotFoundException, UnsupportedEncodingException {
        File folder = new File(repoFolder + File.separator + repoName);
        if (!folder.exists()) {
            folder.mkdir();
            String generalWay = repoFolder + File.separator + repoName + File.separator + "1.0";
            File generalDir = new File(generalWay);
            generalDir.mkdir();
            String repoConfing = repoFolder + File.separator + repoName + File.separator + "repository.cfg";
            PrintWriter writer = new PrintWriter(repoConfing, "UTF-8");
            writer.println(String.format("first_push_dir = %s;", generalWay));
            writer.println("first_push_flag = true;");
            writer.println("last_version = 1.0;");
            writer.println("versions = 1.0");
            writer.close();
            return 0;
        }
        else return 1;
    }

    public String getLastVersionNumber(String repoName) throws IOException {
        File folder = new File(repoFolder + File.separator + repoName);
        String cfgText = getConfigText(folder + File.separator + "repository.cfg");
        HashMap<String, String> cfgInfo = getConfigInfo(cfgText);
        return cfgInfo.get("lastVersionNumber");

    }

    public ArrayList<File> buildRequestingVersion(String repoName, String requestinVersion) throws IOException {
        File folder = new File(repoFolder + File.separator + repoName);
        String cfgText = getConfigText(folder + File.separator + "repository.cfg");
        HashMap<String, String> cfgInfo = getConfigInfo(cfgText);
        ArrayList<File> currentVersion = new ArrayList<File>();
        int i = 0;

        String requestedVersionList = cfgInfo.get("versionList");
        Pattern requestedVersionListPattern = Pattern.compile(String.format("(.*%s;)", requestinVersion));
        Matcher rvpMatcher = requestedVersionListPattern.matcher(requestedVersionList);
        if (rvpMatcher.find())
            requestedVersionList = rvpMatcher.group(1);
        if (!cfgInfo.get("firstPushFlag").equals("true")) {
            for (String version : requestedVersionList.split(";")) {
                File versionFolder = new File(repoFolder + File.separator + repoName + File.separator + version);
                File versionCfgFolder = new File(versionFolder.getAbsolutePath() + File.separator + "version.cfg");
                String[] versionChanges = getConfigText(versionCfgFolder.getAbsolutePath()).replaceAll("\r\n", "").split(";");
                for (String versionChange : versionChanges) {
                    if (!versionChange.equals("")) {
                        switch (versionChange.substring(0, 3)) {
                            case "del": {
                                String nameFile = versionChange.substring(4, versionChange.length());
                                for (File file : currentVersion) {
                                    if (file.getName().equals(nameFile)) {
                                        currentVersion.remove(file);
                                        break;
                                    }
                                }
                                break;
                            }
                            case "add": {
                                String nameFile = versionChange.substring(4, versionChange.length());
                                File addingFile = new File(versionFolder.getAbsolutePath() + File.separator + nameFile);
                                currentVersion.add(addingFile);
                                break;
                            }
                            case "chg": {
                                String nameFile = versionChange.substring(4, versionChange.length());
                                File changingFile = new File(versionFolder.getAbsolutePath() + File.separator + nameFile);
                                for (File file : currentVersion) {
                                    if (file.getName().equals(nameFile)) {
                                        currentVersion.remove(file);
                                        currentVersion.add(changingFile);
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        return new ArrayList<>(currentVersion);
    }

    private String getConfigText(String repoCfgFilePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(repoCfgFilePath));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }

    private HashMap<String, String> getConfigInfo(String cfgText){
        HashMap<String, String> result = new HashMap<String, String>();
        Pattern firstPushDirPattern = Pattern.compile("first_push_dir = (.*);");
        Pattern firstPushFlagPattern = Pattern.compile("first_push_flag = (.*);");
        Pattern versionListPattern = Pattern.compile("versions = (.*)");
        Pattern lastVersionNumber = Pattern.compile("last_version = (.*);");
        Matcher fpfMatcher = firstPushFlagPattern.matcher(cfgText);
        Matcher fpdMatcher = firstPushDirPattern.matcher(cfgText);
        Matcher vlMatcher = versionListPattern.matcher(cfgText);
        Matcher lvnMatcher = lastVersionNumber.matcher(cfgText);
        if (fpdMatcher.find()){
            result.put("firstPushDir", fpdMatcher.group(1));
        }
        if (fpfMatcher.find()){
            result.put("firstPushFlag", fpfMatcher.group(1));
        }
        if (vlMatcher.find()){
            result.put("versionList", vlMatcher.group(1));
        }
        if (lvnMatcher.find()){
            result.put("lastVersionNumber", lvnMatcher.group(1));
        }
        return result;
    }

    public String getVersionFolder(String repoName) throws IOException {
        File folder = new File(repoFolder + File.separator + repoName);
        String cfgText = getConfigText(folder + File.separator + "repository.cfg");
        HashMap<String, String> cfgInfo = getConfigInfo(cfgText);
        if (cfgInfo.get("firstPushFlag").equals("true")){
            changeFirstPushFlag(new File(folder.getAbsolutePath() + File.separator + "repository.cfg"));
            return cfgInfo.get("firstPushDir");
        }
        else {
            String currentVersion = cfgInfo.get("lastVersionNumber");
            String newVersionFolder = createNewVersion(folder.getAbsolutePath(), currentVersion);
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
            DecimalFormat df = (DecimalFormat)nf;
            df.applyPattern("#0.0");
            changeLastVersion(new File(folder + File.separator + "repository.cfg"), df.format(getNewVersionNumber(currentVersion)));
            changeVersionList(new File(folder + File.separator + "repository.cfg"), df.format(getNewVersionNumber(currentVersion)), cfgInfo.get("versionList"));
            return newVersionFolder;
        }
    }

    private String createNewVersion(String repoPath, String oldVersion){
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        DecimalFormat df = (DecimalFormat)nf;
        df.applyPattern("#0.0");
        File folder = new File(repoPath + File.separator + df.format(getNewVersionNumber(oldVersion)));
        folder.mkdir();
        return folder.getAbsolutePath();
    }

    private double getNewVersionNumber(String oldVersion){
        return Double.parseDouble(oldVersion) + 0.1;
    }

    private void changeLastVersion(File cfgFile, String lastVersion) throws IOException {
        String cfgText = getConfigText(cfgFile.getAbsolutePath());
        cfgText = cfgText.replaceAll("last_version = (.*);", String.format("last_version = %s;", lastVersion));
        PrintWriter writer = new PrintWriter(cfgFile, "UTF-8");
        writer.println(cfgText);
        writer.flush();
        writer.close();
    }

    private void changeVersionList(File cfgFile, String lastVersion, String versionList) throws IOException {
        String cfgText = getConfigText(cfgFile.getAbsolutePath());
        cfgText = cfgText.replaceAll("versions = " + versionList, "versions = " + versionList + ";" + lastVersion);
        PrintWriter writer = new PrintWriter(cfgFile, "UTF-8");
        writer.println(cfgText);
        writer.flush();
        writer.close();
    }

    private void changeFirstPushFlag(File cfgFile) throws IOException {
        String cfgText = getConfigText(cfgFile.getAbsolutePath());
        cfgText = cfgText.replaceAll("first_push_flag = true;", "first_push_flag = false;");
        PrintWriter writer = new PrintWriter(cfgFile, "UTF-8");
        writer.println(cfgText);
        writer.flush();
        writer.close();
    }

    @Override
    public void run() {

    }
}
