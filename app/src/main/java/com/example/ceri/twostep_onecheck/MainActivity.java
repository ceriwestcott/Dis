package com.example.ceri.twostep_onecheck;



import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import static android.R.attr.typeface;
import static java.util.Locale.*;
import static java.util.Locale.ENGLISH;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener{
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main_menu);
         button = (Button)findViewById(R.id.button6);
        Button button1 = (Button)findViewById(R.id.button9);
        Button RemoveRecords = (Button)findViewById(R.id.remove_records);
        Button showGraphs = (Button)findViewById(R.id.show_graphs);
        RemoveRecords.setOnClickListener(this);
        showGraphs.setOnClickListener(this);
        button.setOnClickListener(this);
        button1.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        Intent i;
        switch(id){
            case R.id.button6:
                i = new Intent(this, LayoutChooser.class);
                startActivity(i);
                break;
            case R.id.button9:
                i = new Intent(this, FormulaCreator.class);
                startActivity(i);
                break;
            case R.id.remove_records:
                ChevronDatabase chevronRecordingDatabase = new ChevronDatabase(this);
                chevronRecordingDatabase.deleteAllRecords();
                NumericDatabase numericDatabase = new NumericDatabase(this);
                numericDatabase.deleteAllRecords();
                Toast.makeText(this, "Database: Deleted", Toast.LENGTH_SHORT).show();                break;
            case R.id.show_graphs:
                i = new Intent(this, ShowGraph.class);
                startActivity(i);
        }

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        button.setBackgroundColor(782902);
        return false;
    }
}
