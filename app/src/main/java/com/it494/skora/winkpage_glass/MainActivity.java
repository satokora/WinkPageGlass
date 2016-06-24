package com.it494.skora.winkpage_glass;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;
import com.google.android.glass.widget.Slider;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity  implements HeadGestureDetector.OnHeadGestureListener {

   // private CardScrollView mCardScroller;
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Member object for the bluetooth services
    private BluetoothWinkService mWinkService = null;
    // Member object for head motion detection sensor
    private HeadGestureDetector mHeadGestureDetector;
    // Member object to keep a screen on
    private PowerManager.WakeLock mWakeLock;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

//    public static final int READY_TO_CONN =0;
//    public static final int CANCEL_CONN =1;

    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";


    private List<CardBuilder> mCards;
    private ArrayList<BluetoothDevice> mdevices;
    private CardScrollView mCardScrollView;
    private ExampleCardScrollAdapter mAdapter;
    private TextView mNextTextView;
    private TextView mPrevTextView;
//    private TextView mFirstTextView;
//    private TextView mLastTextView;

    private final EyeGesture eyeReceiver = new EyeGesture();

    private Slider mSlider;
    private Slider.Indeterminate mIndeterminate;

    // holds the bluetooth names/ids that we're associated with.
    ArrayAdapter<String> btArray;
    // bt adapter for all your bt needs
    BluetoothAdapter myBt;
    String TAG = "Wink Page";



    Context ctx;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_activity_layout);
        Log.e(TAG, "+++ ON CREATE +++");

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, TAG);
        this.mWakeLock.acquire();
        ctx = this;

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.google.android.glass.action.EYE_GESTURE");
        registerReceiver(eyeReceiver, filter);


        myBt = BluetoothAdapter.getDefaultAdapter();
        mCards = new ArrayList<CardBuilder>();
        mdevices = new ArrayList<BluetoothDevice>();
        if (myBt == null) {
            Toast.makeText(this, "Device Does not Support Bluetooth", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        detectAndSetUp();

        mCardScrollView = new CardScrollView(this);
        mAdapter = new ExampleCardScrollAdapter();
        mCardScrollView.setAdapter(mAdapter);
        mSlider = Slider.from(mCardScrollView);
        mCardScrollView.activate();
        setupClickLIstener();
        setContentView(mCardScrollView);

        mHeadGestureDetector = new HeadGestureDetector(this);
        mHeadGestureDetector.setOnHeadGestureListener(this);


//        mFirstTextView= (TextView)findViewById(R.id.topPage);
//        mLastTextView= (TextView)findViewById(R.id.lastPage);
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "++ ON START ++");


        if (!myBt.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (mWinkService == null) setupWink();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        Log.e(TAG, "+ ON RESUME +");

        if (mWinkService != null) {

            if (mWinkService.getState() == BluetoothWinkService.STATE_NONE) {
                mWinkService.start();
                mHeadGestureDetector.start();
            }
        }

    }

    @Override
    public synchronized void onPause() {

        mHeadGestureDetector.stop();

        Log.e(TAG, "- ON PAUSE -");
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {

        unregisterReceiver(mReceiver);
        // Stop the Bluetooth chat services
        if (mWinkService != null) mWinkService.stop();
        //mCardScroller.deactivate();
        this.mWakeLock.release();
        //mSensorManager.unregisterListener(mSensorEventListener);
        Log.e(TAG, "--- ON DESTROY ---");
        super.onDestroy();
    }
    private void setupWink() {
        Log.d(TAG, "setupWink()");


        // Initialize the BluetoothChatService to perform bluetooth connections
        mWinkService = new BluetoothWinkService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }


    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mWinkService.getState() != BluetoothWinkService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mWinkService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }
    /**
     * Reads a message.
     * @param message  A string of text to send.
     */
    private void readSettingsMessage(String message) {
        Log.e(TAG, "+ READ MESSAGE CALLED +");
        String nextGesture="";
        String prevGesture="";
        String firstGesture="";
        String lastGesture="";

        while(message.length()>0)
        {
            if(message.indexOf("NEXT")>=0)
            {
                message = message.replace("NEXT ","");
                message=message.trim();
                nextGesture=message.substring(0, message.indexOf(" "));
                message = message.replace(message.substring(0, message.indexOf(" ")), "");
                Log.e(TAG, "NEXT: " + nextGesture);
            }
            else if(message.indexOf("PREV")>=0)
            {
                message = message.replace("PREV ", "");
                message=message.trim();
                prevGesture=message.substring(0);
                //prevGesture=message.substring(0, message.indexOf(" "));
               // message = message.replace(message.substring(0, message.indexOf(" ")), "");
                Log.e(TAG, "PREV: " + prevGesture);
                message ="";

            }
//            else if(message.indexOf("FIRST")>=0)
//            {
//                message = message.replace("FIRST ","");
//                message=message.trim();
//                firstGesture=message.substring(0, message.indexOf(" "));
//                message = message.replace(message.substring(0, message.indexOf(" ")), "");
//                Log.e(TAG, "FIRST: " + firstGesture);
//            }
//            else if(message.indexOf("LAST")>=0)
//            {
//                message = message.replace("LAST ", "");
//                message=message.trim();
//                lastGesture=message.substring(0);
//                Log.e(TAG, "LAST: " + lastGesture);
//                message ="";
//            }

        }

        GestureMap.setNextPageGesture(nextGesture);
        GestureMap.setPrevPageGesture(prevGesture);
//        GestureMap.setTopPageGesture(firstGesture);
//        GestureMap.setLastPageGesture(lastGesture);

        setContentView(R.layout.bluetooth_activity_layout);
        mNextTextView= (TextView)findViewById(R.id.nextPage);
        mPrevTextView= (TextView)findViewById(R.id.prevPage);
        mNextTextView.setText(getGestureName(GestureMap.getNextPageGesture()));
        mPrevTextView.setText(getGestureName(GestureMap.getPrevPageGesture()));


    }

    private String getGestureName(String i)
    {

        if (i.equals(GestureMap.WINK))
        {
            return "Wink";
        }
        else if (i.equals(GestureMap.SHAKE_LEFT))
        {
            return "Shake to left";
        }
        else if (i.equals(GestureMap.SHAKE_RIGHT))
        {
            return "Shake to right";
        }
        else if (i.equals(GestureMap.LOOKUP))
        {
            return "Look up";
        }
        else if (i.equals(GestureMap.NOD))
        {
            return "Nod";
        }
//        else if (i==GestureMap.NOD)
//        {
//            return "Nod";
//        }
//        else if (i==GestureMap.DOUBLE_BLINK)
//        {
//            return "Double blink";
//        }
//        else if (i==GestureMap.DOUBLE_NOD)
//        {
//            return "Double nod";
//        }
        else
        {
            return "";
        }
    }
    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                   Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothWinkService.STATE_CONNECTED:
                            Toast.makeText(MainActivity.this, R.string.title_connected_to + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                            setContentView(R.layout.bluetooth_activity_layout);
                            break;
                        case BluetoothWinkService.STATE_CONNECTING:
                            Toast.makeText(MainActivity.this, R.string.title_connecting, Toast.LENGTH_SHORT).show();

                            break;
                        case BluetoothWinkService.STATE_LISTEN:
                        case BluetoothWinkService.STATE_NONE:
                            Toast.makeText(MainActivity.this, R.string.title_not_connected, Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    //Toast.makeText(MainActivity.this, "write", Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.i(TAG,readMessage);
                    //set the message to screen
                    if(readMessage.indexOf("SETTINGS")>=0)
                    {
                        readMessage=readMessage.replace("SETTINGS","");
                        readSettingsMessage(readMessage);
                    }

                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();

                    if(msg.getData().getString(TOAST).equalsIgnoreCase("Device connection was lost"))
                    {
                        finish();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data){

        Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = myBt.getRemoteDevice(address);
                    // Attempt to connect to the device
                    mWinkService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupWink();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void detectAndSetUp() {

        if(myBt.isDiscovering()){
            //検索中の場合は検出をキャンセルする
            myBt.cancelDiscovery();
        }
        myBt.startDiscovery();
        Log.e(TAG, "++++++DISCOVERY STARTED+++++");

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

        Set<BluetoothDevice> pairedDevices = myBt.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {

                Log.i(TAG, device.getName() + " found");

                Card card = new Card(getApplicationContext());
                card.setText(device.getName());
                mCards.add(card);
                mdevices.add(device);

            }
        }

    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver(){
        //検出されたデバイスからのブロードキャストを受ける
        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            String dName = null;
            BluetoothDevice foundDevice;
            Card card = null;

            Log.e(TAG, "++++++ON RECEIVE+++++");
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if((dName = foundDevice.getName()) != null){
                    if(foundDevice.getBondState() != BluetoothDevice.BOND_BONDED){
                        Log.i(TAG, foundDevice.getName() + " found");

                        card = new Card(getApplicationContext());
                        card.setText(foundDevice.getName());
                        mCards.add(card);
                        mdevices.add(foundDevice);
                    }
                }

            }

        }
    };

    private void setupClickLIstener(){
        mCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) {
                int index = mCardScrollView.getSelectedItemPosition();
                BluetoothDevice dev = mdevices.get(index);
                Log.i(TAG, dev.getAddress());
                BluetoothDevice device = myBt.getRemoteDevice(dev.getAddress());
                // Attempt to connect to the device
                mWinkService.connect(device);

            }
        });
    }

    @Override
    public void onNod() {
        sendMessage(GestureMap.NOD);
        Log.i(TAG, "nodded");
    }

    @Override
    public void onShakeToLeft() {
        sendMessage(GestureMap.SHAKE_LEFT);
        Log.i(TAG, "shake to left");
    }

    @Override
    public void onShakeToRight() {
        sendMessage(GestureMap.SHAKE_RIGHT);
        Log.i(TAG, "shake to right");
    }

    @Override
    public void onHey() {
        sendMessage(GestureMap.LOOKUP);
        Log.i(TAG, "hey!");
    }


    public class EyeGesture extends BroadcastReceiver {
        private int count = 0;
        public EyeGesture() {
            count = 0;
        }


        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra("gesture").equals("WINK")) {
                //count++;
                //Disable Camera Snapshot
                abortBroadcast();
                setCurrentCount(1);
                Log.i(TAG, getCurrentCount());
                sendMessage(GestureMap.WINK);

            }
            // if those gestures become available in the future...
//            else if (intent.getStringExtra("gesture").equals("BLINK")) {
//
//                abortBroadcast();
//                Log.i(TAG, "blinked");
//            }
//            else if (intent.getStringExtra("gesture").equals("DOFF")) {
//
//                abortBroadcast();
//                Log.i(TAG, "doffed");
//            }
//            else if (intent.getStringExtra("gesture").equals("DON")) {
//
//                abortBroadcast();
//                Log.i(TAG, "donned");
//            }
//            else if (intent.getStringExtra("gesture").equals("DOUBLE_BLINK")) {
//
//                abortBroadcast();
//                Log.i(TAG, "double blinked");
//            }
//            else if (intent.getStringExtra("gesture").equals("DOUBLE_WINK")) {
//
//                abortBroadcast();
//                Log.i(TAG, "double winked");
//            }
//            else {
//                Log.e("SOMETHING", "is detected " + intent.getStringExtra("gesture"));
//            }
        }

        public void setCurrentCount(int num){
            this.count += num;
        }
        public String getCurrentCount(){
            return "Winked " + count + " times";
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bluetooth, menu);
        return true;
    }


    private class ExampleCardScrollAdapter extends CardScrollAdapter {

        @Override
        public int getPosition(Object item) {
            return mCards.indexOf(item);
        }

        @Override
        public int getCount() {
            return mCards.size();
        }

        @Override
        public Object getItem(int position) {
            return mCards.get(position);
        }

        @Override
        public int getViewTypeCount() {
            return CardBuilder.getViewTypeCount();
        }

        @Override
        public int getItemViewType(int position){
            return mCards.get(position).getItemViewType();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return mCards.get(position).getView(convertView, parent);
        }
    }

}