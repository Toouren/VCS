package threadDispatcher;

import CommandFactory.factory;
import Commands.ICommand;
import javafx.util.Pair;
import utility.*;
import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;


public class clientWorker extends ThreadedTask {

    private Socket client;
    private InputStream in;
    private OutputStream out;

    public clientWorker(Socket sock) throws IOException {
        client = sock;
        name = "ClientWorker";
        out = client.getOutputStream();
        in = client.getInputStream();
    }

    private void endWork() throws IOException {
        out.close();
        in.close();
        client.close();
        finish();
    }

    @Override
    public void run() {
        while (true) {
            try {
                IPacket messagePacket = SocketWorker.getRequest(client);
                File zipArchive = SocketWorker.getZipFile(client, messagePacket.getZipArchiveLength());
                ICommand command = factory.createCommand(messagePacket, zipArchive);
                assert command != null;
                Pair<IPacket, File> pair = command.doCommand();
                SocketWorker.sendResponse(client, pair.getKey());
                SocketWorker.sendZipFile(client, pair.getValue());
            } catch (IOException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }
}
