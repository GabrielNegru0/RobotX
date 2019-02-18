package com.example.robotx;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import java.io.IOException;
import java.util.Set;
import java.util.ArrayList;
import java.util.UUID;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.TextView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import static android.content.ContentValues.TAG;

public class PopUp extends Activity {

    private ProgressDialog progress;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static BluetoothSocket btSocket = null;
    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_BLUETOOTH_DISCOVERABLE = 2;
    public static String EXTRA_VALUE = "device_address";
    //widgets
    ListView devicelist;
    ListView availableList;
    //Bluetooth
    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    private ArrayList<String> arrayList = new ArrayList<String>();
    private ArrayAdapter<String> arrayStringAdapter;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pop_up_layout);

        devicelist    = (ListView)findViewById(R.id.l1);
        availableList = (ListView)findViewById(R.id.l2);

        myBluetooth  = BluetoothAdapter.getDefaultAdapter();

        CheckBluetoothConnection();
        Log.e(TAG,"----------4----" );



        arrayStringAdapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1, arrayList);
        availableList.setAdapter(arrayStringAdapter);
        availableList.setOnItemClickListener(myListClickListener);

    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startDiscoveryDevices()
    {
        checkBTPermissions();
        myBluetooth.startDiscovery();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryResult ,filter);
        Log.e(TAG,"----------3----" );
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void CheckBluetoothConnection()
    {
        if(myBluetooth == null)
        {
            //Show a message. that the device has no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            //finish apk
            finish();
        }
        if(!myBluetooth.isEnabled())
        {
            //Ask to the user turn the bluetooth on
            Intent turnBtOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBtOn,REQUEST_ENABLE_BT);
        }
        if(myBluetooth.isEnabled())
        {
            msg("Bluetooth OK");
            MakeVisible();
            pairedDevicesList();
            startDiscoveryDevices();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
// TODO Auto-generated method stub
        if(requestCode == REQUEST_ENABLE_BT){
            if (resultCode == RESULT_OK){
                msg("Bluetooth  Enabled.");
                MakeVisible();
                pairedDevicesList();
                startDiscoveryDevices();
            }
            if(resultCode == RESULT_CANCELED){
                msg("No Bluetooth, Try again!");
                Intent in = new Intent(PopUp.this, MainActivity.class);
                startActivity(in);
            }
        }
        if(requestCode == REQUEST_BLUETOOTH_DISCOVERABLE)
        {
            if(resultCode != RESULT_CANCELED)
            {
                msg("Your device is visible for others.");
            }
            if(resultCode == RESULT_CANCELED)
            {
                msg("Not visible.");
            }
        }
    }

    private void pairedDevicesList()
    {
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size()>0)
        {
            for(BluetoothDevice bt : pairedDevices)
            {
                list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found, try to find one.", Toast.LENGTH_LONG).show();
            /*Intent in = new Intent(PopUp.this, MainActivity.class);
            //Change the activity.
            in.putExtra(EXTRA_VALUE,"0");
            startActivity(in);*/
        }
        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked
    }

    private final BroadcastReceiver discoveryResult = new BroadcastReceiver() {
        @Override

        public void onReceive(Context context, Intent intent)
        {Log.e(TAG,"----------1----" );
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {Log.e(TAG,"----------2----" );
                BluetoothDevice remoteDevice= intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    arrayList.add(remoteDevice.getName() + "\n" + remoteDevice.getAddress());
                    arrayStringAdapter.notifyDataSetChanged();
            }
        }
    };

    public AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView<?> av, View v, int arg2, long arg3)
        {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String addresss = info.substring(info.length() - 17);
            Log.e(TAG,"----------ADDRES-----" + addresss);

           ConnectBT connectBtObject = new ConnectBT();
           connectBtObject.ad = addresss;
           connectBtObject.execute();

        }
    };


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.l1) {
            return true;
        }
        if (id == R.id.l2) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
    }

 public void MakeVisible(){
     Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
     discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
     startActivityForResult(discoverableIntent,REQUEST_BLUETOOTH_DISCOVERABLE);

 }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private String ad;
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(PopUp.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void...result) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice bltDevice = myBluetooth.getRemoteDevice(ad);//connects to the device's address and checks if it's available
                    btSocket = bltDevice.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed.Please Try again.");
                String p = null ;
                Intent intentToGoToMainn = new Intent(PopUp.this, MainActivity.class);
                intentToGoToMainn.putExtra(EXTRA_VALUE, p); //this will be received at MainActivity to relaod the conection (class)
                //finish();
            }
            else
            {
                Log.e(TAG,"----------Connection done------");
                msg("Connected.");
                isBtConnected = true;
                Intent intentToGoToMain = new Intent(PopUp.this, MainActivity.class);
                intentToGoToMain.putExtra(EXTRA_VALUE,"2");

                //Change the activity.
                startActivity(intentToGoToMain);
            }
            progress.dismiss();
        }

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unregisterReceiver(discoveryResult);
        //myBluetooth.disable();

    }

    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }
}
