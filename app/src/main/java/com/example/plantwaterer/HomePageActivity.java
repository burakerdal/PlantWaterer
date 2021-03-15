package com.example.plantwaterer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Set;

import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import static com.example.plantwaterer.LoginPageActivity.currentuser;
import static com.example.plantwaterer.RegisterPageActivity.mSQLiteHelper;

public class HomePageActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 123;
    boolean hasCameraFlash = false;
    boolean flash = false;
    String choseFlash;
    int flashlight = -1;


    private ProgressDialog mProgressDlg;
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    private ArrayList<BluetoothDevice> mDeviceList2 = new ArrayList<BluetoothDevice>();/*Bluetooth deviceları için arraylist oluşturuyoruz.*/
    private BluetoothAdapter mBluetoothAdapter;/*Bluetooth adapter oluşturuyoruz.*/


    ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();



    SessionManager sessionManager;

    String name = "**",surname = "**",email ="**",password ="**";
    BottomNavigationView bnv;

    String uMail = "";

    TextView nameSurnameTV, mailTV;

    String plantTableName ="";

    final int REQUEST_CODE_LOCATION = 123;

    boolean dark = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);




        sessionManager = new SessionManager(getApplicationContext());

        nameSurnameTV = findViewById(R.id.nameSurnameTV);
        mailTV = findViewById(R.id.mailTV);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();/*Adapterımıza cihazdaki default olan adapteri atyoruz.*/

        if(mBluetoothAdapter==null){
            findViewById(R.id.navigation_devices).setEnabled(false);
        }

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
        registerReceiver(mReceiver, filter);


        uMail = sessionManager.getUsername();
        currentuser.setEmail(uMail);




        getUserInfo(uMail);


        nameSurnameTV.setText(name+" "+surname);
        mailTV.setText(uMail);

        bnv =  findViewById(R.id.navigation);
        bnv.setSelectedItemId(R.id.navigation_home);
        //findViewById(R.id.navigation_home).setBackgroundColor(Color.rgb(255,255,255));
        //findViewById(R.id.navigation_home).setBackgroundResource(R.drawable.gradient_two);
        /*
        ColorStateList textColorStates = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked}
                },
                new int[]{
                        Color.parseColor("#555555"),
                        Color.parseColor("#FFFFFF")
                });
        bnv.setItemTextColor(textColorStates);
        bnv.setItemIconTintList(textColorStates);*/

        bnv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        return true;
                    case R.id.navigation_devices:
                        if(mBluetoothAdapter!=null){
                            Intent btIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(btIntent, 1000);


                            if(mBluetoothAdapter.isEnabled()){
                                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                                if (pairedDevices == null || pairedDevices.size() == 0) {
                                    showToast("No Paired Device");
                                } else {
                                    list.addAll(pairedDevices);
                                }
                                Intent intent = new Intent(HomePageActivity.this, DeviceListActivity.class);
                                intent.putParcelableArrayListExtra("device.list", list);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                return true;
                            }
                        }
                        else{
                            showToast("Device not support Bluetooth");
                            return false;
                        }
                        return true;
                    case R.id.navigation_plants:
                        startActivity( new Intent(HomePageActivity.this,PlantListActivity.class));
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        return true;

                }
                return false;
            }
        });

        findViewById(R.id.settingsIV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingsDialog(HomePageActivity.this);
            }
        });

        findViewById(R.id.logoutIV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog(HomePageActivity.this);
            }
        });
    }

    private void getUserInfo(String uMail) {
        int position = 0;
        Cursor cursor = mSQLiteHelper.getData("SELECT * FROM user WHERE useremail = '"+uMail+"'");
        while (cursor.moveToNext()){
            int id = cursor.getInt(0);
            position = id;
            name = cursor.getString(1);
            surname = cursor.getString(2);
            email = cursor.getString(3);
            password = cursor.getString(4);

        }
    }

    private void showEditProfileDialog(Activity activity, String usermail) {

        int position = 0;
        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.user_update_dialog);

        final EditText edtName = dialog.findViewById(R.id.edtName);
        final EditText edtSurname = dialog.findViewById(R.id.edtSurname);
        final EditText edtEmail = dialog.findViewById(R.id.edtEmail);
        final EditText edtPass = dialog.findViewById(R.id.edtPass);
        Button btnUserUpdate = dialog.findViewById(R.id.btnUserUpdate);

        Cursor cursor = mSQLiteHelper.getData("SELECT * FROM user WHERE useremail = '"+usermail+"'");
        while (cursor.moveToNext()){
            int id = cursor.getInt(0);
            position = id;
            String name = cursor.getString(1);
            edtName.setText(name);
            String surname = cursor.getString(2);
            edtSurname.setText(surname);
            String email = cursor.getString(3);
            edtEmail.setText(email);
            String password = cursor.getString(4);
            edtPass.setText(password);


        }

        int width = (int) (activity.getResources().getDisplayMetrics().widthPixels*0.8);
        int height = (int) (activity.getResources().getDisplayMetrics().heightPixels*0.6);
        dialog.getWindow().setLayout(width,height);
        dialog.show();


        final int finalPosition = position;
        btnUserUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = currentuser.email;
                int iend = email.indexOf("@");
                if(iend !=-1){
                    plantTableName = email.substring(0,iend);
                }
                mSQLiteHelper.queryData("CREATE TABLE IF NOT EXISTS '"+plantTableName+"' (id INTEGER PRIMARY KEY AUTOINCREMENT, plantname VARCHAR, plantwtime VARCHAR, plantwamount VARCHAR, plantimage BLOB)");
                try{
                    mSQLiteHelper.updateData(
                            edtName.getText().toString().trim(),
                            edtSurname.getText().toString().trim(),
                            edtEmail.getText().toString().trim(),
                            edtPass.getText().toString().trim(),
                            finalPosition
                    );
                    currentuser.setName(edtName.getText().toString());
                    currentuser.setSurname(edtSurname.getText().toString());
                    currentuser.setEmail(edtEmail.getText().toString());
                    currentuser.setPassword(edtPass.getText().toString());
                    String newEmail = currentuser.email;
                    int iend2 = newEmail.indexOf("@");
                    String newPlantTableName ="";
                    if(iend2 !=-1){
                        newPlantTableName = newEmail.substring(0,iend);
                    }
                    mSQLiteHelper.queryData("ALTER TABLE '"+plantTableName+"' RENAME TO '"+newPlantTableName+"'");
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(),"update successfully",Toast.LENGTH_SHORT).show();
                }catch (Exception e){

                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        showLogoutDialog(HomePageActivity.this);
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

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_ON) {
                    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                    if (pairedDevices == null || pairedDevices.size() == 0) {
                        showToast("No Paired Device");
                    } else {
                        list.addAll(pairedDevices);
                    }
                    Intent intent3 = new Intent(HomePageActivity.this, DeviceListActivity.class);
                    intent3.putParcelableArrayListExtra("device.list", list);
                    startActivity(intent3);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mDeviceList2 = new ArrayList<BluetoothDevice>();
                mProgressDlg.show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                mProgressDlg.dismiss();
                Intent newIntent = new Intent(HomePageActivity.this, DeviceListActivity.class);
                newIntent.putParcelableArrayListExtra("device.list", list);
                newIntent.putParcelableArrayListExtra("device2.list", mDeviceList2);
                startActivity(newIntent);
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

   /* @Override
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
        if(requestCode == CAMERA_REQUEST){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }
            else{

            }
            return;

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }*/


    private void showLogoutDialog(Activity activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Log out?");
        builder.setMessage("Are you sure for exit?");
        builder.setIcon(R.drawable.ic_warning_yellow_24dp);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // log out and return login page
                sessionManager.setLogin(false);
                sessionManager.setUsername("");
                dialog.dismiss();
                startActivity( new Intent(HomePageActivity.this,LoginPageActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showSettingsDialog(final Activity activity) {

        int position = 0;
        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.home_user_menu);

        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);

        hasCameraFlash = getPackageManager().
                hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        final RelativeLayout menuMain = dialog.findViewById(R.id.settingsMain);
        RelativeLayout epRL = dialog.findViewById(R.id.eply);
        RelativeLayout flash1 = dialog.findViewById(R.id.flash1);
        RelativeLayout aboutly = dialog.findViewById(R.id.aboutly);
        SwitchCompat menuSwitchDark = dialog.findViewById(R.id.switchDark);
        SwitchCompat menuSwitchBluetooth = dialog.findViewById(R.id.switchBT);
        SwitchCompat menuSwitchFlash = dialog.findViewById(R.id.switchFlash);
        String[] whichflash = { "Back", "Front"};
        final Spinner lightSpinner = dialog.findViewById(R.id.lightSpinner);
        ArrayAdapter adapter2 = new ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,whichflash);
        lightSpinner.setAdapter(adapter2);

        int width = (int) (activity.getResources().getDisplayMetrics().widthPixels*0.9);
        int height = (int) (activity.getResources().getDisplayMetrics().heightPixels*0.8);
        dialog.getWindow().setLayout(width,height);
        dialog.show();

        epRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog(activity,uMail);
            }
        });

        if(mBluetoothAdapter!=null){

            if(mBluetoothAdapter.isEnabled()){
                menuSwitchBluetooth.setChecked(true);
            }else {
                menuSwitchBluetooth.setChecked(false);
            }
            menuSwitchBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intent, 1000);
                    }
                    else{
                        mBluetoothAdapter.disable();
                    }
                }
            });
        }
        else{
            menuSwitchBluetooth.setEnabled(false);
        }

        aboutly.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showAboutDialog(activity);
            }
        });



        if (flash){
            menuSwitchFlash.setChecked(true);
        }
        else{
            menuSwitchFlash.setChecked(false);
        }



        lightSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                choseFlash = parent.getItemAtPosition(position).toString();
                if(choseFlash.equalsIgnoreCase("Back")){
                    flashlight = 0;

                }
                else if (choseFlash.equalsIgnoreCase("Front")){
                    flashlight = 1;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if(hasCameraFlash){
            menuSwitchFlash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        flash = true;
                        flashLightOn(flashlight);
                    }
                    else{
                        flash = false;
                        flashLightOff(flashlight);
                    }
                }
            });
        }
        else{
            menuSwitchFlash.setEnabled(false);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void flashLightOn(int number) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[number];
            // 0 arka 1 on flash
            cameraManager.setTorchMode(cameraId, true);
        } catch (CameraAccessException e) {
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void flashLightOff(int number) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = cameraManager.getCameraIdList()[number];
            cameraManager.setTorchMode(cameraId, false);
        } catch (CameraAccessException e) {
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                showAboutDialog(HomePageActivity.this);
                break;
            case R.id.tutorial:
                showHowToUseDialog(HomePageActivity.this);
                break;
            case R.id.home_settings:
                showSettingsDialog(HomePageActivity.this);
                break;
            case R.id.exit:
                showLogoutDialog(HomePageActivity.this);
                break;
            default:
                break;
        }

        return true;
    }

    private void showAboutDialog(final Activity activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("PlantWaterer");
        builder.setMessage("PlantWaterer is a watering plant application for android mobile devices which works with Arduino device also."+"\r\n\n"+
                "This application was developed for Computer Engineering Senior Project on Dokuz Eylül University by Atakan Tuzcu and Burak Erdal in 2019."+"\r\n\n"+
                "version 2.3.0"+"\r\n"+
                "developed and designed by burakerdal"+"\r\n"+
                "Copyright © 2021");
        builder.setIcon(R.drawable.ic_info_black_24dp);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton("How To Use", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                showHowToUseDialog(activity);

            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void showHowToUseDialog(Activity activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("How To Use");
        builder.setMessage("1.) Visit *My Plants* page and add your plants."+"\r\n"+
                "2.) Visit *My Devices* page and pick Arduino called *PlantWaterer* in your bonded Bluetooth devices or search new device and find your Arduino device."+"\r\n"+
                "3.) Set connection between mobile device and Arduino."+"\r\n"+
                "4.) Pick a plant and send plant infos with *Apply Changes* button.");
        builder.setIcon(R.drawable.ic_live_help_orange_24dp);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }



}
