package com.example.plantwaterer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import static com.example.plantwaterer.RegisterPageActivity.mSQLiteHelper;
import static com.example.plantwaterer.LoginPageActivity.currentuser;
import static java.lang.Thread.sleep;

public class PlantChooserActivity extends AppCompatActivity {


    int plant_index = 99;

    Button addDBBT;
    EditText pNameET,d1ET,d2ET;
    ImageView pImageView;

    final int REQUEST_CODE_GALLERY = 999;

    String email;
    String plantTableName;

    RadioGroup rgroup;
    RadioButton rb;

    boolean r1 = false, r2 = false;

    RadioButton appGalleryRadio1;
    RadioButton appGalleryRadio2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_chooser);

        pNameET = (EditText) findViewById(R.id.plantnameEditText);




        d1ET = (EditText) findViewById(R.id.delay1ET);
        d2ET = (EditText) findViewById(R.id.delay2ET);

        mSQLiteHelper = new SQLiteHelper(this,"PlantWaterer.sqlite",null,1);

        email = currentuser.email;
        int iend = email.indexOf("@");
        if(iend !=-1){
            plantTableName = email.substring(0,iend);
        }


        mSQLiteHelper.queryData("CREATE TABLE IF NOT EXISTS '"+plantTableName+"' (id INTEGER PRIMARY KEY AUTOINCREMENT, plantname VARCHAR, plantwtime VARCHAR, plantwamount VARCHAR, plantimage BLOB)");


        addDBBT = (Button) findViewById(R.id.addDBButton);
        addDBBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(plantControl(pNameET.getText().toString().trim())){
                    try {
                        mSQLiteHelper.insertData2(
                                plantTableName,
                                pNameET.getText().toString().trim(),
                                d1ET.getText().toString().trim(),
                                d2ET.getText().toString().trim(),
                                imageViewToByte(pImageView)
                        );
                        Toast.makeText(PlantChooserActivity.this,"Added successfully",Toast.LENGTH_SHORT).show();
                        pNameET.setText("");
                        d1ET.setText("");
                        d2ET.setText("");
                        pImageView.setImageResource(R.drawable.add_image);
                        startActivity(new Intent(PlantChooserActivity.this,PlantListActivity.class));

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                else{
                    showToast("This plant already exist");
                }



            }
        });

        pImageView = (ImageView) findViewById(R.id.plantImageView);


        pImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showPickImageDialog(PlantChooserActivity.this);

            }
        });


















    }

    private void showPickImageDialog(Activity activity) {
        int file = 0x1F4C1;
        int picture = 0x1F5BC;
        int camera = 0x1F4F7;
        String f = String.valueOf(Character.toChars(file));
        String p = String.valueOf(Character.toChars(picture));
        String c = String.valueOf(Character.toChars(camera));
        String item0 = f +"  AppGallery -- x --";
        String item1 = c +"  Camera -- x --";
        String item2 = p +"  Gallery";
        CharSequence[] items = {item0, item1, item2};

        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);

        dialog.setTitle("Select Image from");

        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0){
                    // app gallery r.raw files

                }
                if(which==1){
                    // open camera and take picture

                }
                if(which==2){
                    // My pictures -- phone gallery
                    ActivityCompat.requestPermissions(
                            PlantChooserActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_GALLERY
                    );
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public static byte[] imageViewToByte(ImageView mImageView) {
        Bitmap bitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();

        Bitmap smallImage = makeSmallerImage(bitmap,300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG, 50,outputStream);
        byte[] byteArray = outputStream.toByteArray();
        return  byteArray;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_GALLERY){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/*");
                startActivityForResult(gallery,REQUEST_CODE_GALLERY);

            }
            else{

            }
            return;

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK){
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
                pImageView.setImageURI(resultUri);
            }
            else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error = result.getError();

            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public static Bitmap makeSmallerImage(Bitmap image, int maximumSize) {

        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;

        if (bitmapRatio > 1) {
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image,width,height,true);
    }

    private boolean plantControl(String pname) {
        boolean check = true;
        try {
            Cursor cursor = mSQLiteHelper.getData("SELECT * FROM '"+plantTableName+"' WHERE plantname = '"+pname+"'");

            if(cursor.getCount()>0) {
                check = false;
            }
            else{
                check = true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return check;
    }





}
