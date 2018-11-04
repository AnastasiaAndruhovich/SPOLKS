package by.andruhovich.server.command;

import by.andruhovich.server.type.CommandType;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CommandParser {
    private static final String REGEXP_SPACE = "\\s";
    private static final int PACKET_NUMBER_POSITION = 0;
    private static final int COMMAND_TYPE_POSITION = 1;
    private static final int DATA_START_POSITION = 2;

    public static int getPacketNumber(String data) {
        String packetNumber = splitBySpace(data)[PACKET_NUMBER_POSITION];
        return Integer.parseInt(packetNumber);
    }

    public static List<Integer> getACKPacketNumbers(String data) {
        String[] packetNumbers = splitBySpace(data);
        List<String> packetList = new LinkedList<>(Arrays.asList(packetNumbers));
        packetList.remove(0);
        packetList.remove(0);

        List<Integer> result = new LinkedList<>();
        for (String packetNumber : packetList) {
            result.add(Integer.parseInt(packetNumber));
        }
        return result;
    }

    public static CommandType getCommandType(String data) {
        String commandType = splitBySpace(data)[COMMAND_TYPE_POSITION];
        try {
            return CommandType.valueOf(commandType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CommandType.UNDEFINED_COMMAND;
        }
    }

    public static String getFilename(String data) {
        return splitBySpace(data)[DATA_START_POSITION];
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

    private static String[] splitBySpace(String data){
        return data.split(REGEXP_SPACE);
    }

}
