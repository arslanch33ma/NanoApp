package com.example.aarshad.nanoapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by aarshad on 7/20/16.
 */
public class MyDBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 7;
    private static final String DATABASE_NAME = "productDB.db";
    public static final String TABLE_LOCATIONS = "locations";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_UID = "uid";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_LNG = "lng";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_TIME = "time";

    //We need to pass database information along to superclass
    public MyDBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_LOCATIONS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_UID + " TEXT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_LAT + " TEXT, " +
                COLUMN_LNG + " TEXT, " +
                COLUMN_ADDRESS + " TEXT, " +
                COLUMN_TIME + " TEXT " +
                ");";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
        onCreate(db);
    }

    //Adding a new row to the database
    public void insertLocation(LocationInfo locationInfo){
        ContentValues values = new ContentValues();
        values.put(COLUMN_UID, locationInfo.get_uid());
        values.put(COLUMN_NAME, locationInfo.get_name());
        values.put(COLUMN_LAT, locationInfo.get_lat());
        values.put(COLUMN_LNG, locationInfo.get_lng());
        values.put(COLUMN_ADDRESS,locationInfo.get_address());
        values.put(COLUMN_TIME,locationInfo.get_time());
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_LOCATIONS, null, values);
        db.close();
    }

    //Delete a product from the database
    public void deleteData(String uid){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_LOCATIONS + " WHERE " + COLUMN_UID + "=\"" + uid + "\";");
    }

    public String databaseToString(){
        String dbString = "";
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_LOCATIONS + " WHERE 1";

        //Cursor points to a location in your results
        Cursor c = db.rawQuery(query, null);
        //Move to the first row in your results
        c.moveToFirst();

        //Position after the last row means the end of the results
        while (!c.isAfterLast()) {
            if (c.getString(c.getColumnIndex("uid")) != null) {
                dbString += c.getString(c.getColumnIndex("uid"));
                dbString += "  ";
                dbString += c.getString(c.getColumnIndex("lat"));
                dbString += "  ";
                dbString += c.getString(c.getColumnIndex("lng"));
                dbString += "\n";
            }
            c.moveToNext();
        }
        db.close();
        return dbString;
    }

    public Cursor queryName(String signedInID) {
        SQLiteDatabase db = getWritableDatabase();

        String query = "SELECT * FROM " + TABLE_LOCATIONS + " WHERE " + COLUMN_UID + "=\"" + signedInID + "\";" ;

        Cursor c = db.rawQuery(query,null);

        return c;

    }
    public Cursor getData(String signedInID){
        SQLiteDatabase db = getWritableDatabase();

        String query = "SELECT * FROM " + TABLE_LOCATIONS + " WHERE " + COLUMN_UID + "=\"" + signedInID + "\";" ;

        Cursor c = db.rawQuery(query,null);

        return c;
    }




}