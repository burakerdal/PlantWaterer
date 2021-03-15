package com.example.plantwaterer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.plantwaterer.RegisterPageActivity.mSQLiteHelper;

public class LoginPageActivity extends AppCompatActivity {

    RelativeLayout mainRelativeLayout;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    CardView loginCard;

    TextView loginButtonTV;
    EditText usermailtext;
    EditText passwordtext;
    TextView maintitletext,registerpagetext;
    RadioButton radioLogin;

    SessionManager sessionManager;

    boolean passStatus = false;

    String usname,ussurname,uspass,usmail ;

    public static User currentuser;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);



        sharedPreferences = getSharedPreferences("LOGIN",MODE_PRIVATE);
        editor = sharedPreferences.edit();

        mainRelativeLayout = findViewById(R.id.mainRelativeLayout);

        loginButtonTV =  findViewById(R.id.loginButtonTV);
        usermailtext = (EditText) findViewById(R.id.emailEditText);
        passwordtext = (EditText) findViewById(R.id.passwordEditText);
        maintitletext = (TextView) findViewById(R.id.appNameTextView);
        registerpagetext = (TextView) findViewById(R.id.registerTextView);
        radioLogin = findViewById(R.id.radiobtnLogin);

        loginCard = (CardView) findViewById(R.id.loginCard);

        sessionManager = new SessionManager(getApplicationContext());

        mSQLiteHelper = new SQLiteHelper(this,"PlantWaterer.sqlite",null,1);
        mSQLiteHelper.queryData("CREATE TABLE IF NOT EXISTS user(id INTEGER PRIMARY KEY AUTOINCREMENT, username VARCHAR, usersurname VARCHAR, useremail VARCHAR, userpassword VARCHAR)");

        String mailCheck = getSharedPreferences("LOGIN",MODE_PRIVATE).getString("MAIL","");
        String passCheck = getSharedPreferences("LOGIN",MODE_PRIVATE).getString("PSW","");
        boolean checkBoxStatus = getSharedPreferences("LOGIN",MODE_PRIVATE).getBoolean("BOX",false);

        usermailtext.setText(mailCheck);
        passwordtext.setText(passCheck);
        radioLogin.setChecked(checkBoxStatus);


        registerpagetext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginPageActivity.this, RegisterPageActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });



        passwordtext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!passStatus){
                    passwordtext.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    passwordtext.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_open_half_opacity_black_24dp, 0, 0, 0);
                    passStatus = true;

                }
                else {
                    passwordtext.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    passwordtext.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_outline_half_opacity_black_24dp, 0, 0, 0);
                    passStatus = false;

                }

            }
        });

        loginButtonTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cursor = mSQLiteHelper.getData("SELECT * FROM user WHERE useremail = '"+usermailtext.getText().toString().trim()+"'");

                while (cursor.moveToNext()){
                    int id = cursor.getInt(0);
                    String name = cursor.getString(1);
                    String surname = cursor.getString(2);
                    String email = cursor.getString(3);
                    String password = cursor.getString(4);
                    uspass = password;
                    usname = name;
                    ussurname = surname;
                    usmail = email;
                    //Toast.makeText(getApplicationContext(),"ID: "+id+"\nName:"+name+"\nSurname:"+surname+"\nEmail:"+email+"\nPass:"+password,Toast.LENGTH_SHORT).show();
                }
                if(usermailtext.getText().toString().equals("")){
                    usermailtext.setError("Please enter mail");
                }
                if(passwordtext.getText().toString().equals("")){
                    passwordtext.setError("Please enter password");
                }
                else if(uspass!=null){
                    if(uspass.equals(passwordtext.getText().toString())){
                        if(radioLogin.isChecked()){
                            editor.putString("MAIL",usermailtext.getText().toString());
                            editor.putString("PSW",passwordtext.getText().toString());
                            editor.putBoolean("BOX",radioLogin.isChecked());
                            editor.commit();
                        }
                        else {
                            editor.putString("MAIL","");
                            editor.putString("PSW","");
                            editor.putBoolean("BOX",radioLogin.isChecked());
                            editor.commit();
                        }
                        sessionManager.setLogin(true);
                        sessionManager.setUsername(usmail);
                        startActivity(new Intent(getApplicationContext(),HomePageActivity.class));
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        finish();
                        currentuser = new User(usname,ussurname,usmail,uspass);

                    }
                    else{
                        Toast.makeText(getApplicationContext(),"Wrong password",Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(),"Wrong mail",Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (sessionManager.getLogin()){
            currentuser = new User("usname","ussurname","usmail","uspass");
            startActivity(new Intent(getApplicationContext(),HomePageActivity.class));
            finish();
        }

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }







}
