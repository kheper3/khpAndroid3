package khp;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.format.Formatter;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import com.parse.ParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.String;
import java.util.List;
import static java.lang.Float.parseFloat;
import khp.R;

import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;

/**
 * Created by ros on 21/05/2015.
 */
public class KheperSettingsActivity extends Activity {
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_STATUS = "DEVICE_STATUS";
    private String mDeviceName;
    private String mDeviceAddress;
    private khp.BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;

    static boolean initParse =false;

    TextView nameAff;
    TextView passwordAff;
//    ImageView okVoyAff;
    ImageButton upBut;
    ImageView iv;
    boolean connected = false;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((khp.BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                finish();
            }
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (khp.BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {

            } else if (khp.BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                invalidateOptionsMenu();

                clearUI();
            } else if (khp.BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                mConnected = true;


                invalidateOptionsMenu();
            } else if (khp.BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

                String data = intent.getStringExtra(khp.BluetoothLeService.EXTRA_DATA);
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        nameAff = (TextView) this.findViewById(R.id.name);
        passwordAff = (TextView) this.findViewById(R.id.password);
        upBut = (ImageButton) this.findViewById(R.id.uploadBut);
/*
        passwordAff.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    loginClick();
                    handled = true;
                }
                return handled;
            }
        });

        */
        iv = (ImageView) this.findViewById(R.id.swipeView1);

        Intent gattServiceIntent = new Intent(this, khp.BluetoothLeService.class);
        Log.d("KheperSettingsActivity", "Try to bindService=" + bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE));
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if(initParse == false) {
            Parse.initialize(this, "ebmAvXTYQBwP4eFJyoy4qKmFaPa5nleQuVZGRMaZ", "AguluO1MbzzbYIEmjhFstkt9PZCaQgOSMxyD99ot ");
            initParse = true;
        }
        iv.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeRight() {
                final Intent intent = new Intent(KheperSettingsActivity.this, DeviceScanActivity.class);
                setContentView(R.layout.settings_layout);
                startActivity(intent);
            }

            public void onSwipeLeft() {
                final Intent intent = new Intent(KheperSettingsActivity.this, DeviceControlActivity.class);
                setContentView(R.layout.settings_layout);
                intent.putExtra(khp.KheperSettingsActivity.EXTRAS_DEVICE_NAME, mDeviceName);
                intent.putExtra(KheperSettingsActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);

                startActivity(intent);
            }


        });

    }

    private void clearUI() {
        //mDataField.setText(R.string.no_data);
    }

    /**
     * Detects left and right swipes across a view.
     */
    public class OnSwipeTouchListener implements View.OnTouchListener {

        private final GestureDetector gestureDetector;

        public OnSwipeTouchListener(Context context) {
            gestureDetector = new GestureDetector(context, new GestureListener());
        }

        public void onSwipeLeft() {
        }

        public void onSwipeRight() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_DISTANCE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float distanceX = e2.getX() - e1.getX();
                float distanceY = e2.getY() - e1.getY();
                if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (distanceX > 0)
                        onSwipeRight();
                    else
                        onSwipeLeft();
                    return true;
                }
                return false;
            }
        }
    }











    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(khp.BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(khp.BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(khp.BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        return intentFilter;
    }


    @Override

    protected void onStart() {
        super.onStart();

    }


    @Override
    protected void onResume() {
        super.onResume();


        }



    @Override
    protected void onPause() {
        super.onPause();
       if(mConnected) mBluetoothLeService.disconnect();
        unregisterReceiver(mGattUpdateReceiver);
        unbindService(mServiceConnection);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //this.unregisterReceiver(mGattUpdateReceiver);
        //unbindService(mServiceConnection);
        if (mBluetoothLeService != null) {
            mBluetoothLeService.close();
            mBluetoothLeService = null;
        }

    }
/*
    public void loginClick() {
        ParseUser.logOut();
        ParseUser.logInInBackground(nameAff.getText().toString(), passwordAff.getText().toString(),new LogInCallback() {
  @Override
            public void done(ParseUser user, ParseException e) {
                if (user != null) {

                    connected = true;

                    upBut.setBackground(getResources().getDrawable(R.drawable.uploadx));
                } else {
                    // Signup failed. Look at the ParseException to see what happened.

                    connected = false;
                    switch (e.getCode()) {
                        case ParseException.USERNAME_TAKEN:
                            Log.d("Testing", "Sorry, this username has already been taken.");
                            break;
                        case ParseException.USERNAME_MISSING:
                            Log.d("Testing", "Sorry, you must supply a username to register.");
                            break;
                        case ParseException.PASSWORD_MISSING:
                            Log.d("Testing", "Sorry, you must supply a password to register.");
                            break;
                        case ParseException.OBJECT_NOT_FOUND:
                            Log.d("Testing", "Sorry, those credentials were invalid.");
                            break;
                        case ParseException.CONNECTION_FAILED:
                            Log.d("Testing", "Internet connection was not found. Please see your connection settings.");
                            break;
                        default:
                            Log.d("Testing", e.getMessage());
                            break;
                    }
                }
            }
        });
    }


  */

    public void uploadClick(View view) {

        String label1, label2, label3;
        ParseUser user;

        ParseUser.logOut();
        upBut.setBackground(getResources().getDrawable(R.drawable.uploading));
        ParseUser.logInInBackground(nameAff.getText().toString(), passwordAff.getText().toString(),new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException pe) {
                if (user != null) {

                    connected = true;
//                 okVoyAff.setBackground(getResources().getDrawable(R.drawable.vert));

                    upBut.setBackground(getResources().getDrawable(R.drawable.uploading));
                } else {
                    // Signup failed. Look at the ParseException to see what happened.

                    connected = false;
                    switch (pe.getCode()) {
                        case ParseException.USERNAME_TAKEN:
                            Log.d("Testing", "Sorry, this username has already been taken.");
                            break;
                        case ParseException.USERNAME_MISSING:
                            Log.d("Testing", "Sorry, you must supply a username to register.");
                            break;
                        case ParseException.PASSWORD_MISSING:
                            Log.d("Testing", "Sorry, you must supply a password to register.");
                            break;
                        case ParseException.OBJECT_NOT_FOUND:
                            Log.d("Testing", "Sorry, those credentials were invalid.");
                            break;
                        case ParseException.CONNECTION_FAILED:
                            Log.d("Testing", "Internet connection was not found. Please see your connection settings.");
                            break;
                        default:
                            Log.d("Testing", pe.getMessage());
                            break;
                    }
                }


                SystemClock.sleep(2000);

                if (connected == false) {
                    Toast.makeText(KheperSettingsActivity.this, "You have to log in first", Toast.LENGTH_SHORT).show();
                    upBut.setBackground(getResources().getDrawable(R.drawable.signin));
                    return;
                }
                if (mConnected == false) {
                    Toast.makeText(KheperSettingsActivity.this, "You have to connect BT first", Toast.LENGTH_SHORT).show();
                    upBut.setBackground(getResources().getDrawable(R.drawable.signin));
                    return;
                }


                try {
                    user = ParseUser.getCurrentUser().fetch();
                } catch (ParseException pp) {
                    Toast.makeText(KheperSettingsActivity.this, "You have to log in first", Toast.LENGTH_SHORT).show();

                    return;
                }


// curve #1


                mBluetoothLeService.WriteValue("P9933333 ;");
                SystemClock.sleep(2000);

                String st = user.getString("stator");
                int stator = parseInt(st) * 100;

                mBluetoothLeService.WriteValue("E" + user.getString("descrid1") + ";");

                JSONArray Curve = user.getJSONArray("Curve1");

                for (int i = 0; i < Curve.length(); i++) {
                    try {
                        String s = Curve.getString(i);
                        int d = s.lastIndexOf(",") + 1;
                        int p = s.lastIndexOf(".");
                        int e = s.lastIndexOf("]");
                        String dec = s.substring(p + 1, e);
                        if (dec.length() == 1) dec = dec + "0";
                        int v = stator - (parseInt(s.substring(d, p)) * 100 + parseInt(dec));
                        if (v < 0) v = 0;
                        String com = String.format("P%02d%05d;", i, v);
                        mBluetoothLeService.WriteValue(com);
                        Log.d("szer", com);
                    } catch (JSONException pp) {

                    }
                }
                mBluetoothLeService.WriteValue("w1;");
                SystemClock.sleep(2500);


                mBluetoothLeService.WriteValue("E" + user.getString("descrid2") + ";");

                Curve = user.getJSONArray("Curve2");

                for (int i = 0; i < Curve.length(); i++) {
                    try {
                        String s = Curve.getString(i);
                        int d = s.lastIndexOf(",") + 1;
                        int p = s.lastIndexOf(".");
                        int e = s.lastIndexOf("]");
                        String dec = s.substring(p + 1, e);
                        if (dec.length() == 1) dec = dec + "0";
                        int v = stator - (parseInt(s.substring(d, p)) * 100 + parseInt(dec));
                        if (v < 0) v = 0;
                        String com = String.format("P%02d%05d;", i, v);
                        mBluetoothLeService.WriteValue(com);
                        Log.d("szer", com);
                    } catch (JSONException pp) {

                    }
                }
                mBluetoothLeService.WriteValue("w2;");
                SystemClock.sleep(2500);

                mBluetoothLeService.WriteValue("E" + user.getString("descrid3") + ";");
                Curve = user.getJSONArray("Curve3");

                for (int i = 0; i < Curve.length(); i++) {
                    try {
                        String s = Curve.getString(i);
                        int d = s.lastIndexOf(",") + 1;
                        int p = s.lastIndexOf(".");
                        int e = s.lastIndexOf("]");
                        String dec = s.substring(p + 1, e);
                        if (dec.length() == 1) dec = dec + "0";
                        int v = stator - (parseInt(s.substring(d, p)) * 100 + parseInt(dec));
                        if (v < 0) v = 0;
                        String com = String.format("P%02d%05d;", i, v);
                        mBluetoothLeService.WriteValue(com);
                        Log.d("szer", com);
                    } catch (JSONException pp) {

                    }
                }
                mBluetoothLeService.WriteValue("w3;");

                upBut.setBackground(getResources().getDrawable(R.drawable.done));
                Toast.makeText(KheperSettingsActivity.this, "Curve #1 :  " + user.getString("descrid1") + " uploaded", Toast.LENGTH_LONG).show();
                Toast.makeText(KheperSettingsActivity.this, "Curve #2 :  " + user.getString("descrid2") + " uploaded", Toast.LENGTH_LONG).show();
                Toast.makeText(KheperSettingsActivity.this, "Curve #3 :  " + user.getString("descrid3") + " uploaded", Toast.LENGTH_LONG).show();


            }
        });
        }
}