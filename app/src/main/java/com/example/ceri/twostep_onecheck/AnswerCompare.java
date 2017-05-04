package com.example.ceri.twostep_onecheck;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Ceri on 07-Mar-17.
 */

public class AnswerCompare extends Activity implements View.OnClickListener {
    String m_chevronValue;
    String m_numericValue;
    Button reveal;
    Animation annimation;
    ChevronDatabase chevronDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_answers);
        m_chevronValue = getIntent().getStringExtra("Chevron");
        m_numericValue = getIntent().getStringExtra("Numeric");
        reveal = (Button)findViewById(R.id.reveal_answer_button);
        reveal.setOnClickListener(this);
        chevronDatabase  = new ChevronDatabase(this);

        annimation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_in);
    }

    public void onClick(View v){
        int id = v.getId();
        switch (id){
            case R.id.reveal_answer_button:

                TextView chevron = (TextView)findViewById(R.id.chevron_answer);
                TextView numeric = (TextView)findViewById(R.id.numeric_answer);
                TextView choice = (TextView)findViewById(R.id.display_correct);

                if(Double.parseDouble(m_chevronValue) == Double.parseDouble(m_numericValue)){
                    choice.setTextColor(Color.GREEN);
                    choice.setText("Matching Values");
                }else{
                    choice.setText("Values do not Match");
                    choice.setTextColor(Color.RED);
                }

                chevron.setText("Chevron Method - " + m_chevronValue);
                numeric.setText("Numeric Method -" + m_numericValue);
                chevron.startAnimation(annimation);
                numeric.startAnimation(annimation);
                choice.startAnimation(annimation);






        }
    }

}
