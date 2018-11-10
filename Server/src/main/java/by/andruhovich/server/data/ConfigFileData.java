package by.andruhovich.server.data;

import by.andruhovich.server.exception.file.FileActionTechnicalException;
import by.andruhovich.server.exception.file.FileNotFoundTechnicalException;
import by.andruhovich.server.file.FileReader;

import java.util.Objects;

public class ConfigFileData extends FileData{
    private boolean isDownloading;
    private boolean isOldClient;
    private String lastFilename;
    private int lastOffset;
    private int handshakeCount;
    private long packetCount;
    private int packetNumber;
    private int sentPacketCount;
    private long startDownloadTime;
    private long sentBytesCount;
    private boolean isContinue;

    private FileReader fileReader;
    private FileData fileData;

    public ConfigFileData(){
        isDownloading = false;
        isOldClient = false;
        isContinue = false;
        lastOffset = 0;
        handshakeCount = 0;
        sentPacketCount = 0;
        sentBytesCount = 0;
        packetNumber = 0;
        packetCount = 0;

        fileReader = new FileReader();
    }

    public boolean isDownloading() {
        return isDownloading;
    }

    public void setDownloading(boolean downloading) {
        isDownloading = downloading;
    }

    public boolean isOldClient() {
        return isOldClient;
    }

    public void setOldClient(boolean oldClient) {
        isOldClient = oldClient;
    }

    public String getLastFilename() {
        return lastFilename;
    }

    public void setLastFilename(String lastFilename) {
        this.lastFilename = lastFilename;
    }

    public int getLastOffset() {
        return lastOffset;
    }

    public void addLastOffset(int lastOffset) {
        this.lastOffset += lastOffset;
    }

    public int getHandshakeCount() {
        return handshakeCount;
    }

    public void cleanHandshakeCount() {
        this.handshakeCount = 0;
    }

    public void addHandshakeCount() {
        this.handshakeCount++;
    }

    public long getPacketCount() {
        return packetCount;
    }

    public void addPacketCount() {
        this.packetCount++;
    }

    public int getPacketNumber() {
        return packetNumber;
    }

    public void addPacketNumber() {
        this.packetNumber++;
    }

    public int getSentPacketCount() {
        return sentPacketCount;
    }

    public void addSentPacketCount() {
        this.sentPacketCount++;
    }

    public long getStartDownloadTime() {
        return startDownloadTime;
    }

    public void setStartDownloadTime() {
        this.startDownloadTime = System.currentTimeMillis();
    }

    public long getSentBytesCount() {
        return sentBytesCount;
    }

    public void addSentBytesCount(long sentBytesCount) {
        this.sentBytesCount += sentBytesCount;
    }

    public void openFile() throws FileNotFoundTechnicalException {
        fileReader.openFile(lastFilename);
    }

    public void setPacketCount(long packetCount) {
        this.packetCount = packetCount;
    }

    public void calculatePacketCount(int dataSize) throws FileActionTechnicalException {
        packetCount = fileReader.calculatePacketCount(lastOffset, dataSize);
    }

    public boolean isSentPacketCountLessThanPacketCount() {
        return sentPacketCount < packetCount;
    }

    public int readDataFromFile(byte[] data, int readSize) throws FileActionTechnicalException {
        return fileReader.readDataFromFile(data, readSize);
    }

    public void setFileData(byte[] packet, int packetSize) {
        fileData = new FileData(packet, packetSize);
    }

    public float calculateDownloadSpeed() {
        long currentTime = System.currentTimeMillis();
        float timeDifference = (float)(currentTime - startDownloadTime) / 1000;
        return ((float)sentBytesCount / timeDifference);
    }

    public void cleanSentBytesCount() {
        sentBytesCount = 0;
    }

    public FileData getFileData() {
        return fileData;
    }

    public void setIsContinue(boolean isContinue) {
        this.isContinue = isContinue;
    }

    public boolean isContinue() {
        return isContinue;
    }

    public void clean() {
        isDownloading = false;
        lastOffset = 0;
        sentPacketCount = 0;
        packetCount = 0;
        sentBytesCount = 0;
        fileData = null;
        fileReader.closeFile();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ConfigFileData that = (ConfigFileData) o;
        return isDownloading == that.isDownloading &&
                lastOffset == that.lastOffset &&
                handshakeCount == that.handshakeCount &&
                packetCount == that.packetCount &&
                sentPacketCount == that.sentPacketCount &&
                startDownloadTime == that.startDownloadTime &&
                sentBytesCount == that.sentBytesCount &&
                Objects.equals(lastFilename, that.lastFilename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), isDownloading, lastFilename, lastOffset, handshakeCount, packetCount, sentPacketCount, startDownloadTime, sentBytesCount);
    }

    @Override
    public String toString() {
        return "ConfigFileData{" +
                "isDownloading=" + isDownloading +
                ", lastFilename='" + lastFilename + '\'' +
                ", lastOffset=" + lastOffset +
                ", handshakeCount=" + handshakeCount +
                ", packetCount=" + packetCount +
                ", sentPacketCount=" + sentPacketCount +
                ", startDownloadTime=" + startDownloadTime +
                ", sentBytesCount=" + sentBytesCount +
                '}';
    }
}
