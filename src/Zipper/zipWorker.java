package Zipper;

import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class zipWorker {
    public static File zipFiles(ArrayList<File> zippingFiles) throws IOException {
        File versionArchive = new File("tmp\\versionarchive.zip");
        FileOutputStream fos = new FileOutputStream(versionArchive.getAbsolutePath());
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        for (File zippingFile : zippingFiles) {
            FileInputStream fis = new FileInputStream(zippingFile);
            ZipEntry zipEntry = new ZipEntry(zippingFile.getName());
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
        }
        zipOut.close();
        fos.close();
        return versionArchive;
    }

    public static ArrayList<File> unzippingFiles(File zipFile) throws IOException {
        byte[] buffer = new byte[1024];
        ArrayList<File> resultList = new ArrayList<>();
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile.getAbsolutePath()));
        ZipEntry zipEntry = zis.getNextEntry();
        while(zipEntry != null){
            String fileName = zipEntry.getName();
            File newFile = new File("tmp\\" + fileName);
            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            zipEntry = zis.getNextEntry();
            resultList.add(newFile);
        }
        zis.closeEntry();
        zis.close();
        return resultList;
    }
}
