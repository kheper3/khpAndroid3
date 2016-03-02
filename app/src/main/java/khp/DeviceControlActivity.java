/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package khp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothClass.Device;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import khp.R;
import khp.KheperSettingsActivity;

import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;


import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;

import java.util.ArrayList;
import java.util.List;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class  DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_STATUS = "DEVICE_STATUS";

 //   private TextView mDataField;

    private String mDeviceName ;
    private String mDeviceAddress ;
    private khp.BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 3000;
    private Handler mHandler;
    List<BluetoothDevice> deviceList;

    LocationManager mlocManager;
    LocationListener mlocListener;

    TextView tpmAff;
    TextView retardAff;
    TextView noCAff;
    TextView labelAff;
    ImageButton bt1, bt2, bt3;
    ImageView iv;
    Drawable im0;
    float xc, yc;

    static String com, come;
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((khp.BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            mBluetoothLeService.connect(mDeviceAddress);
            Log.e(TAG, "mBluetoothLeService is okay"); // Automatically connects to the device upon successful start-up initialization.

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
        public void onReceive(Context context, Intent intent) { final String action = intent.getAction();

            if (khp.BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {

            	Log.e(TAG, "Only gatt, just wait");
            } else if (khp.BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                invalidateOptionsMenu();

                clearUI();
            }else if(khp.BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action))
            {
            	mConnected = true;
                ShowDialog(); //
            	Log.e(TAG, "In what we need");
            	invalidateOptionsMenu();
            }else if (khp.BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
            	Log.e(TAG, "RECV DATA");
            	String data = intent.getStringExtra(khp.BluetoothLeService.EXTRA_DATA);
// reconstitution du message issu du Kheper3 (terminé par LF \r)
            	if (data != null)
                {
                   com = com.concat(data);
                    int l = com.length();
                    int n = com.indexOf("\r");
                    if(n != -1) {
                        if (n == (l - 1)) {
                            come = com;
                            com = new String();
                        }
                        if (l > n+1) {
                            come = com.substring(0, n + 1);
                            com = com.substring(n + 1, l );
                        }
//analyse et exécution du message
                                comEx();
                    }

                }


            }
        }
    };
//////////////////////////////////////
//
// analyse et exécution du message
//
///////////////////////////////////////
    void comEx()
    {
        int l;
        char type;
        float rf;
        int ti;
        double tif;
        int rm;
        float rmf;
        String tpm;
        String ret;
        String noC;
        String lb;
        l = come.length();
        type = come.charAt(0);
//un message valide commence par 'P'
        if( type == 'P') {
// 1er champ => vitesse du moteur (rpm)
//
            tpm = come.substring(1, 6);
            rm = parseInt(tpm);
            tpm = valueOf(rm);
            tpm = tpm.concat(" rpm");
//affiché dans le champ correspondant de l'écran
            tpmAff.setText(tpm);

// 2ème champ  => retard en 1/100 de degré
//
            ret = come.substring(7, 12);
            ti = parseInt(ret);
            tif = ti;
            tif = tif / 100.0;
            ret = valueOf(tif);
// affiché dans le champ correspondant
            retardAff.setText(ret);

// réaffichage des 3 boutons de sélection des 3 profils modifiables
            bt1.setBackground(getResources().getDrawable(R.drawable.unx));
            bt2.setBackground(getResources().getDrawable(R.drawable.deuxx));
            bt3.setBackground(getResources().getDrawable(R.drawable.troisx));

// 3ème champ => no du profil en cours
            noC = come.substring(16, 18);
//affichage de la valeur
            noCAff.setText(noC);
            ti = parseInt(noC);
// si ce profil correspond à l'un des 3 profils : affichage spécifique du bouton correspondant
            switch (ti) {
                case 1:
                    bt1.setBackground(getResources().getDrawable(R.drawable.unsx));
                    break;
                case 2:
                    bt2.setBackground(getResources().getDrawable(R.drawable.deuxsx));
                    break;
                case 3:
                    bt3.setBackground(getResources().getDrawable(R.drawable.troissx));
                    break;
            }
            lb = come.substring(19);
            labelAff.setText(lb);
// positionnement de l'image de l'aiguille du compteur en fonction de la vitesse du moteur (rpm)
            iv = (ImageView) findViewById(R.id.a4View);
            im0 = getResources().getDrawable(R.drawable.aig2 );
            iv.setImageDrawable(im0);
//pour déterminer le centre de rotation
            Rect ri = new Rect();
            iv.getGlobalVisibleRect(ri);
            float ll = ri.left;
            float r = ri.right;
            float t = ri.top;
            float b = ri.bottom;
            float a;
// calcul de l'angle de rotation en fonction de rm
// amplitude du compteur 300 degrés   -75 degrés à +225 degrés  (0 => 15000 rpm)
            a = rm ;
            a = a/50f - 75f;
            if (a<0) a = a + 360f;
//repositionnement centre de rotation
            xc = (r - ll) + 32f ;
            yc = (b - t) - 12f ;
//rotation
            Matrix matrix = new Matrix();
            matrix.postRotate(a, xc, yc);
            matrix.postScale(0.5f, 0.5f);
            iv.setScaleType(ImageView.ScaleType.MATRIX);
            iv.setImageMatrix(matrix);
        }
    }



    private void clearUI() {
        //mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        //       if(intent.getStringExtra(EXTRAS_STATUS).equals("connected"))mConnected = true; else mConnected = false;
        String connectionStatus = intent.getStringExtra(EXTRAS_STATUS);

        mHandler = new Handler();
        Intent gattServiceIntent = new Intent(this, khp.BluetoothLeService.class);
        Log.d(TAG, "Try to bindService=" + bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE));
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());





        tpmAff = (TextView) this.findViewById(R.id.rpm);
        retardAff = (TextView) this.findViewById(R.id.retard);
        noCAff = (TextView) this.findViewById(R.id.noCDisp);
        labelAff = (TextView) this.findViewById(R.id.label);
        bt1 = (ImageButton) this.findViewById(R.id.unBut);
        bt2 = (ImageButton) this.findViewById(R.id.deuxBut);
        bt3 = (ImageButton) this.findViewById(R.id.troisBut);

        iv = (ImageView) this.findViewById(R.id.swipeView);

        com = new String();






// on utilise le LocationManager pour accéder à la localisation
        mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
// on implémente le location listener
        mlocListener = new MyLocationListener();

        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);

        iv.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                final Intent intent = new Intent(DeviceControlActivity.this, DeviceScanActivity.class);
                setContentView(R.layout.scan_layout);
                startActivity(intent);
            }

            public void onSwipeRight() {
                final Intent intent1 = new Intent(DeviceControlActivity.this, KheperSettingsActivity.class);
                setContentView(R.layout.settings_layout);
                intent1.putExtra(khp.KheperSettingsActivity.EXTRAS_DEVICE_NAME, mDeviceName);
                intent1.putExtra(KheperSettingsActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);

                startActivity(intent1);
            }
        });
        if (mDeviceAddress == null){
            // Use this check to determine whether BLE is supported on the device.  Then you can
            // selectively disable BLE-related features.
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
                finish();
            }

            // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
            // BluetoothAdapter through BluetoothManager.
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();

            // Checks if Bluetooth is supported on the device.
            if (mBluetoothAdapter == null) {
                Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            deviceList = new ArrayList<BluetoothDevice>();
            deviceList.clear();
            scanLeDevice(true);
            Toast.makeText(this, "Scanning...", Toast.LENGTH_LONG).show();
/*
            if(mScanning){
                SystemClock.sleep(500);
            }
            if(deviceList.size() != 1){
                final Intent intent2 = new Intent(DeviceControlActivity.this, DeviceScanActivity.class);
                setContentView(R.layout.listitem_device);
                startActivity(intent2);
                finish();
                return;
            }
            else {
                BluetoothDevice dev = deviceList.get(0);
                mDeviceName = dev.getName();
                mDeviceAddress = dev.getAddress();
            }
            */
        }
    }

    /**
     * Detects left and right swipes across a view.
     */
    public class OnSwipeTouchListener implements OnTouchListener {

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

        private final class GestureListener extends SimpleOnGestureListener {

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


    void scanLeDevice(final boolean enable)
    {
        if (enable) {
            // Stops scanning after a pre-defined scan period.

            mHandler.postDelayed(new Runnable(){
                @Override
                        public void run(){
                    mScanning= false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

                    if((deviceList.size() > 1)||(deviceList.size()==0)){
                        final Intent intent2 = new Intent(DeviceControlActivity.this, DeviceScanActivity.class);
                        setContentView(R.layout.listitem_device);
                        startActivity(intent2);
                        finish();
                        return;
                    }
                    BluetoothDevice dev = deviceList.get(0);
                    mDeviceName = dev.getName();
                    mDeviceAddress = dev.getAddress();
                    mBluetoothLeService.connect(mDeviceAddress);
                }

            },SCAN_PERIOD);


            mScanning = true;
            //F000E0FF-0451-4000-B000-000000000000
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);      }
    }

/*
    // Hander
      final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: // Notify change
     //               mLeDeviceListAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
*/
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                             boolean deviceFound = false;
                            for(BluetoothDevice listDev:deviceList){
                                if(listDev.getAddress().equals(device.getAddress())){
                                    deviceFound=true;
                                    break;
                                }
                            }
                            String prefix = device.getName().substring(0,3);
                            if(!deviceFound &&((prefix.equals("KHP"))||(prefix.equals("HMS")))){
                                deviceList.add(device);

                            }
                            }
                    });
                }
            };


    public class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location loc) {
// calcul et affichage de la vitesse
            float vit = loc.getSpeed();
            vit = vit * 3.6f;
            TextView vitText = (TextView) findViewById(R.id.speed);
            vitText.setText(String.format("%.1f km/h", vit));
        }


        public void onProviderDisabled(String provider) {
            // todo
        }

        public void onProviderEnabled(String provider) {
            // todo
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {

          /*  TextView altText = (TextView)findViewById(R.id.altitude);
            TextView latText = (TextView)findViewById(R.id.latitude);
            TextView lonText = (TextView)findViewById(R.id.longitude);
            String stat;
            stat = " ";
            switch(status)
            {
                case 0 : stat = "OUT_OF_SERVICE";break;
                case 1 : stat = "TEMPORARILY_UNAVAILABLE"; break;
                case 2 : stat = "AVAILABLE";
            }

            latText.setText(stat);
 //           lonText.setText("yyyyyy");
            altText.setText(provider);
            */
        }

    }
        @Override
        protected void onStart() {
            super.onStart();
        }


        @Override
        protected void onResume() {
            super.onResume();
        /*
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
        */
        }

        @Override
        protected void onPause() {
            super.onPause();
            if (mConnected) mBluetoothLeService.disconnect();
            unregisterReceiver(mGattUpdateReceiver);
            unbindService(mServiceConnection);
            mlocManager.removeUpdates(mlocListener);
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
            Log.d(TAG, "We are in destroy");
        }


    private void ShowDialog()
    {
    	Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show();
    }

    public void plusClick(View view)
    {
        mBluetoothLeService.WriteValue("+\n");
    }

    public void moinsClick (View view)
    {
        mBluetoothLeService.WriteValue("-\n");
    }

    public void unClick(View view)
    {
        mBluetoothLeService.WriteValue("v1;");
    }

    public void deuxClick(View view)
    {
        mBluetoothLeService.WriteValue("v2;");
    }

    public void troisClick(View view)
    {
        mBluetoothLeService.WriteValue("v3;");
    }

    public void stopClick (View view) {mBluetoothLeService.WriteValue("s;");}





    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(khp.BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(khp.BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(khp.BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        return intentFilter;
    }
}
