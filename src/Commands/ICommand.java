package Commands;

import fileWorker.ReposWorker;
import javafx.util.Pair;
import utility.IPacket;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public interface ICommand {
    ReposWorker reposWorker = ReposWorker.getInstance();

    Pair<IPacket, File> doCommand() throws IOException, NoSuchAlgorithmException;
}
