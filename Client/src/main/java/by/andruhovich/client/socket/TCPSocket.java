package by.andruhovich.client.socket;

import by.andruhovich.client.exception.socket.CreateSocketTechnicalException;
import by.andruhovich.client.exception.socket.ReceiveDataTechnicalException;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class TCPSocket {
    private Socket clientSocket;
    private DataInputStream inBytes;
    private BufferedReader in;
    private PrintWriter out;

    private static final int BUFFER_SIZE = 1024;

    public TCPSocket(InetAddress ipServer, int port) throws CreateSocketTechnicalException {
        try {
            clientSocket = new Socket(ipServer, port);
            clientSocket.setSendBufferSize(BUFFER_SIZE);
            clientSocket.setReceiveBufferSize(BUFFER_SIZE * BUFFER_SIZE);
            if (clientSocket.isConnected()) {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                inBytes = new DataInputStream(clientSocket.getInputStream());

                System.out.println("Connection was successful!");
                System.out.println("Address: " + clientSocket.getInetAddress());
                System.out.println("Port: " + port);
            } else {
                throw new CreateSocketTechnicalException("Creating server socket failure! Attempt count is exceeded. Port: " + port + "\nIP: " + ipServer.getHostAddress());
            }
        } catch (IOException e) {
            throw new CreateSocketTechnicalException("Creating server socket failure! Attempt count is exceeded. Port: " + port + "\nIP: " + ipServer.getHostAddress());
        }
    }

    public void sendData(String data) {
        out.flush();
        out.println(data);
    }

    public String receiveStringData() throws ReceiveDataTechnicalException {
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

    public void receiveByteData(byte[] data, int size) throws ReceiveDataTechnicalException {
        try {
            inBytes.readFully(data, 0, size);
        } catch (IOException e) {
            throw new ReceiveDataTechnicalException("Receiving byte data failure!");
        }
    }

    public boolean isConnected() {
        return clientSocket.isConnected();
    }

    public void closeSocket() {
        try {
            in.close();
            out.close();
            inBytes.close();
            clientSocket.close();

            System.out.println("Socket was closed");
        } catch (IOException e) {
            System.out.println("Closing socket failure!");
        }
    }

    public InetAddress getInetAddress() {
        return clientSocket.getInetAddress();
    }

    public int getPort() {
        return clientSocket.getPort();
    }
}
