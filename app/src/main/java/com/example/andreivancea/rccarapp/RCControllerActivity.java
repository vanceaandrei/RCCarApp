package com.example.andreivancea.rccarapp;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.andreivancea.rccarapp.bluetooth.BTConnection;
import com.example.andreivancea.rccarapp.exception.ConnectionException;
import com.example.andreivancea.rccarapp.handlers.MyHandler;

public class RCControllerActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "RCControllerActivity";

    private static final String MESSAGE_FORWARD = "F";
    private static final String MESSAGE_LEFT = "L";
    private static final String MESSAGE_RIGHT = "R";
    private static final String MESSAGE_BACKWARD = "B";
    private static final String MESSAGE_STOP = "S";

    private static final int SENSOR_THRESHOLD = 3;

    private BTConnection bl = null;
    private final MyHandler mHandler = new MyHandler(this);

    String address;

    private ImageView forward;
    private ImageView left;
    private ImageView right;
    private ImageView backward;

    private Button retryConnectionBtn;

    private FloatingActionButton fab;

    private SensorManager mSensorManager;
    private Sensor mSensor = null;
    private boolean sensorControl = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rccontroller);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Activate sensors control
                if (!sensorControl) {
                    activateSensorControl();
                } else {
                    deactivateSensorControl();
                }

            }
        });

        retryConnectionBtn = (Button) findViewById(R.id.buttonReconnect);
        retryConnectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bl.connectTo(address);
            }
        });


        setupImageViewListeners();

        Intent intent = getIntent();
        address = intent.getStringExtra("address");

        try {
            bl = new BTConnection(mHandler);
        } catch (ConnectionException e) {
            Log.e(TAG, e.getMessage());
            handleConnectionException(e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        bl.destroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bl.pause();
        if (sensorControl) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bl.connectTo(address);
        if (sensorControl) {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void setupImageViewListeners() {
        forward = (ImageView) findViewById(R.id.image_forward);
        left = (ImageView) findViewById(R.id.image_left);
        right = (ImageView) findViewById(R.id.image_right);
        backward = (ImageView) findViewById(R.id.image_backward);

        forward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    bl.sendData(MESSAGE_FORWARD);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    bl.sendData(MESSAGE_STOP + MESSAGE_FORWARD);
                }
                return false;
            }
        });
        left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    bl.sendData(MESSAGE_LEFT);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    bl.sendData(MESSAGE_STOP + MESSAGE_LEFT);
                }
                return false;
            }
        });
        right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    bl.sendData(MESSAGE_RIGHT);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    bl.sendData(MESSAGE_STOP + MESSAGE_RIGHT);
                }
                return false;
            }
        });
        backward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    bl.sendData(MESSAGE_BACKWARD);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    bl.sendData(MESSAGE_STOP + MESSAGE_BACKWARD);
                }
                return false;
            }
        });

    }

    private void activateSensorControl() {

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if ((mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)) == null) {
            Toast.makeText(this, "Accelerometer not available!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Sensor control activated!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Sensor control activated!");
            //change fab icon
            fab.setImageDrawable(getResources().getDrawable(android.R.drawable.star_big_on, this.getApplicationContext().getTheme()));
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
            sensorControl = true;
        }
    }

    private void deactivateSensorControl() {
        fab.setImageDrawable(getResources().getDrawable(android.R.drawable.star_big_off, this.getApplicationContext().getTheme()));
        mSensorManager.unregisterListener(this);
        sensorControl = false;
        Toast.makeText(this, "Sensor control deactivated!", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Sensor control deactivated!");
    }

    private void handleConnectionException(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    String lastAction;

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (sensorControl) {
            float x = event.values[0]; //x
            float y = event.values[1]; //y

            String messageToSend = "";

            if (Math.abs(y) > SENSOR_THRESHOLD) {
                if (y < 0) {
                    //forward
                    messageToSend += MESSAGE_FORWARD;
                } else {
                    //backward
                    messageToSend += MESSAGE_BACKWARD;
                }
            }

            if (Math.abs(x) > SENSOR_THRESHOLD) {
                if (x < 0) {
                    //right
                    messageToSend += MESSAGE_RIGHT;
                } else {
                    //left
                    messageToSend += MESSAGE_LEFT;
                }
            }

            if (!messageToSend.equals(lastAction)) {
                bl.sendData(messageToSend);
                lastAction = messageToSend;
                Log.d(TAG, "Sending: " + messageToSend);
            }
            if (messageToSend.equals("") && !messageToSend.equals(lastAction)) {
                bl.sendData(MESSAGE_STOP);
                lastAction = messageToSend;
                Log.d(TAG, "Sending: STOP");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

