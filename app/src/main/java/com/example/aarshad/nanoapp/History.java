package com.example.aarshad.nanoapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class History extends AppCompatActivity implements  AdapterView.OnItemLongClickListener {

    MyDBHandler dbHandler;
    ListView listView;

    public static final String MyPREFERENCES = "PREF" ;
    public static final String userID = "UserID";

    SharedPreferences sharedPreferences ;
    String signedInID ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        listView = (ListView) findViewById(R.id.lv_locations);

        listView.setOnItemLongClickListener(this);

        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        signedInID = sharedPreferences.getString(userID,"");

        dbHandler = new MyDBHandler(this, null, null, 1);
        populateListView();

    }

    public void populateListView ()
    {
        Cursor cursor ;
        String[] from = { "uid", "lat", "lng","address" };
        int[] to = {R.id.tv_uid, R.id.tv_lat, R.id.tv_lng, R.id.tv_HistoryAddress };
        cursor = dbHandler.queryName(signedInID);
        SimpleCursorAdapter myCursor = new SimpleCursorAdapter(getBaseContext(),R.layout.row,cursor,from,to);
        ListView lv = (ListView) findViewById(R.id.lv_locations);
        lv.setAdapter(myCursor);
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        String contactId = ((TextView) view.findViewById(R.id.tv_lat)).getText().toString();
        Toast.makeText(getApplicationContext(),"On Item Long Click : " + contactId , Toast.LENGTH_SHORT).show();
        return false;
    }
}
