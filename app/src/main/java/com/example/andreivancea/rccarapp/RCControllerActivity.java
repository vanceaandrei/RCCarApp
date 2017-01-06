package com.example.andreivancea.rccarapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.andreivancea.rccarapp.bluetooth.BluetoothConnection;
import com.example.andreivancea.rccarapp.exception.ConnectionException;
import com.example.andreivancea.rccarapp.handlers.MyHandler;

public class RCControllerActivity extends AppCompatActivity {

    private static final String MESSAGE_FORWARD = "F";
    private static final String MESSAGE_LEFT = "L";
    private static final String MESSAGE_RIGHT = "R";
    private static final String MESSAGE_BACKWARD = "B";
    private static final String MESSAGE_STOP = "S";
    private BluetoothConnection bl = null;
    private final MyHandler mHandler = new MyHandler(this);

    String address;

    private ImageView forward;
    private ImageView left;
    private ImageView right;
    private ImageView backward;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rccontroller);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        setupImageViewListeners();

        Intent intent = getIntent();
        address = intent.getStringExtra("address");

        bl = new BluetoothConnection(this, mHandler);
    }

    @Override
    protected void onDestroy() {
        bl.BT_onPause();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bl.BT_onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bl.BT_Connect(address, false);
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
                    try {
                        bl.sendData(MESSAGE_FORWARD);
                    } catch (ConnectionException e) {
                        handleConnectionException(e.getMessage());
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        bl.sendData(MESSAGE_STOP);
                    } catch (ConnectionException e) {
                        handleConnectionException(e.getMessage());
                    }
                }
                return false;
            }
        });
        left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        bl.sendData(MESSAGE_LEFT);
                    } catch (ConnectionException e) {
                        handleConnectionException(e.getMessage());
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        bl.sendData(MESSAGE_STOP);
                    } catch (ConnectionException e) {
                        handleConnectionException(e.getMessage());
                    }
                }
                return false;
            }
        });
        right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        bl.sendData(MESSAGE_RIGHT);
                    } catch (ConnectionException e) {
                        handleConnectionException(e.getMessage());
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        bl.sendData(MESSAGE_STOP);
                    } catch (ConnectionException e) {
                        handleConnectionException(e.getMessage());
                    }
                }
                return false;
            }
        });
        backward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        bl.sendData(MESSAGE_BACKWARD);
                    } catch (ConnectionException e) {
                        handleConnectionException(e.getMessage());
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        bl.sendData(MESSAGE_STOP);
                    } catch (ConnectionException e) {
                        handleConnectionException(e.getMessage());
                    }
                }
                return false;
            }
        });

    }

    private void handleConnectionException(String message) {
        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                .findViewById(R.id.content_rccontroller)).getChildAt(0);
        Snackbar.make(viewGroup.getRootView(), message, Snackbar.LENGTH_SHORT).show();
        //finish();
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
}

