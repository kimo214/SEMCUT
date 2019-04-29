package com.products.ammar.sem_cut.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class BlutoothHelper extends Thread implements IBlutoothHelper {
    public static final String SERVICE_ID = "00001101-0000-1000-8000-00805F9B34FB"; //SPP UUID
    public static final String SERVICE_ADDRESS = "98:D3:32:31:82:7A"; // HC-05 BT ADDRESS
    private static final String TAG = "BlutoothHelper";
    public BluetoothAdapter btAdapter;
    public BluetoothDevice btDevice;
    public BluetoothSocket btSocket;
    private OnReceiveDataListener callback;
    private Context mContext;
    private byte[] mmBuffer; // mmBuffer store for the stream


    public BlutoothHelper(Context context) {
        mContext = context;
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Toast.makeText(context, "Bluetooth not available in this mobile", Toast.LENGTH_SHORT).show();
            return;
        }
        btDevice = btAdapter.getRemoteDevice(SERVICE_ADDRESS);

        if (!btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mContext.startActivity(enableIntent);
        } else {
            BlutoothHelper.ConnectThread connectThread = new BlutoothHelper.ConnectThread(btDevice);
            connectThread.start();
        }

    }

    @Override
    public void run() {
        // TODO: I believe you can edit 1024 value depends on your size of data received
        mmBuffer = new byte[10];
        int numBytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            if (btSocket == null) {
                continue;
            }
            try {
                // Read from the InputStream.
                // TODO: you can also pass numBytes to indicate the end of data instead of getting ? ? ? ? in rest of string
                numBytes = btSocket.getInputStream().read(mmBuffer);
                callback.receive(mmBuffer);
            } catch (IOException e) {
                Log.d(TAG, "Input stream was disconnected", e);
                break;
            }
        }
    }


    @Override
    public void send(byte[] message) {
        if (btSocket != null) {
            try {
                OutputStream out = btSocket.getOutputStream();
                out.write(message);
            } catch (IOException e) {
                Log.e(TAG, "send: error in send IOException", e);
            }
        }
    }

    @Override
    public void setOnReceiveData(OnReceiveDataListener callback) {
        this.callback = callback;
    }


    private class ConnectThread extends Thread {
        private final BluetoothSocket thisSocket;
        private final BluetoothDevice thisDevice;

        ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            thisDevice = device;

            try {
                tmp = thisDevice.createRfcommSocketToServiceRecord(UUID.fromString(SERVICE_ID));
            } catch (IOException e) {
                Log.e("TEST", "Can't connect to service");
            }
            thisSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            btAdapter.cancelDiscovery();

            try {
                thisSocket.connect();
                Log.d("TESTING", "Connected to shit");
            } catch (IOException connectException) {
                try {
                    thisSocket.close();
                } catch (IOException closeException) {
                    Log.e("TEST", "Can't close socket");
                }
                return;
            }

            btSocket = thisSocket;

        }

        public void cancel() {
            try {
                thisSocket.close();
            } catch (IOException e) {
                Log.e("TEST", "Can't close socket");
            }
        }
    }
}
