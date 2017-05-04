package com.example.ceri.twostep_onecheck;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Ceri on 24-Apr-17.
 */

public class LayoutChooser extends AppCompatActivity implements View.OnClickListener {
    Button chevronTest;
    Dialog settingsDialog;
    ArrayList<RadioButton> radioButtonArrayList = new ArrayList<RadioButton>();
    int chosen;
    RadioGroup radioGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_chooser);
         radioGroup = (RadioGroup)findViewById(R.id.radio_Group);
       for(int i = 0; i < radioGroup.getChildCount(); i++){
           radioButtonArrayList.add((RadioButton) radioGroup.getChildAt(i));

       }
      Button confirm = (Button)findViewById(R.id.button_confirm);
        confirm.setOnClickListener(this);

    }





    @Override
    public void onClick(View view) {
        Intent i = new Intent(this, FormulaChooser.class);
        int radio = radioGroup.getCheckedRadioButtonId();
        switch (radio){
            case R.id.radio_text:
                i.putExtra("layout", "-1");                startActivity(i);


                break;
            case R.id.radio_linear:
                i.putExtra("layout", "1");
                startActivity(i);

                break;
            case R.id.radio_pictorial:
                i.putExtra("layout", "0");
                startActivity(i);
                break;
        }
    }
}
