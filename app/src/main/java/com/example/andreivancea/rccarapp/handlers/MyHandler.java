package com.example.andreivancea.rccarapp.handlers;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.andreivancea.rccarapp.RCControllerActivity;
import com.example.andreivancea.rccarapp.bluetooth.BluetoothConnection;

import java.lang.ref.WeakReference;

/**
 * Created by andrei.vancea on 1/6/2017.
 */

public class MyHandler extends Handler {
    private final WeakReference<RCControllerActivity> mActivity;

    public MyHandler(RCControllerActivity activity) {
        mActivity = new WeakReference<RCControllerActivity>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
        RCControllerActivity activity = mActivity.get();
        if (activity != null) {
            switch (msg.what) {
                case BluetoothConnection.BL_NOT_AVAILABLE:
                    Log.d(BluetoothConnection.TAG, "Bluetooth is not available. Exit");
                    Toast.makeText(activity.getBaseContext(), "Bluetooth is not available", Toast.LENGTH_SHORT).show();
                    activity.finish();
                    break;
                case BluetoothConnection.BL_INCORRECT_ADDRESS:
                    Log.d(BluetoothConnection.TAG, "Incorrect MAC address");
                    Toast.makeText(activity.getBaseContext(), "Incorrect Bluetooth address", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothConnection.BL_REQUEST_ENABLE:
                    Log.d(BluetoothConnection.TAG, "Request Bluetooth Enable");
                    BluetoothAdapter.getDefaultAdapter();
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    activity.startActivityForResult(enableBtIntent, 1);
                    break;
                case BluetoothConnection.BL_SOCKET_FAILED:
                    Toast.makeText(activity.getBaseContext(), "Socket failed", Toast.LENGTH_SHORT).show();
                    //activity.finish();
                    break;
            }
        }
    }
}