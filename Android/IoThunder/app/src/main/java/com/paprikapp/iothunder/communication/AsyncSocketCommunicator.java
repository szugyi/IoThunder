package com.paprikapp.iothunder.communication;

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.AsyncSocket;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.Util;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.ConnectCallback;
import com.koushikdutta.async.callback.DataCallback;

import timber.log.Timber;

/**
 * ICommunicator implementation using the AsyncSocket library from
 * <a href="https://github.com/koush/AndroidAsync">github.com/koush/AndroidSocket</a>.
 */
public class AsyncSocketCommunicator implements ICommunicator {
    private AsyncSocket socket;
    private OnDataReceivedListener listener;

    @Override
    public void setEndpoint(final String address, final int port) {
        Timber.i("Connecting to " + address + ":" + port);
        AsyncServer.getDefault().connectSocket(address, port, new ConnectCallback() {
            @Override
            public void onConnectCompleted(Exception ex, final AsyncSocket socket) {
                if (ex != null) throw new RuntimeException(ex);

                socket.setDataCallback(new DataCallback() {
                    @Override
                    public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
                        String data = new String(bb.getAllByteArray());

                        if (listener != null) {
                            listener.onReceive(data);
                        }
                    }
                });

                socket.setClosedCallback(new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception ex) {
                        if (ex != null) throw new RuntimeException(ex);
                        Timber.i("Successfully closed connection");
                        if (listener != null) {
                            listener.onDisconnected();
                        }
                    }
                });

                socket.setEndCallback(new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception ex) {
                        if (ex != null) throw new RuntimeException(ex);
                        Timber.i("Successfully end connection");
                    }
                });

                Timber.i("Connected to " + address + ":" + port);
                AsyncSocketCommunicator.this.socket = socket;
                if (listener != null) {
                    listener.onConnected();
                }
            }
        });
    }

    @Override
    public void setListener(OnDataReceivedListener listener) {
        this.listener = listener;
    }

    @Override
    public void sendData(String data) {
        if (socket == null) {
            return;
        }

        data += "\r\n";
        Util.writeAll(socket, data.getBytes(), new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if (listener != null) {
                    if (ex == null) {
                        Timber.v("Successfully wrote message");
                        listener.onDataSent();
                    } else {
                        Timber.v("Message lost");
                        listener.onDataLost();
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
    }

    @Override
    public synchronized void disconnect() {
        Timber.i("Disconnect");
        if (socket != null) {
            socket.end();
            socket.close();
        }
    }
}
