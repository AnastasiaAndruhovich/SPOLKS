package by.andruhovich.client.command;

import by.andruhovich.client.type.CommandType;

public class CommandParser {
    private static final String REGEXP_SPACE = "\\s";
    private static final int PACKET_NUMBER_POSITION = 0;
    private static final int COMMAND_TYPE_POSITION = 1;
    private static final int DATA_START_POSITION = 2;
    private static final int FILE_DATA_SIZE_POSITION = 2;
    private static final int FILE_DATA_START_POSITION = 3;
    private static final int PACKET_COUNT_POSITION = 3;

    public static int getPacketNumber(String data) {
        String packetNumber = splitBySpace(data)[PACKET_NUMBER_POSITION];
        return Integer.parseInt(packetNumber);
    }

    public static int getPortNumber(String data) {
        String port = splitBySpace(data)[DATA_START_POSITION];
        port = port.replaceAll("\0", "");
        return Integer.parseInt(port);
    }

    public static int getFileDataSize(String data) {
        String size = splitBySpace(data)[FILE_DATA_SIZE_POSITION];
        return Integer.parseInt(size);
    }

    public static float getDownloadSpeed(String data) {
        String downloadSpeed = splitBySpace(data)[DATA_START_POSITION];
        return Float.valueOf(downloadSpeed);
    }

    public static int getACKPacketNumber(String data) {
        String packetNumber = splitBySpace(data)[DATA_START_POSITION];
        return Integer.parseInt(packetNumber);
    }

    public static CommandType getCommandType(String data) {
        String commandType = splitBySpace(data)[COMMAND_TYPE_POSITION];
        try {
            commandType = commandType.replaceAll("\0", "");
            return CommandType.valueOf(commandType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CommandType.UNDEFINED_COMMAND;
        }
    }

    public static String getFilename(String data) {
        return splitBySpace(data)[DATA_START_POSITION];
    }

    public static int getPacketCount(String data) {
        String[] list = splitBySpace(data);
        return Integer.parseInt(list[PACKET_COUNT_POSITION]);
    }

    public static String getData(String data) {
        String result = data;
        int spacePos = 0;
        for (int i = 0; i < data.length(); i++) {
            if (data.charAt(i) == ' ') {
                spacePos++;
            }
            if (spacePos == DATA_START_POSITION) {
                result =  data.substring(i + 1);
                break;
            }
        }
        return result;
    }

    public static byte[] getFileData(byte[] data, int size) {
        byte[] result = new byte[size];
        int pos = 0;
        int i;
        for (i = 0; i < data.length && pos != FILE_DATA_START_POSITION; i++) {
            if (data[i] == ' ') {
                pos++;
            }
        }

        for (int j = 0; j < size && i < data.length; i++, j++) {
            result[j] = data[i];
        }
        return result;

    }

    private static String[] splitBySpace(String data){
        return data.split(REGEXP_SPACE);
    }
}
