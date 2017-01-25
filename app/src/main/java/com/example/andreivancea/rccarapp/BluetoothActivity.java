package com.example.andreivancea.rccarapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.andreivancea.rccarapp.util.Const;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BluetoothActivity extends AppCompatActivity {

    private static final String TAG = "BluetoothActivity";
    BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    Map<String, String> devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //enable bluetooth
        enableBluetooth();

//        setResult(Activity.RESULT_CANCELED);
        mNewDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.message);
        Set<BluetoothDevice> deviceSet = mBluetoothAdapter.getBondedDevices();
        devices = new HashMap<>();
        for (BluetoothDevice device : deviceSet) {
            devices.put(device.getName(), device.getAddress());
        }
        mNewDevicesArrayAdapter.addAll(devices.keySet());
        ListView pairedListView = (ListView) findViewById(R.id.list_paired);
        pairedListView.setAdapter(mNewDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private AdapterView.OnItemClickListener mDeviceClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBluetoothAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = devices.get(info);
            if (BluetoothAdapter.checkBluetoothAddress(address)) {
                Intent in = new Intent(getApplicationContext(), RCControllerActivity.class);
                in.putExtra("address", address);
                startActivity(in);
            } else {
                Toast.makeText(getApplicationContext(), "Incorrect Bluetooth address!", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());

                }

                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setTitle(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };

    private void enableBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, Const.EXCEPTION_BT_NOT_AVAILABLE);
            Toast.makeText(this, Const.EXCEPTION_BT_NOT_AVAILABLE, Toast.LENGTH_LONG).show();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
    }

}
