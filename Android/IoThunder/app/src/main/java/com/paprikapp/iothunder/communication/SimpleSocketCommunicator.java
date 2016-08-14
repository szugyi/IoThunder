package com.paprikapp.iothunder.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import timber.log.Timber;

/**
 * ICommunicator implementation using only classes available in Java.
 * The communicator starts its own {@link Thread} in which it is waiting for data from the server,
 * and notifies the {@link OnDataReceivedListener} from this {@link Thread}.
 */
public class SimpleSocketCommunicator implements ICommunicator {
    private String address;
    private int port;

    private Thread thread;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private OnDataReceivedListener listener;

    @Override
    public void setEndpoint(String address, int port) {
        this.address = address;
        this.port = port;
        this.thread = new Thread(new ClientDataNodeRunnable());
        this.thread.start();
    }

    @Override
    public void setListener(OnDataReceivedListener listener) {
        this.listener = listener;
    }

    @Override
    public void sendData(String data) {
        Timber.v("sendData called");
        if (writer != null) {
            writer.println(data + "\r");
            Timber.v("Sent: " + data);

            if (listener != null) {
                listener.onDataSent();
            }
        } else {
            if (listener != null) {
                listener.onDataLost();
            }
        }
    }

    @Override
    public void disconnect() {
        Timber.v("Disconnecting from: " + address + ":" + port);
        cleanUp();
    }

    /**
     * Frees up resources and to close all the connections properly.
     */
    private synchronized void cleanUp() {
        Timber.v("Cleaning up: " + address + ":" + port);
        try {
            if (socket != null) {
                if (socket.isConnected()) {
                    socket.shutdownInput();
                    socket.shutdownOutput();
                }
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            socket = null;
        }

        try {
            if (reader != null) {
                reader.close();
                reader = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            reader = null;
        }

        if (writer != null) {
            writer.close();
            writer = null;
        }
    }

    /**
     * This {@link Runnable} implementation is used in a {@link Thread} to connect to the server
     * on the address and port specified in the communicator and to listen on the reader for incoming data.
     */
    class ClientDataNodeRunnable implements Runnable {
        private static final int TIMEOUT = 5000;
        public void run() {
            String input = null;
            try {
                Timber.v("Connecting to " + address + ":" + port);

                InetAddress serverAddress = InetAddress.getByName(address);
                SocketAddress socketAddress = new InetSocketAddress(serverAddress, port);

                socket = new Socket();
                socket.connect(socketAddress, TIMEOUT);
                writer = new PrintWriter(socket.getOutputStream(), true);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                Timber.v("Connected to " + address + ":" + port);
                if (listener != null) {
                    listener.onConnected();
                }

                while ((input = reader.readLine()) != null) {
                    Timber.v("Received: " + input);
                    if (listener != null) {
                        listener.onReceive(input);
                    }
                }
            } catch (Exception e) {
                Timber.v("Socket exception: " + e.toString());
            } finally {
                // close connection
                cleanUp();
            }

            Timber.v("Thread exit");
            Timber.v("input: " + ((input == null) ? "null" : input));
            if (listener != null) {
                listener.onDisconnected();
            }
        }
    }
}
