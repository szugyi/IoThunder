package com.paprikapp.iothunder.communication;

/**
 * ICommunicator interface
 * See the System Design document in R3.4.3 for more information
 */
public interface ICommunicator {
    /**
     * Set the server address to which the client can send the data.
     * @param address The url of the server.
     * @param port The open port of the server on which it is listening.
     */
    void setEndpoint(String address, int port);

    /**
     * Sets a listener to receive state changes and information about receiving/sending data from/to the server.
     * @param listener The listener to be notified.
     */
    void setListener(OnDataReceivedListener listener);

    /**
     * Send data to the server.
     * @param data The data to be sent.
     */
    void sendData(String data);

    /**
     * Disconnect from the server and free up used resources.
     */
    void disconnect();
}
