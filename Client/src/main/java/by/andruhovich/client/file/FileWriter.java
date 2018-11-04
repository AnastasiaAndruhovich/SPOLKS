package by.andruhovich.client.file;

import by.andruhovich.client.exception.file.FileActionTechnicalException;
import by.andruhovich.client.exception.file.FileNotFoundTechnicalException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileWriter {
    private File file;
    private FileOutputStream fileOutputStream;

    public void openFile(String filename, boolean clearFile) throws FileNotFoundTechnicalException {
        file = new File(filename);
        try {
            if (clearFile && file.exists()) {
                file.delete();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            fileOutputStream = new FileOutputStream(filename, true);
        } catch (FileNotFoundException e) {
            System.out.println("File " + filename + " not found!");
            throw new FileNotFoundTechnicalException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isFileExist() {
        return file.exists();
    }

    public long getFileLength() {
        return file.length();
    }

    public void writeDataInFile(byte[] data, int size) throws FileActionTechnicalException {
        try {
            fileOutputStream.write(data, 0, size);
        } catch (IOException e) {
            System.out.println("File " + file.getName() + " reading failure!");
            throw new FileActionTechnicalException(e);
        }

    }

    public void closeFile() {
        try {
            fileOutputStream.close();
        } catch (IOException e) {
            System.out.println("File " + file.getName() + " close failure!");
        }
    }
}
