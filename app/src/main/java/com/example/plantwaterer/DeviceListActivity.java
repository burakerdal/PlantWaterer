package com.example.plantwaterer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;


import android.widget.TextView;
import android.widget.Toast;

import android.os.Bundle;

import static com.example.plantwaterer.RegisterPageActivity.mSQLiteHelper;

public class DeviceListActivity extends AppCompatActivity {

    private ProgressDialog mProgressDlg;

    BluetoothAdapter mBluetoothAdapter;
    private ListView mListView;
    private DeviceListAdapter mAdapter;
    private ArrayList<BluetoothDevice> mDeviceList;

    private ListView mListView2;
    private DeviceListAdapter mAdapter2;
    private ArrayList<BluetoothDevice> mDeviceList2;

    final int REQUEST_CODE_LOCATION = 123;

    boolean pair = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);



        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();



        mDeviceList = getIntent().getExtras().getParcelableArrayList("device.list");
        mDeviceList2 = getIntent().getExtras().getParcelableArrayList("device2.list");


        mListView = (ListView) findViewById(R.id.lv_paired);
        mListView2 = (ListView) findViewById(R.id.lv_newDevice);

        mAdapter = new DeviceListAdapter(this);
        mAdapter2 = new DeviceListAdapter(this);

        mAdapter.setData(mDeviceList);
        mAdapter2.setData(mDeviceList2);


        mAdapter.setListener(new DeviceListAdapter.OnPairButtonClickListener() {
            @Override
            public void onPairButtonClick(int position,TextView txt) {
                BluetoothDevice device = mDeviceList.get(position);

                if(txt.getText().toString().equals("Pair") || txt.getText().toString().equals("Delete")){
                    if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                        unpairDevice(device);
                    } else {
                        Toast.makeText(getApplicationContext(),"Pairing...",Toast.LENGTH_SHORT).show();
                        pairDevice(device);
                    }
                }
                else if (txt.getText().toString().equals("Select")){

                    Intent i = new Intent(DeviceListActivity.this,DeviceChooserActivity.class);
                    i.putExtra("deviceName",device.getName().toString());
                    i.putExtra("position",String.valueOf(position));
                    i.putParcelableArrayListExtra("device.list", mDeviceList);
                    startActivity(i);

                }

            }
        });

        mAdapter2.setListener(new DeviceListAdapter.OnPairButtonClickListener() {
            @Override
            public void onPairButtonClick(int position,TextView txt) {
                BluetoothDevice device = mDeviceList2.get(position);

                if(txt.getText().toString().equals("Pair") || txt.getText().toString().equals("Delete")){
                    if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                        unpairDevice(device);
                    } else {
                        Toast.makeText(getApplicationContext(),"Pairing...",Toast.LENGTH_SHORT).show();
                        pairDevice(device);
                        Intent i = new Intent(DeviceListActivity.this,DeviceChooserActivity.class);
                        i.putExtra("deviceName",device.getName().toString());
                        i.putExtra("position",String.valueOf(position));
                        i.putParcelableArrayListExtra("device2.list", mDeviceList2);
                        startActivity(i);


                    }

                }
                else if (txt.getText().toString().equals("Seç")){

                    Intent i = new Intent(DeviceListActivity.this,DeviceChooserActivity.class);
                    i.putExtra("deviceName",device.getName().toString());
                    i.putExtra("position",String.valueOf(position));
                    i.putParcelableArrayListExtra("device2.list", mDeviceList2);
                    startActivity(i);

                }

            }
        });

        mListView.setAdapter(mAdapter);
        mListView2.setAdapter(mAdapter2);

        registerReceiver(mPairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));


        mProgressDlg = new ProgressDialog(this);
        mProgressDlg.setMessage("Scanning...");
        mProgressDlg.setCancelable(false);
        mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mBluetoothAdapter.cancelDiscovery();
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mPairReceiver, filter);

        findViewById(R.id.refreshfloatingActionButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(
                        DeviceListActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_LOCATION
                );
            }
        });

    }


    /*******************************************
     * ONCREATE FİNİSHED
     *******************************************/

    @Override
    public void onDestroy() {
        unregisterReceiver(mPairReceiver);

        super.onDestroy();
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    Toast.makeText(getApplicationContext(),"Paired successfuly",Toast.LENGTH_SHORT).show();
                    pair = true;

                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                    Toast.makeText(getApplicationContext(),"can not paired",Toast.LENGTH_SHORT).show();
                    pair = false;
                }

                mAdapter.notifyDataSetChanged();
            }
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_ON) {
                    //showToast("Açık");
                    //showEnabled();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mDeviceList2 = new ArrayList<BluetoothDevice>();
                mProgressDlg.show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                mProgressDlg.dismiss();

            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device.getName() != null){
                    if(device.getBondState() != BluetoothDevice.BOND_BONDED){
                        mDeviceList2.add(device);
                        showToast("Found Devices " + device.getName());
                    }

                }
            }
        }
    };

    @Override
    public void onBackPressed() {
        startActivity(new Intent(DeviceListActivity.this,HomePageActivity.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_LOCATION){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                mBluetoothAdapter.startDiscovery();
            }
            else{
                showToast("location permission not granted");
            }
            return;

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPause() {
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
        }
        super.onPause();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }



}
