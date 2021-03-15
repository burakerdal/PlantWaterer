package com.example.plantwaterer;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.icu.text.StringSearch;

public class SQLiteHelper extends SQLiteOpenHelper {

    SQLiteHelper(Context context,
                     String name,
                     SQLiteDatabase.CursorFactory factory,
                     int version){
        super (context,name,factory,version);
    }

    public void queryData(String sql){
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL(sql);
    }

    public void insertData(String name,String surname, String email, String password){
        SQLiteDatabase database = getWritableDatabase();
        String sql = "INSERT INTO user VALUES (NULL, ?, ?, ?, ?)";

        SQLiteStatement statement = database.compileStatement(sql);
        statement.clearBindings();

        statement.bindString(1,name);
        statement.bindString(2,surname);
        statement.bindString(3,email);
        statement.bindString(4,password);

        statement.executeInsert();

    }

    public void insertData2(String tableName,String name,String time, String amount, byte[] image){
        SQLiteDatabase database = getWritableDatabase();
        String sql = "INSERT INTO "+tableName+" VALUES (NULL, ?, ?, ?, ?)";

        SQLiteStatement statement = database.compileStatement(sql);
        statement.clearBindings();

        statement.bindString(1,name);
        statement.bindString(2,time);
        statement.bindString(3,amount);
        statement.bindBlob(4,image);

        statement.executeInsert();

    }

    public void updateData(String name,String surname, String email, String password, int id){
        SQLiteDatabase database = getWritableDatabase();
        String sql = "UPDATE user SET username=?, usersurname=?, useremail=?,userpassword=? WHERE id=?";

        SQLiteStatement statement = database.compileStatement(sql);
        statement.clearBindings();

        statement.bindString(1,name);
        statement.bindString(2,surname);
        statement.bindString(3,email);
        statement.bindString(4,password);
        statement.bindDouble(5,(double)id);

        statement.execute();
        database.close();

    }

    public void updateData2(String tableName,String name,String time, String amount, byte[] image, int id){
        SQLiteDatabase database = getWritableDatabase();
        String sql = "UPDATE "+tableName+" SET plantname=?, plantwtime=?, plantwamount=?,plantimage=? WHERE id=?";

        SQLiteStatement statement = database.compileStatement(sql);
        statement.clearBindings();

        statement.bindString(1,name);
        statement.bindString(2,time);
        statement.bindString(3,amount);
        statement.bindBlob(4,image);
        statement.bindDouble(5,(double)id);

        statement.execute();
        database.close();

    }

    public void deleteData(int id){
        SQLiteDatabase database = getWritableDatabase();
        String sql = "DELETE FROM user WHERE id=?";

        SQLiteStatement statement = database.compileStatement(sql);
        statement.clearBindings();
        statement.bindDouble(1,(double)id);

        statement.execute();
        database.close();

    }

    public void deleteData2(String tableName,int id){
        SQLiteDatabase database = getWritableDatabase();
        String sql = "DELETE FROM "+tableName+" WHERE id=?";

        SQLiteStatement statement = database.compileStatement(sql);
        statement.clearBindings();
        statement.bindDouble(1,(double)id);

        statement.execute();
        database.close();

    }

    public Cursor getData (String sql){
        SQLiteDatabase database = getReadableDatabase();
        return database.rawQuery(sql,null);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
