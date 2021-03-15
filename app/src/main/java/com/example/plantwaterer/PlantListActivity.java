package com.example.plantwaterer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Set;

import static java.lang.Thread.sleep;

import static com.example.plantwaterer.RegisterPageActivity.mSQLiteHelper;
import static com.example.plantwaterer.LoginPageActivity.currentuser;


public class PlantListActivity extends AppCompatActivity {

    ListView mListView;
    ArrayList<Plant> mList;
    PlantListAdapter mAdapter = null;

    private ProgressDialog mProgressDlg;
    private ArrayList<BluetoothDevice> mDeviceList2 = new ArrayList<BluetoothDevice>();
    private BluetoothAdapter mBluetoothAdapter;/*Bluetooth adapter oluşturuyoruz.*/


    ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();

    ImageView imageViewIcon;

    String email;
    String plantTableName;

    BottomNavigationView bnv;

    FloatingActionButton floatingActionButton;


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_list);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mListView = findViewById(R.id.plistView);
        mList = new ArrayList<>();
        mAdapter = new PlantListAdapter(this,R.layout.plantitem,mList);
        mListView.setAdapter(mAdapter);

        bnv = (BottomNavigationView) findViewById(R.id.navigation);
        floatingActionButton = findViewById(R.id.floatingActionButton);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent(PlantListActivity.this,PlantChooserActivity.class));
            }
        });

        bnv.setSelectedItemId(R.id.navigation_plants);
        //findViewById(R.id.navigation_plants).setBackgroundColor(Color.rgb(255,255,255));
        //findViewById(R.id.navigation_plants).setBackgroundResource(R.drawable.radius);

        bnv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        startActivity( new Intent(PlantListActivity.this,HomePageActivity.class));
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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
                                Intent intent = new Intent(PlantListActivity.this, DeviceListActivity.class);
                                intent.putParcelableArrayListExtra("device.list", list);
                                startActivity(intent);
                                return true;
                            }
                        }
                        else{
                            showToast("Device not support Bluetooth");
                            return false;
                        }
                        return true;

                        /*
                        showToast("Should be on the Home Page for this action! Redirecting....");

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(PlantListActivity.this,HomePageActivity.class));
                            }
                        }, 2000);
                        return true;
                        */

                    case R.id.navigation_plants:
                        // şu anki sayfa
                        return true;

                }
                return false;
            }
        });

        mSQLiteHelper = new SQLiteHelper(this,"PlantWaterer.sqlite",null,1);

        email = currentuser.email;
        int iend = email.indexOf("@");
        if(iend !=-1){
            plantTableName = email.substring(0,iend);
        }

        mSQLiteHelper.queryData("CREATE TABLE IF NOT EXISTS '"+plantTableName+"' (id INTEGER PRIMARY KEY AUTOINCREMENT, plantname VARCHAR, plantwtime VARCHAR, plantwamount VARCHAR, plantimage BLOB)");


        Cursor cursor = mSQLiteHelper.getData("SELECT * FROM '"+plantTableName+"'");
        mList.clear();
        while (cursor.moveToNext()){
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            String time = cursor.getString(2);
            String amount = cursor.getString(3);
            byte[] image = cursor.getBlob(4);

            mList.add(new Plant(id,name,time,amount,image));
        }
        mAdapter.notifyDataSetChanged();

        if(mList.size() == 0){
            Toast.makeText(getApplicationContext(),"No record",Toast.LENGTH_SHORT).show();
        }

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                CharSequence[] items = {" Update", " Delete"};

                AlertDialog.Builder dialog = new AlertDialog.Builder(PlantListActivity.this);

                dialog.setTitle("Choose an option");
                dialog.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which==0){
                            Cursor c = mSQLiteHelper.getData("SELECT id FROM '"+plantTableName+"'");
                            ArrayList<Integer> arrID = new ArrayList<Integer>();
                            while (c.moveToNext()){
                                arrID.add(c.getInt(0));
                            }
                            showUpdateDialog(PlantListActivity.this,arrID.get(position));
                        }
                        if(which==1){
                            Cursor c = mSQLiteHelper.getData("SELECT id FROM '"+plantTableName+"'");
                            ArrayList<Integer> arrID = new ArrayList<Integer>();
                            while (c.moveToNext()){
                                arrID.add(c.getInt(0));
                            }
                            showDeleteDialog(arrID.get(position));


                        }
                    }
                });
                dialog.show();

            }
        });

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
    }

    private void showDeleteDialog(final int idRecord) {
        AlertDialog.Builder dialogDelete = new AlertDialog.Builder(PlantListActivity.this);
        dialogDelete.setTitle("Warning!!");
        dialogDelete.setMessage("Are you sure to delete?");
        dialogDelete.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try{
                    mSQLiteHelper.deleteData2(plantTableName,idRecord);
                    Toast.makeText(getApplicationContext(),"delete successfully",Toast.LENGTH_SHORT).show();

                }catch (Exception e){
                    e.printStackTrace();
                }
                updateRecordList();
            }
        });
        dialogDelete.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogDelete.show();
    }

    private void showUpdateDialog(final Activity activity, final int position){
        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.plant_update_dialog);
        dialog.setTitle("Update");

        imageViewIcon = dialog.findViewById(R.id.imageViewRecord);
        final EditText edtName = dialog.findViewById(R.id.edtName);
        final EditText edtTime = dialog.findViewById(R.id.edtTime);
        final EditText edtAmount = dialog.findViewById(R.id.edtAmount);
        Button btnUpdate = dialog.findViewById(R.id.btnUpdate);

        Cursor cursor = mSQLiteHelper.getData("SELECT * FROM '"+plantTableName+"' WHERE id ="+position);
        mList.clear();
        while (cursor.moveToNext()){
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            edtName.setText(name);
            String time = cursor.getString(2);
            edtTime.setText(time);
            String amount = cursor.getString(3);
            edtAmount.setText(amount);
            byte[] image = cursor.getBlob(4);
            imageViewIcon.setImageBitmap(BitmapFactory.decodeByteArray(image,0,image.length));

            mList.add(new Plant(id,name,time,amount,image));
        }

        int width = (int) (activity.getResources().getDisplayMetrics().widthPixels*0.95);
        int height = (int) (activity.getResources().getDisplayMetrics().heightPixels*0.7);
        dialog.getWindow().setLayout(width,height);
        dialog.show();

        imageViewIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(
                        PlantListActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        888
                );
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    mSQLiteHelper.updateData2(
                            plantTableName,
                            edtName.getText().toString().trim(),
                            edtTime.getText().toString().trim(),
                            edtAmount.getText().toString().trim(),
                            PlantChooserActivity.imageViewToByte(imageViewIcon),
                            position
                    );
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(),"update successfully",Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(),"Warning!! Arduino has old values, Please reload to that plant!",Toast.LENGTH_LONG).show();
                }catch (Exception e){

                }
                updateRecordList();
            }
        });

    }

    private void updateRecordList() {
        Cursor cursor = mSQLiteHelper.getData("SELECT * FROM '"+plantTableName+"'");
        mList.clear();
        while(cursor.moveToNext()){
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            String age = cursor.getString(2);
            String phone = cursor.getString(3);
            byte[] image = cursor.getBlob(4);

            mList.add(new Plant(id,name,age,phone,image));
        }
        mAdapter.notifyDataSetChanged();
    }

    public static byte[] imageViewToByte(ImageView mImageView) {
        Bitmap bitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100,outputStream);
        byte[] byteArray = outputStream.toByteArray();
        return  byteArray;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == 888){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/*");
                startActivityForResult(gallery,888);

            }
            else{

            }
            return;

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == 888 && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                Uri resultUri = result.getUri();
                imageViewIcon.setImageURI(resultUri);
            }
            else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error = result.getError();

            }

        }

        super.onActivityResult(requestCode, resultCode, data);
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
                    Intent intent3 = new Intent(PlantListActivity.this, DeviceListActivity.class);
                    intent3.putParcelableArrayListExtra("device.list", list);
                    startActivity(intent3);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mDeviceList2 = new ArrayList<BluetoothDevice>();
                mProgressDlg.show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                mProgressDlg.dismiss();
                Intent newIntent = new Intent(PlantListActivity.this, DeviceListActivity.class);
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





    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
