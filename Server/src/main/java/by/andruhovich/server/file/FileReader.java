package by.andruhovich.server.file;

import by.andruhovich.server.exception.file.FileActionTechnicalException;
import by.andruhovich.server.exception.file.FileNotFoundTechnicalException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileReader {
    private File file;
    private FileInputStream fileInputStream;

    public void openFile(String filename) throws FileNotFoundTechnicalException {
        file = new File(filename);
        try {
            fileInputStream = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundTechnicalException("File " + filename + " not found!");
        }
    }

    public boolean isFileExist() {
        return file.exists();
    }

    public long getFileLength() {
        return file.length();
    }

    public void skipFile(long offset) throws FileActionTechnicalException {
        try {
            fileInputStream.skip(offset);
        } catch (IOException e) {
            throw new FileActionTechnicalException("Skip file failure!");
        }
    }

    public long calculatePacketCount(long offset, int packetSize) throws FileActionTechnicalException {
        try {
            long skippedBytes = fileInputStream.skip(offset);
            long fileLength = getFileLength();
            long rest = (fileLength - skippedBytes) % packetSize;
            long packetCount = (fileLength - skippedBytes) / packetSize;
            if (rest != 0) {
                packetCount++;
            }
            return packetCount;
        } catch (IOException e) {
            System.out.println("File " + file.getName() + " offset failure!");
            throw new FileActionTechnicalException(e);
        }
    }

    public int readDataFromFile(byte[] data, int size) throws FileActionTechnicalException {
        try {
            return fileInputStream.read(data, 0, size);
        } catch (IOException e) {
            System.out.println("File " + file.getName() + " reading failure!");
            throw new FileActionTechnicalException(e);
        }

    }

    public void closeFile() {
        try {
            fileInputStream.close();
        } catch (IOException e) {
            System.out.println("File " + file.getName() + " close failure!");
        }
    }
}
