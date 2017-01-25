package com.example.andreivancea.rccarapp.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.andreivancea.rccarapp.exception.ConnectionException;
import com.example.andreivancea.rccarapp.util.Const;
import com.example.andreivancea.rccarapp.util.MessageConstants;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by andrei.vancea on 1/25/2017.
 */

public class BTConnection {

    public static final String TAG = "BTConnection";

    private Handler mHandler;
    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;

    private boolean connected = false;

    private ConnectedThread connection;

    public BTConnection(Handler handler) throws ConnectionException {
        this.mHandler = handler;
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            mHandler.sendEmptyMessage(MessageConstants.BL_NOT_AVAILABLE);
            throw new ConnectionException(Const.EXCEPTION_BT_NOT_AVAILABLE);
        }
    }

    public boolean isEnabled() {
        return btAdapter.isEnabled();
    }

    public boolean connectTo(String address) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            Log.e(TAG, "Invalid Address \"" + address + "\".");
            mHandler.sendEmptyMessage(MessageConstants.BL_INCORRECT_ADDRESS);
            return false;
        }
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        btSocket = createBluetoothSocket(device);

        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        }

        Log.d(TAG, "...Connecting...");
        try {
            btSocket.connect();
            Log.d(TAG, "Connected to " + device.getName());
            connected = true;
            connection = new ConnectedThread(btSocket);

        } catch (IOException e) {
            mHandler.sendEmptyMessage(MessageConstants.BL_SOCKET_FAILED);
            Log.e(TAG, "Connection to " + device.getName() + " failed: " + e.getMessage());
        }
        return true;
    }

    public void sendData(String data) {
        if (connected) {
            connection.write(data.getBytes());
        }
    }

    public void pause() {
        if (connected) {
            connection.flushOutputStream();
            connection.cancel();
        }
    }

    public void destroy() {
        connection = null;
        try {
            btSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close socket!");
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) {
        try {
            return device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Could not create Bluetooth Socket: " + e.getMessage());
        }
        return null;
    }

    private class ConnectedThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            OutputStream tmpOut = null;
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }
            mmOutStream = tmpOut;
        }

        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
                Log.d(TAG, "Sent " + bytes.toString() + "to device.");
//                // Share the sent message with the UI activity.
//                Message writtenMsg = mHandler.obtainMessage(
//                        MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
//                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        mHandler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                mHandler.sendMessage(writeErrorMsg);
            }
        }

        public void flushOutputStream() {
            try {
                mmOutStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Could not flush the stream: " + e.getMessage());
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}
