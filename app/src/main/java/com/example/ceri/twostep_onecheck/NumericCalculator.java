package com.example.ceri.twostep_onecheck;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by Ceri on 06-Jan-17.
 */

public class NumericCalculator extends Activity implements View.OnClickListener{

    private ArrayList<String> prompt;
    private Boolean hasDecimal = false;
    private TextView textTyped;
    private TextView textHint;
    private int counter =0;
    private String chevron_value;
    private String m_trailingZeros;
    private String m_chevronRecalculate;
    private int m_numericRecalculate = 0;
    private int m_clearField = 0;
    private int m_deleteNumber = 0;
    private NumericDatabase numericDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prompt = new ArrayList<String>();
        setContentView(R.layout.activity_numeric);
        numericDatabase = new NumericDatabase(this);
        textHint = (TextView)findViewById(R.id.calculateTextView);
        chevron_value = getIntent().getStringExtra("Value");
        m_trailingZeros = getIntent().getStringExtra("TrailingZeros");
        m_chevronRecalculate = getIntent().getStringExtra("Recalculate");

        prompt.add("Enter bag size in mL");
        prompt.add("Enter Drops per mL");
        prompt.add("Enter time in minutes. Hours * 60 if you do not know the minutes");
        prompt.add("Press Equals");
        textHint.setText(prompt.get(counter));
        init();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch(id){
            case R.id.button_delete:
                if(!textTyped.getText().toString().equals("")){
                    textTyped.setText(textTyped.getText().toString().substring(0,textTyped.length()-1));
                    m_deleteNumber++;
                }

                break;
            case R.id.number_0:
                addToText(0);
                break;
            case R.id.number_1:
                addToText(1);
                break;
            case R.id.number_2:
                addToText(2);
                break;
            case R.id.number_3:
                addToText(3);
                break;
            case R.id.number_4:
                addToText(4);
                break;
            case R.id.number_5:
                addToText(5);
                break;
            case R.id.number_6:
                addToText(6);
                break;
            case R.id.number_7:
                addToText(7);
                break;
            case R.id.number_8:
                addToText(8);
                break;
            case R.id.number_9:
                addToText(9);
                break;
            case R.id.button_decimal:
                if(!hasDecimal) {
                    textTyped.setText(textTyped.getText().toString() + ".");
                    hasDecimal = true;
                }
                break;
            case R.id.button_addition:
                textTyped.setText(textTyped.getText().toString() + "+");
                setPrompt();
                break;
            case R.id.button_division:
                textTyped.setText(textTyped.getText().toString() + "/");
                setPrompt();

                break;
            case R.id.button_minus:
                textTyped.setText(textTyped.getText().toString() + "-");
                setPrompt();

                break;
            case R.id.button_multiply:
                textTyped.setText(textTyped.getText().toString() + "*");
                setPrompt();
                break;
            case R.id.button_equals:
                if(!textTyped.equals("") || !textTyped.equals(null)) {
                    Expression eval = new Expression(textTyped.getText().toString());
                    BigDecimal eval1 = eval.eval();
                    textTyped.setText(eval1.toPlainString() + " Drops per mL");
                    textTyped.setTextSize(20);
                    m_numericRecalculate++;
                    Intent i = new Intent(this, AnswerCompare.class);

                    i.putExtra("Chevron", chevron_value);
                    i.putExtra("Numeric", eval1.toPlainString());
                    numericDatabase.insertData(m_numericRecalculate, m_deleteNumber);
                    startActivity(i);
                }
                break;
            case R.id.button_right_bracket:
                textTyped.setText(textTyped.getText().toString() + ")");
            break;
            case R.id.button_left_bracket:
                textTyped.setText(textTyped.getText().toString() + "(");
                break;
        }
    }

    private void setPrompt() {
        if(counter+1 < prompt.size()){
            counter++;
            textHint.setText(prompt.get(counter));
        }
    }

    private void addToText(int i) {
        textTyped.setText(textTyped.getText().toString() + i);
    }

    private void init() {
        textTyped = (TextView)findViewById(R.id.text_typed);
        Button m_number1 = (Button) findViewById(R.id.number_1);
        m_number1.setOnClickListener(this);

        Button m_number2 = (Button) findViewById(R.id.number_2);
        m_number2.setOnClickListener(this);

        Button m_number3 = (Button)findViewById(R.id.number_3);
        m_number3.setOnClickListener(this);

        Button m_number4 = (Button)findViewById(R.id.number_4);
        m_number4.setOnClickListener(this);

        Button m_number5 = (Button)findViewById(R.id.number_5);
        m_number5.setOnClickListener(this);

        Button m_number6 = (Button)findViewById(R.id.number_6);
        m_number6.setOnClickListener(this);

        Button m_number7 = (Button)findViewById(R.id.number_7);
        m_number7.setOnClickListener(this);

        Button m_number8 = (Button)findViewById(R.id.number_8);
        m_number8.setOnClickListener(this);

        Button m_number9 = (Button)findViewById(R.id.number_9);
        m_number9.setOnClickListener(this);

        Button m_number0 = (Button)findViewById(R.id.number_0);
        m_number0.setOnClickListener(this);

        Button m_decimal = (Button)findViewById(R.id.button_decimal);
        m_decimal.setOnClickListener(this);

        Button m_multiply = (Button)findViewById(R.id.button_multiply);
        m_multiply.setOnClickListener(this);

        Button m_divide = (Button)findViewById(R.id.button_division);
        m_divide.setOnClickListener(this);

        Button m_addition = (Button)findViewById(R.id.button_addition);
        m_addition.setOnClickListener(this);

        Button m_substract = (Button)findViewById(R.id.button_minus);
        m_substract.setOnClickListener(this);

        Button m_equals = (Button)findViewById(R.id.button_equals);
        m_equals.setOnClickListener(this);

        Button m_left_bracket = (Button)findViewById(R.id.button_left_bracket);
        m_left_bracket.setOnClickListener(this);

        Button m_right_bracket = (Button)findViewById(R.id.button_right_bracket);
        m_right_bracket.setOnClickListener(this);

        Button m_buttonDel = (Button)findViewById(R.id.button_delete);
        m_buttonDel.setOnClickListener(this);

    }
}
