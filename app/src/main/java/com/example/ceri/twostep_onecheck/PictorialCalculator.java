package com.example.ceri.twostep_onecheck;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ceri on 05-Feb-17.
 */

public class PictorialCalculator extends Activity implements  View.OnClickListener {
    ArrayList<ImageView> bag_size_images;
    List<Pair> pointer_value;
    int currently_selected = 0;
    TextView text;
    TextView selected_value;

    Integer[] values = {50,100,150,250,275,450,500,1000,2000};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pictorial);
        selected_value = (TextView)findViewById(R.id.current_value);
        getChildren();
        pointer_value.get(currently_selected).setSelected();


    }

    private void getChildren() {
        LinearLayout l = (LinearLayout) findViewById(R.id.pictorial_buttons);
        for (int i = 0; i < l.getChildCount(); i++) {
            l.getChildAt(i).setOnClickListener(this);
        }


        pointer_value = new ArrayList<>();
        TableLayout t = (TableLayout) findViewById(R.id.values_table);
        for (int i = 0; i < t.getChildCount(); i++) {
            View child = t.getChildAt(i);

            if (child instanceof TableRow) {
                TableRow row = (TableRow) child;
                pointer_value.add(new Pair<TextView, TextView>((TextView)row.getChildAt(0), (TextView)row.getChildAt(1)));
            }
        }
        text = (TextView) pointer_value.get(currently_selected).getPointerSpace();
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.change_bag_down:
              if(currently_selected + 1 < pointer_value.size()){
                pointer_value.get(currently_selected).setPointerSpace();
                  currently_selected++;
                  text = pointer_value.get(currently_selected).getPointerSpace();
                  text.setText(">");
                  pointer_value.get(currently_selected).setSelected();
                  selected_value.setText(Integer.toString(values[currently_selected]));

              }
                break;
            case R.id.change_bag_up:
                if(!((currently_selected - 1) < 0)){
                    pointer_value.get(currently_selected).setPointerSpace();
                    currently_selected--;
                    text = pointer_value.get(currently_selected).getPointerSpace();
                    text.setText(">");
                    selected_value.setText(Integer.toString(values[currently_selected]));
                }
                break;
            case R.id.change_unit_up:
                break;
            case R.id.change_unit_down:
                break;
        }

    }


}
