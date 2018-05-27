package utility;

public abstract class IPacket {
    String commandName;
    long zipArchiveLength = 0;

    public long getZipArchiveLength() {
        return zipArchiveLength;
    }

    public String getCommandName() {
        return commandName;
    }
}
