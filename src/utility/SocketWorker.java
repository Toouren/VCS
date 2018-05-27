package utility;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;

public class SocketWorker {
    public static void sendResponse(Socket socket, IPacket response) throws IOException {
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
        byte[] data = Serializator.serializeClass(response);
        if (data != null) {
            outputStream.writeInt(data.length);
            outputStream.write(data);
            outputStream.flush();
        } else throw new NullPointerException();
    }

    public static IPacket getRequest(Socket socket) throws IOException {
        while (true) {
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            int length = inputStream.readInt();
            if (length > 0) {
                byte[] message = new byte[length];
                inputStream.readFully(message);
                return (IPacket) Serializator.deserializeClass(message);
            }
        }
    }

    public static File getZipFile(Socket socket, Long length) throws IOException {
        InputStream stream = socket.getInputStream();
        byte[] data = new byte[Math.toIntExact(length)];
        int count = stream.read(data);
        File zipFile = new File("tmp\\zipOnClient.zip");
        FileOutputStream fos = new FileOutputStream(zipFile.getAbsolutePath());
        fos.write(data);
        fos.flush();
        fos.close();
        return zipFile;
    }

    public static void sendZipFile(Socket socket, File file) throws IOException {
        if (file != null) {
            byte[] data = Files.readAllBytes(file.toPath());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.write(data);
            outputStream.flush();
        }
    }
}
