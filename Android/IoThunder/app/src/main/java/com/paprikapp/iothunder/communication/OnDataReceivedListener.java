package com.paprikapp.iothunder.communication;

/**
 * Interface for OnDataReceivedListener.
 * The ICommunicator uses this Listener to report the connection state, success or failure of data sending,
 * and to pass received data.
 *
 * See the System Design document in R3.4.3 for more information
 */
public interface OnDataReceivedListener {
    /**
     * Called when data has been received from the server.
     * @param data the data received.
     */
    void onReceive(String data);

    /**
     * Called when the client has successfully connected to the server.
     */
    void onConnected();

    /**
     * Called when the client is disconnected from the server.
     */
    void onDisconnected();

    /**
     * Called if the data sent in the {@link ICommunicator#sendData(String) ICommunicator.sendData}
     * function has been sent successfully to the server.
     */
    void onDataSent();

    /**
     * Called if the data sent in the {@link ICommunicator#sendData(String) ICommunicator.sendData}
     * function could not be sent to the server. The client can log this event, or retry to send the
     * data again.
     */
    void onDataLost();
}
