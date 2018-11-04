package by.andruhovich.server.socket;

import by.andruhovich.server.exception.socket.AcceptSocketTechnicalException;
import by.andruhovich.server.exception.socket.CreateSocketTechnicalException;
import by.andruhovich.server.exception.socket.ReceiveDataTechnicalException;
import by.andruhovich.server.exception.socket.SendDataTechnicalException;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPSocket {
    private ServerSocket serverSocket;
    private DataOutputStream outForFile;
    private BufferedReader in;
    private PrintWriter out;
    private boolean haveClient;

    private static final int BUFFER_SIZE = 1500;

    public TCPSocket(int port) throws CreateSocketTechnicalException {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReceiveBufferSize(BUFFER_SIZE);
            haveClient = false;

            System.out.println("Address: " + InetAddress.getLocalHost());
            System.out.println("Port: " + port);
        } catch (IOException e) {
            throw new CreateSocketTechnicalException("Creating server socket failure! Port: " + port);
        }
    }

    public Socket waitForClient() throws AcceptSocketTechnicalException {
        System.out.println("Waiting for client...");
        try {
            Socket clientSocket = serverSocket.accept();
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            outForFile = new DataOutputStream(clientSocket.getOutputStream());
            haveClient = true;

            System.out.println("Client connected with IP: " + clientSocket.getInetAddress());
            return clientSocket;
        } catch (IOException e) {
            throw new AcceptSocketTechnicalException("Accepting client socket failure!");
        }
    }

    public void sendData(String data) {
        out.flush();
        out.println(data);
    }

    public void sendData(byte[] data, int size) throws SendDataTechnicalException {
        try {
            outForFile.flush();
            outForFile.write(data, 0,size);
        } catch (IOException e) {
            throw new SendDataTechnicalException("Sending file failure");
        }
    }

    public String receiveData() throws ReceiveDataTechnicalException {
        StringBuilder data = new StringBuilder();
        String line;
        try {
            do {
                line = in.readLine();
                data.append(line);
            } while (in.ready());
            return data.toString();
        } catch (IOException e) {
            throw new ReceiveDataTechnicalException("Receiving data failure!");
        }
    }

    public boolean isHaveClient() {
        return haveClient;
    }

    public int getBufferSize() {
        return BUFFER_SIZE;
    }

    public void setNoClient() {
        haveClient = false;
    }

    public void closeSocket() {
        try {
            in.close();
            out.close();
            outForFile.close();
            serverSocket.close();

            System.out.println("Socket was closed");
        } catch (IOException e) {
            System.out.println("Closing socket failure!");
        }
    }
}
