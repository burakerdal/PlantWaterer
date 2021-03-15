package com.example.plantwaterer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.TestLooperManager;
import android.speech.RecognizerIntent;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import static com.example.plantwaterer.LoginPageActivity.currentuser;
import static com.example.plantwaterer.RegisterPageActivity.mSQLiteHelper;

public class DeviceChooserActivity extends AppCompatActivity {


    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    private BluetoothAdapter mBluetoothAdapter;

    BottomNavigationView bnv;
    TextView dnameTV;

    Spinner plantSpinner;

    Button connectDevice,applyChangesDevice;

    TextView txvResult;

    BluetoothSocket mmSocket;

    InputStream mmInputStream;
    OutputStream mmOutputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    private boolean stopWorker;
    String sGelenVeri;

    private DeviceListAdapter mAdapter2;

    private ArrayList<BluetoothDevice> mDeviceList2;

    String dname;
    int dPosition;
    boolean dStatus;

    String email ="";
    String plantTableName = "";

    ArrayList<String> plantNameList;

    String plantSpinnerSelected = "";

    ImageView dcPimgIcon,dcInfoImg;
    TextView dcTxtName, dcTxtTime, dcTxtAmount;

    ImageView wateringInfoImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_chooser);

        wateringInfoImage = findViewById(R.id.wateringInfoImage);




        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();






        dcPimgIcon = findViewById(R.id.dcPimgIcon);
        dcInfoImg = findViewById(R.id.wateringInfoImage);
        dcTxtName = findViewById(R.id.dcTxtName);
        dcTxtTime = findViewById(R.id.dcTxtTime);
        dcTxtAmount = findViewById(R.id.dcTxtAmount);

        dcInfoImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWarningDialog(DeviceChooserActivity.this);
            }
        });

        email = currentuser.email;

        int iend = email.indexOf("@");
        if(iend !=-1){
            plantTableName = email.substring(0,iend);
        }

        plantNameList = new ArrayList<>();

        plantSpinner = findViewById(R.id.spinner);
        fillSpinner();

        plantSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                plantSpinnerSelected = parent.getItemAtPosition(position).toString();
                getPlantInfo(plantSpinnerSelected);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        mAdapter2 = new DeviceListAdapter(this);

        dStatus =false;
        mAdapter2.setData(mDeviceList2);
        mDeviceList2 = getIntent().getExtras().getParcelableArrayList("device.list");

        connectDevice = findViewById(R.id.deviceConnectButton);
        connectDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BluetoothDevice device = mDeviceList2.get(dPosition);

                if(connectDevice.getText().toString().equals("Connect")){
                    try {
                        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                        mmSocket = device.createRfcommSocketToServiceRecord(uuid);
                        mmSocket.connect();
                        mmOutputStream = mmSocket.getOutputStream();
                        dStatus = true;
                        Toast.makeText(getApplicationContext(), device.getName() +" connected",Toast.LENGTH_SHORT).show();

                        beginListenForData();

                        connectDevice.setText("Disconnect");

                    } catch (Exception ignored) {
                        Toast.makeText(getApplicationContext(),"could not connect!!",Toast.LENGTH_SHORT).show();
                    }

                }
                else if(connectDevice.getText().toString().equals("Disconnect")){

                    try {
                        mmSocket.close();
                        dStatus = false;
                        connectDevice.setText("Connect");

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getApplicationContext(), device.getName() +" disconnect",Toast.LENGTH_SHORT).show();

                }

            }
        });

        applyChangesDevice = findViewById(R.id.applyDevice);
        applyChangesDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dStatus){
                    try {
                        sendData(dcTxtTime.getText().toString()+","+dcTxtAmount.getText().toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getApplicationContext(),dcTxtTime.getText().toString()+","+dcTxtAmount.getText().toString()+" sent",Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getApplicationContext(),"Should be connect to device before!",Toast.LENGTH_SHORT).show();
                }



            }
        });



        dnameTV = findViewById(R.id.deviceNameTV);
        dname = getIntent().getStringExtra("deviceName");
        dPosition = Integer.parseInt(getIntent().getStringExtra("position"));

        dnameTV.setText(dname);

        bnv = findViewById(R.id.navigation);

        bnv.setSelectedItemId(R.id.navigation_devices);
        //findViewById(R.id.navigation_devices).setBackgroundResource(R.drawable.radius);


        //findViewById(R.id.navigation_devices).setBackgroundResource(R.drawable.radius_selected_menu_item);
        /*ColorStateList textColorStates = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked}
                },
                new int[]{
                        Color.parseColor("#75AB6D"),
                        Color.parseColor("#FFFFFF")
                });
        bnv.setItemTextColor(textColorStates);
        bnv.setItemIconTintList(textColorStates);*/

        bnv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        if(dStatus){
                            try {
                                mmSocket.close();
                                dStatus = false;
                                connectDevice.setText("Connect");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(getApplicationContext(),"Disconnect and Redirecting....",Toast.LENGTH_SHORT).show();


                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(DeviceChooserActivity.this,HomePageActivity.class));
                                }
                            }, 2000);
                        }
                        else{
                            Intent i0 = new Intent(DeviceChooserActivity.this,HomePageActivity.class);
                            startActivity(i0);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }
                        return true;
                    case R.id.navigation_devices:

                        return true;

                    case R.id.navigation_plants:
                        if(dStatus){
                            try {
                                mmSocket.close();
                                dStatus = false;
                                connectDevice.setText("Connect");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(getApplicationContext(),"Disconnect and Redirecting....",Toast.LENGTH_SHORT).show();


                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(DeviceChooserActivity.this,PlantListActivity.class));
                                }
                            }, 2000);
                        }
                        else{
                            Intent i0 = new Intent(DeviceChooserActivity.this,PlantListActivity.class);
                            startActivity(i0);
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        }
                        return true;

                }
                return false;
            }
        });
    }


    private void showWarningDialog(Activity activity){
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Warning!!");
        builder.setMessage("Reconnect and reset device can be reqıired after watering to plant for new watering");
        builder.setIcon(R.drawable.ic_warning_yellow_24dp);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getPlantInfo(String plantSpinnerSelected) {

        int id = -1;
        String name = "", time = "", amount = "";
        byte[] image = new byte[0];
        final Cursor cursor = mSQLiteHelper.getData("SELECT * FROM '"+plantTableName+"' WHERE plantname = '"+plantSpinnerSelected+"'");
        while (cursor.moveToNext()){
            id = cursor.getInt(0);
            name = cursor.getString(1);
            time = cursor.getString(2);
            amount = cursor.getString(3);
            image = cursor.getBlob(4);
        }

        dcTxtName.setText(name);
        dcTxtTime.setText(time);
        dcTxtAmount.setText(amount);

        byte[] recordImage = image;
        Bitmap bitmap = BitmapFactory.decodeByteArray(recordImage,0,recordImage.length);
        dcPimgIcon.setImageBitmap(bitmap);


    }

    void fillSpinner(){

        final Cursor cursor = mSQLiteHelper.getData("SELECT plantname FROM '"+plantTableName+"'");
        while (cursor.moveToNext()){
            String pname = cursor.getString(0);
            plantNameList.add(pname);
        }


        ArrayAdapter adapter1 = new ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,plantNameList);
        plantSpinner.setAdapter(adapter1);
    }

    void sendData(String data) throws IOException {
        try {
            mmOutputStream.write(String.valueOf(data).getBytes());
            Toast.makeText(getApplicationContext(), data +" sent",Toast.LENGTH_SHORT).show();
        } catch (Exception ignored) {

        }
    }

    void beginListenForData() {
        try {
            final Handler handler = new Handler();
            final byte delimiter = 10; //This is the ASCII code for a newline character

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];
            workerThread = new Thread(new Runnable() {
                public void run() {
                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                        try {

                            if(mmInputStream != null){
                                int bytesAvailable = mmInputStream.available();
                                if (bytesAvailable > 0) {
                                    byte[] packetBytes = new byte[bytesAvailable];
                                    mmInputStream.read(packetBytes);
                                    for (int i = 0; i < bytesAvailable; i++) {
                                        byte b = packetBytes[i];
                                        if (b == delimiter) {
                                            final byte[] encodedBytes = new byte[readBufferPosition];
                                            System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                            final String data = new String(readBuffer, StandardCharsets.US_ASCII);
                                            readBufferPosition = 0;



                                            handler.post(new Runnable() {
                                                public void run() {
                                                    sGelenVeri = data;
                                                    //sGelenVeri = sGelenVeri.substring(0, 3);
                                                    Toast.makeText(getApplicationContext(),sGelenVeri,Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else {
                                            readBuffer[readBufferPosition++] = b;
                                        }
                                    }
                                }
                            }

                        } catch (IOException ex) {
                            stopWorker = true;
                        }
                    }
                }
            });
            workerThread.start();
        } catch (Exception ignored) {
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    public void getSpeechInput(View view) {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txvResult.setText(result.get(0));
                }
                break;
        }
    }
}
