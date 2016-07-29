package com.example.aarshad.nanoapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


public class History extends AppCompatActivity implements   AdapterView.OnItemClickListener {

    MyDBHandler dbHandler;

    public static final String MyPREFERENCES = "PREF" ;
    public static final String userID = "UserID";
    public static final String CONTENTS_STATUS = "MapContents";

    SharedPreferences sharedPreferences ;
    SharedPreferences.Editor editor ;
    String signedInID ;

    Cursor cursor ;
    SimpleCursorAdapter myCursor;
    ListView lv;
    TextView empty_tv;
    String[] from;
    int[] to;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        signedInID = sharedPreferences.getString(userID,"");

        editor = sharedPreferences.edit();

        dbHandler = new MyDBHandler(this, null, null, 1);

        from = new String[]{ "name","lat","lng", "time", "address" };
        to = new int[]{R.id.tv_historyUsername, R.id.tv_historyLat, R.id.tv_historyLng, R.id.tv_historyTime, R.id.tv_HistoryAddress };
        cursor = dbHandler.queryName(signedInID);
        lv = (ListView) findViewById(R.id.lv_locations);
        lv.setOnItemClickListener(this);

        empty_tv = (TextView) findViewById(android.R.id.empty) ;

        if (cursor.getCount()>0) {
            myCursor = new SimpleCursorAdapter(getBaseContext(), R.layout.row, cursor, from, to);
            lv.setAdapter(myCursor);
            empty_tv.setVisibility(View.INVISIBLE);
        }
        else{
            empty_tv.setVisibility(View.VISIBLE);
        }

        populateListView();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete_history:


                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setMessage("Do you want to delete the Locations History ?");

                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        dbHandler.deleteData(signedInID);
                        cursor = dbHandler.queryName(signedInID);
                        myCursor = new SimpleCursorAdapter(getBaseContext(), R.layout.row, cursor, from, to);
                        lv.setAdapter(myCursor);

                        empty_tv.setVisibility(View.VISIBLE);
                        lv.setEmptyView(empty_tv);

                        editor.putString(CONTENTS_STATUS, "Clear" );
                        editor.commit();

                    }
                });

                alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

            break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void populateListView ()
    {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Double lat = Double.valueOf( ((TextView) view.findViewById(R.id.tv_historyLat)).getText().toString());
        Double lng = Double.valueOf( ((TextView) view.findViewById(R.id.tv_historyLng)).getText().toString());
        String name = ((TextView) view.findViewById(R.id.tv_historyUsername)).getText().toString();
        String time = ((TextView) view.findViewById(R.id.tv_historyTime)).getText().toString();

        Intent intent = new Intent(this, HistoryLocationShow.class);
        Bundle b = new Bundle();
        b.putDouble("lat", lat);
        b.putDouble("lng", lng);
        b.putString("name", name );
        b.putString("time",time);

        intent.putExtras(b);
        startActivity(intent);
    }
}
