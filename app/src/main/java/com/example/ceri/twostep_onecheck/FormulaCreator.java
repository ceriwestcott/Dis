package com.example.ceri.twostep_onecheck;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Ceri on 10-Jan-17.
 */

public class FormulaCreator extends Activity implements View.OnClickListener{
    FormulaDatabase myDb;
    EditText first_input ;
    EditText second_input;
    EditText third_input;
    CheckBox multiply_hours;
    EditText title_calculation;
    Spinner Calculations;
    Button add_to_database;
    Button formula_back;
    ArrayList<EditText> arrayOfEditTexts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_calculation_activity);
        arrayOfEditTexts = new ArrayList<EditText>();
        myDb = new FormulaDatabase(this);
        first_input = (EditText)findViewById(R.id.first_input_formula);
        second_input = (EditText)findViewById(R.id.second_formula_input);
        third_input = (EditText)findViewById(R.id.third_formula_input);
        title_calculation = (EditText)findViewById(R.id.title_calculation);
        multiply_hours = (CheckBox)findViewById(R.id.multiply_hours);

        arrayOfEditTexts.add(first_input);
        arrayOfEditTexts.add(second_input);
        arrayOfEditTexts.add(third_input);
        arrayOfEditTexts.add(title_calculation);


        formula_back = (Button)findViewById(R.id.formula_back);

        formula_back.setOnClickListener(this);
         add_to_database = (Button)findViewById(R.id.add_calculation);
        add_to_database.setOnClickListener(this);


        loadSpinner();
    }

    private void loadSpinner() {
         Calculations = (Spinner)findViewById(R.id.spinner);
        List<String> m_measurements = Arrays.asList(getResources().getStringArray(R.array.measurements));
        ArrayAdapter<String> measurementAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item, m_measurements);
        Calculations.setAdapter(measurementAdapter);
    }

    @Override
    public void onClick(View view) {
    int id = view.getId();
        if(id == R.id.add_calculation){

           if(checkFields()){
               boolean isInserted = myDb.insertData(
                       first_input.getText().toString(),
                       second_input.getText().toString(),
                       third_input.getText().toString(),
                       multiply_hours.isChecked(),
                       Calculations.getSelectedItem().toString(),
                       title_calculation.getText().toString()
               );

               if(isInserted == true){
                   Toast.makeText(this,"Successfully Added", Toast.LENGTH_SHORT).show();
               }else{
                   Toast.makeText(this,"Error whilst adding to the database", Toast.LENGTH_SHORT).show();

               }
           }

        }else{
            finish();
        }
        Log.d("Cgecj bix", Boolean.toString(multiply_hours.isChecked()));

    }

    private Boolean checkFields() {
        Boolean isEmpty = true;
        for(EditText textField : arrayOfEditTexts){
            if(textField.getText().toString().equals("")){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder
//                Set Title, Message, Icon for the alert
                        .setTitle("Error!")
                        .setMessage("One or more field are empty!")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Okay", null)
                        //Do nothing on no
                        .show();
                isEmpty = false;
                break;
            }
        }
        return isEmpty;
    }
}
