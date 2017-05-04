package com.example.ceri.twostep_onecheck;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Ceri on 12-Jan-17.
 */

public class FormulaChooser extends Activity implements Spinner.OnItemSelectedListener, View.OnClickListener{
    Button toChevron;
    Spinner displayFormulas;
    FormulaDatabase myDb;
    TextView displayTopFormulaContent;
    TextView displayBottomFormulaContent;
    int layout;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_formula);
        myDb = new FormulaDatabase(this);
        layout = Integer.parseInt(getIntent().getStringExtra("layout"));
        Button back = (Button)findViewById(R.id.back);
        back.setOnClickListener(this);

        toChevron= (Button)findViewById(R.id.to_chevron_method);
        toChevron.setOnClickListener(this);

        displayTopFormulaContent = (TextView)findViewById(R.id.display_top_formula);
        displayBottomFormulaContent = (TextView)findViewById(R.id.display_bottom_formula);

        loadSpinner();
    }

    private void loadSpinner() {
        displayFormulas = (Spinner)findViewById(R.id.choose_formula);
        displayFormulas.setOnItemSelectedListener(this);
        Cursor res = myDb.getTitleData();
        if(res.getCount() == 0){

        }
        List<String> formula_titles = new ArrayList<String>();
        while(res.moveToNext()){
            formula_titles.add(res.getString(0));
        }
        ArrayAdapter<String> measurementAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item, formula_titles);
        displayFormulas.setAdapter(measurementAdapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Cursor res = myDb.getAllData(displayFormulas.getSelectedItem().toString());

       while(res.moveToNext()) {
           String multiply = "";
           Log.d("blagg", res.getString(4));
            if(res.getString(4).equals("1")){
                multiply = res.getString(3) + "*60";
            }else{
                multiply = res.getString(3);
            }
           displayTopFormulaContent.setText(
             "(" + res.getString(1) + " * " + res.getString(2)+ ")");
           displayBottomFormulaContent.setText(multiply + "\n\n Unit of Measurement: " + res.getString(5) );
       }


//

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch(id){
            case R.id.to_chevron_method:
                Intent i = new Intent(this, ChevronCalculator.class);
                i.putExtra("layout", Integer.toString(layout));
                i.putExtra("Title", displayFormulas.getSelectedItem().toString());
                startActivity(i);
                break;
            case R.id.back:
                this.finish();
        }
    }
}
