package by.andruhovich.manager.file;

import java.io.*;

public class ProcessChecker {

    public static boolean isFileBusy(String filename) {
        File file = new File(filename);
        try {
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(1);
            return false;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return true;
        }
    }

}
