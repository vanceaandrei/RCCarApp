package com.example.andreivancea.rccarapp.handlers;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.andreivancea.rccarapp.RCControllerActivity;
import com.example.andreivancea.rccarapp.bluetooth.BTConnection;
import com.example.andreivancea.rccarapp.util.MessageConstants;

import java.lang.ref.WeakReference;

/**
 * Created by andrei.vancea on 1/6/2017.
 */

public class MyHandler extends Handler {
    private final WeakReference<RCControllerActivity> mActivity;

    public MyHandler(RCControllerActivity activity) {
        mActivity = new WeakReference<>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
        RCControllerActivity activity = mActivity.get();
        if (activity != null) {
            switch (msg.what) {
                case MessageConstants.BL_NOT_AVAILABLE:
                    Log.d(BTConnection.TAG, "Bluetooth is not available. Exit");
                    Toast.makeText(activity.getBaseContext(), "Bluetooth is not available", Toast.LENGTH_SHORT).show();
                    activity.finish();
                    break;
                case MessageConstants.BL_INCORRECT_ADDRESS:
                    Log.d(BTConnection.TAG, "Incorrect MAC address");
                    Toast.makeText(activity.getBaseContext(), "Incorrect Bluetooth address", Toast.LENGTH_SHORT).show();
                    break;
                case MessageConstants.BL_SOCKET_FAILED:
                    Toast.makeText(activity.getBaseContext(), "Socket failed", Toast.LENGTH_SHORT).show();
                    //activity.finish();
                    break;
//                case MessageConstants.MESSAGE_WRITE:
//                    Toast.makeText(activity.getBaseContext(), "Socket failed", Toast.LENGTH_SHORT).show();
//                    //activity.finish();
//                    break;
                case MessageConstants.MESSAGE_TOAST:
                    Toast.makeText(activity.getBaseContext(), msg.getData().getString("toast"), Toast.LENGTH_SHORT).show();
                    //activity.finish();
                    break;
            }
        }
    }
}