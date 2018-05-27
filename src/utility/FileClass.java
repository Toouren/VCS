package utility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileClass {
    private String fileName;
    private byte[] fileContent;

    public FileClass(){

    }
    public FileClass(File file) throws IOException {
        fileName = file.getName();
        fileContent = Files.readAllBytes(file.toPath());
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public String getFileName() {
        return fileName;
    }
}
