package com.example.andreivancea.rccarapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

    public static final String BROADCAST = "com.example.andreivancea.rccarapp.android.action.broadcast";

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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_search);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                    Log.d(TAG, "cancelDiscovery");
                }
                Log.d(TAG, "startDiscovery");
                mBluetoothAdapter.startDiscovery();
            }
        });

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
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
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
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Toast.makeText(context, "Scanning...", Toast.LENGTH_SHORT).show();
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
                String derp = device.getName() + " - " + device.getAddress();
                Toast.makeText(getApplicationContext(), derp, Toast.LENGTH_LONG).show();
                Log.d(TAG, "Found Device:" + derp);
                mNewDevicesArrayAdapter.add(derp);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "Bluetooth scan finished.");
                Toast.makeText(context, "Scan finished.", Toast.LENGTH_SHORT).show();
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
