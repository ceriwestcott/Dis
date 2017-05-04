package com.example.ceri.twostep_onecheck;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

/**
 * Created by Ceri on 12-Apr-17.
 */

public class ShowGraph extends Activity {

    private final String[] NAMES =  {"","C-overshoot", "C-Recalculate",  "N-Delete","N-Recalculate",""};


    ChevronDatabase chevronDatabase;
    NumericDatabase numericDatabase;
    int numericDelete;
    int numericRecalculate;
    int chevronZeros;
    int chevronRecalculate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_visual);

        numericDatabase = new NumericDatabase(this);
        chevronDatabase = new ChevronDatabase(this);
        getValues();

        GraphView graph = (GraphView)findViewById(R.id.graph);

        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(new DataPoint[]{
                new DataPoint(0,0),
                new DataPoint(1, chevronZeros),
                new DataPoint(2, chevronRecalculate),
                new DataPoint(3, numericDelete),
                new DataPoint(4, numericRecalculate),
                new DataPoint(5, 0),


        });
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
        staticLabelsFormatter.setHorizontalLabels(NAMES);
        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);


        series.setSpacing(50);

        series.setDrawValuesOnTop(true);
        series.setValuesOnTopColor(Color.WHITE);
        graph.addSeries(series);

    }

    void getValues() {
        int  res = chevronDatabase.getTrailingZeros();
        if(res == -1){
            chevronZeros = 0;
        }else{
            chevronZeros = res;
        }



        res = chevronDatabase.getRecalculate();
        if(res ==-1){
            chevronRecalculate = 0;
        }else{
            chevronRecalculate = res;
        }


        res = numericDatabase.getRecalculate();
        if(res == -1){
            numericRecalculate = 0;
        }else{
            numericRecalculate = res;
        }


        res = numericDatabase.getNumericDelete();
        if(res == -1){
            numericDelete = 0;
        }else {
            numericDelete = res;
        }
        }

}


