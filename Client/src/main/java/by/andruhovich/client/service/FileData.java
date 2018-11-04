package by.andruhovich.client.service;

import java.util.Arrays;
import java.util.Objects;

public class FileData {
    private byte[] packet;
    private int packetSize;

    public FileData(byte[] packet, int packetSize) {
        this.packet = packet;
        this.packetSize = packetSize;
    }

    public byte[] getPacket() {
        return packet;
    }

    public void setPacket(byte[] packet) {
        this.packet = packet;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public void setPacketSize(int packetSize) {
        this.packetSize = packetSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileData fileData = (FileData) o;
        return packetSize == fileData.packetSize &&
                Arrays.equals(packet, fileData.packet);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(packetSize);
        result = 31 * result + Arrays.hashCode(packet);
        return result;
    }

    @Override
    public String toString() {
        return "FileData{" +
                "packet=" + Arrays.toString(packet) +
                ", packetSize=" + packetSize +
                '}';
    }
}
