package com.paprikapp.iothunder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.paprikapp.iothunder.communication.AsyncSocketCommunicator;
import com.paprikapp.iothunder.communication.OnDataReceivedListener;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    AsyncSocketCommunicator communicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Timber.plant(new Timber.DebugTree());

        communicator = new AsyncSocketCommunicator();
        communicator.setListener(new OnDataReceivedListener() {
            @Override
            public void onReceive(String data) {

            }

            @Override
            public void onConnected() {
                communicator.sendData("L9999D10L");
            }

            @Override
            public void onDisconnected() {

            }

            @Override
            public void onDataSent() {

            }

            @Override
            public void onDataLost() {

            }
        });
        communicator.setEndpoint("192.168.0.21", 8081);
    }
}
