package com.example.plantwaterer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterPageActivity extends AppCompatActivity {

    EditText name;
    EditText surname;
    EditText email;
    EditText password;
    EditText confirmPassword;
    ImageView passwordImage;
    ImageView confirmPassImage;

    TextView registerButtonTV;
    TextView loginText;

    boolean passwordStatus = false;
    boolean confirmpasswordStatus = false;

    public static SQLiteHelper mSQLiteHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);

        loginText = (TextView) findViewById(R.id.loginTextView);
        name = (EditText)findViewById(R.id.nameEditText);
        surname = (EditText)findViewById(R.id.surnameEditText);
        email = (EditText)findViewById(R.id.emailEditText);
        password = (EditText)findViewById(R.id.passwordEditText);
        confirmPassword = (EditText)findViewById(R.id.confirmPasswordEditText);
        passwordImage = (ImageView) findViewById(R.id.passwordImageView);
        confirmPassImage = (ImageView) findViewById(R.id.confirmImageView);
        registerButtonTV = findViewById(R.id.registerButtonTV);

        mSQLiteHelper = new SQLiteHelper(this,"PlantWaterer.sqlite",null,1);

        mSQLiteHelper.queryData("CREATE TABLE IF NOT EXISTS user(id INTEGER PRIMARY KEY AUTOINCREMENT, username VARCHAR, usersurname VARCHAR, useremail VARCHAR, userpassword VARCHAR)");


        passwordImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!passwordStatus){
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    passwordStatus = true;

                }
                else {
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    passwordStatus = false;

                }

            }
        });

        confirmPassImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!confirmpasswordStatus){
                    confirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    confirmpasswordStatus = true;

                }
                else {
                    confirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    confirmpasswordStatus = false;

                }

            }
        });

        registerButtonTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(emailControl(email.getText().toString().trim())){
                    if(password.getText().toString().equals(confirmPassword.getText().toString())){
                        try {
                            mSQLiteHelper.insertData(
                                    name.getText().toString().trim(),
                                    surname.getText().toString().trim(),
                                    email.getText().toString().trim(),
                                    password.getText().toString().trim()
                            );
                            Toast.makeText(RegisterPageActivity.this,"Added successfully",Toast.LENGTH_SHORT).show();
                            name.setText("");
                            surname.setText("");
                            email.setText("");
                            password.setText("");
                            confirmPassword.setText("");
                            Intent i = new Intent(RegisterPageActivity.this, LoginPageActivity.class);
                            startActivity(i);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    else{
                        Toast.makeText(RegisterPageActivity.this,"Dont match passwords",Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });



        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RegisterPageActivity.this, LoginPageActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    private boolean emailControl(String mail) {
        boolean check = true;

        boolean operator = mail.contains("@");

        if(operator){
            try {
                Cursor cursor = mSQLiteHelper.getData("SELECT * FROM user WHERE useremail = '"+mail+"'");

                if(cursor.getCount()>0) {
                    check = false;
                    Toast.makeText(RegisterPageActivity.this,"This mail already exist",Toast.LENGTH_SHORT).show();
                }
                else{
                    check = true;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else{
            check = false;
            Toast.makeText(RegisterPageActivity.this,"Invalid mail address",Toast.LENGTH_SHORT).show();
        }

        return check;
    }
}
