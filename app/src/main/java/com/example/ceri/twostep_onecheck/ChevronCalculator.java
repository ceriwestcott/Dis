package com.example.ceri.twostep_onecheck;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Ceri on 10-Dec-16.
 */

public class ChevronCalculator extends Activity implements View.OnClickListener, View.OnFocusChangeListener{

    private int selectedNumber;
    private StringBuilder up;
    EditText n_edit;
    EditText h_edit;
    EditText s_edit;
    EditText currentEdit;
    ArrayList<EditText> editList = new ArrayList<EditText>();
    android.app.AlertDialog.Builder builder;
    private Boolean isCalculated = false;
    FormulaDatabase myDb;
    Cursor res;
    double temp;
    int currentPosition = 1;
    String number;
    SharedPreferences preferences = null;
    String isMulitply;
    String unitOfMeasurement;
    TextView display_text;
    int m_trailingZeros = 0;
    int m_recalculate = 0;
    ChevronDatabase chevronRecordingDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        int layout = Integer.parseInt(getIntent().getStringExtra("layout"));
            if(layout == -1) {
                setContentView(R.layout.activity_chevron);
            } else if( layout == 0 ){
                setContentView(R.layout.activity_chevron_2);

            }else{
                setContentView(R.layout.activity_chevron_3);

            }





        myDb = new FormulaDatabase(this);
         chevronRecordingDatabase = new ChevronDatabase(this);

        Button down_chevron = (Button)findViewById(R.id.down_chevron);
        Button up_chevron = (Button)findViewById(R.id.up_chevron);
        Button right_chevron = (Button)findViewById(R.id.right_chevron);
        Button left_chevron = (Button)findViewById(R.id.left_chevron);
        Button Calculate = (Button)findViewById(R.id.button6);
        Button NextCalculation = (Button)findViewById(R.id.button_calculation_2);




        n_edit = (EditText) findViewById(R.id.n_edit);
         h_edit = (EditText) findViewById(R.id.h_edit);
         s_edit = (EditText) findViewById(R.id.s_edit);
        n_edit.setInputType(InputType.TYPE_NULL);
        h_edit.setInputType(InputType.TYPE_NULL);
        s_edit.setInputType(InputType.TYPE_NULL);
        Intent intent = getIntent();

        String QueryTitle = intent.getStringExtra("Title");
        res = myDb.getAllData(QueryTitle);

        setHints(res, QueryTitle);

        editList.add(n_edit);
        editList.add(h_edit);
        editList.add(s_edit);

        for(int i = 0; i < editList.size(); i++){
            editList.get(i).setOnFocusChangeListener(this);
        }

        down_chevron.setOnClickListener(this);
        right_chevron.setOnClickListener(this);
        up_chevron.setOnClickListener(this);
        left_chevron.setOnClickListener(this);
        Calculate.setOnClickListener(this);
        NextCalculation.setOnClickListener(this);

    }

    private void setHints(Cursor res, String queryTitle) {


        while(res.moveToNext()){
            n_edit.setHint(res.getString(1));
            h_edit.setHint(res.getString(2));
            s_edit.setHint(res.getString(3));
            isMulitply = res.getString(4);
            unitOfMeasurement = res.getString(5);
        }
    }

    public void changeLayout(View v){
        setContentView(R.layout.activity_chevron_2);
    }


    @Override
    protected void onResume() {
        super.onResume();



    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.down_chevron:
                if(!CheckTextExists()){
                    number = currentEdit.getText().toString();
                    up = new StringBuilder(number);
                    selectedNumber = Character.getNumericValue(number.charAt(currentPosition-1));
                    if(selectedNumber - 1 >= 0 ){
                        selectedNumber--;
                    }else{
                        selectedNumber = 9;
                    }
                    up.setCharAt(currentPosition-1, Character.forDigit(selectedNumber, 10));
                    currentEdit.setText(up.toString());
                    currentEdit.setSelection(currentPosition-1,currentPosition);
                }
                break;
            case R.id.up_chevron:
                if(!CheckTextExists()){
                    number = currentEdit.getText().toString();
                    up = new StringBuilder(number);
                    selectedNumber = Character.getNumericValue(number.charAt(currentPosition-1));
                    if(selectedNumber + 1 > 9){
                        selectedNumber = 0;
                    }else{
                        selectedNumber++;
                    }
                    up.setCharAt(currentPosition-1, Character.forDigit(selectedNumber, 10));
                    currentEdit.setText(up.toString());
                    currentEdit.setSelection(currentPosition-1,currentPosition);
                }

                break;
            case R.id.right_chevron:
                number = currentEdit.getText().toString();
                if(!number.equals("")) {

                    char charAtNumber = number.charAt(0);
                    if (number.equals("") || currentPosition+  1 >number.length()) {

                    } else if (charAtNumber == '0') {
                        number = number.substring(1, number.length());
                        m_trailingZeros++;
                        currentEdit.setText(number);
                    }else{
                        currentPosition++;
                        currentEdit.setText(number);
                    }
                    currentEdit.setText(number);
                    currentEdit.setSelection(currentPosition-1,currentPosition);

                }
                break;
            case R.id.left_chevron:
                number = currentEdit.getText().toString();
                leftCheck(number);

                break;
            case R.id.button_calculation_2:
                if(isCalculated) {
                    Intent i = new Intent(this, NumericCalculator.class);
                    i.putExtra("Value", Double.toString(temp));
                    startActivity(i);
                } else{
                    builder = new android.app.AlertDialog.Builder(this);
                    builder
//                Set Title, Message, Icon for the alert
                            .setTitle("Error!")
                            .setMessage("Calculation not complete.")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton("Okay", null)
                            //Do nothing on no
                            .show();
                }
            case R.id.button6:

                 n_edit = (EditText) findViewById(R.id.n_edit);
                 h_edit = (EditText) findViewById(R.id.h_edit);
                 s_edit = (EditText) findViewById(R.id.s_edit);
                if(n_edit.getText().toString().equals("") || h_edit.getText().toString().equals("")
                        || s_edit.getText().toString().equals("")){

                    builder = new android.app.AlertDialog.Builder(
                            this
                    );
                    builder
//                Set Title, Message, Icon for the alert
                            .setTitle("Error!")
                            .setMessage("One or more field are empty!")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton("Okay", null)
                            //Do nothing on no
                            .show();

                }else{
                     display_text = (TextView)findViewById(R.id.display_answer);
                double n_float = Double.parseDouble(n_edit.getText().toString());
                double h_float = Double.parseDouble(h_edit.getText().toString());
                double s_float = Double.parseDouble(s_edit.getText().toString());


                        if(isMulitply.equals("1")){
                             temp = ((n_float * h_float)/s_float*60);
                        }else{
                            temp = ((n_float * h_float)/s_float);
                        }


                    display_text.setText(Double.toString(temp) + unitOfMeasurement);
                    chevronRecordingDatabase.insertData(m_trailingZeros,m_recalculate);
                    m_recalculate++;


                isCalculated = true;

                break;
        }

    }
    }

    private boolean CheckTextExists() {
        String TextExist = currentEdit.getText().toString();
        return TextExist.equals("") ? true:false;
    }

    private void leftCheck(String number) {
        if(currentPosition == 1){
            Log.d("CurrentPosition", Integer.toString(currentPosition));
            number  = 0 + number;
            currentEdit.setText(number);
            currentEdit.setSelection(currentPosition-1, currentPosition);
        }else{
            currentPosition--;
            currentEdit.setSelection(currentPosition-1, currentPosition);
        }
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if(b && view instanceof EditText){
            currentEdit = (EditText) view;
            currentPosition=1;
        }
    }
}
