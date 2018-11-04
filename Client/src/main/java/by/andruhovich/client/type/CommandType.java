package by.andruhovich.client.type;

public enum CommandType {
    HANDSHAKE, ACK, TIME, ECHO, DOWNLOAD, EXIT, UNDEFINED_COMMAND, FILE_NOT_FOUND, START_DOWNLOAD, END_DOWNLOAD,
    CONTINUE_DOWNLOAD, NO_CONTINUE_DOWNLOAD, RESEND_PACKETS, NO_PACKETS_FOR_RESEND
}
