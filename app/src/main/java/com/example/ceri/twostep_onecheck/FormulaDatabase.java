package com.example.ceri.twostep_onecheck;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Ceri on 10-Jan-17.
 */

public class FormulaDatabase extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "FormulasDatabase.db";
    public static final String TABLE_NAME = "formulas_database";

    public static final String ID = "_ID";
    public static final String TITLE_CALCULATION = "TITLE";
    public static final String FIRST_FIELD = "FIRST_FIELD";
    public static final String SECOND_FIELD = "SECOND_FIELD";
    public static final String THIRD_FIELD = "THIRD_FIELD";
    public static final String MULTIPLY_HOURS = "MULTIPLY_HOURS";
    public static final String MEASUREMENT_TYPE = "MEASUREMENT_TYPE";


    public FormulaDatabase(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table "+
        TABLE_NAME + " ("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + FIRST_FIELD + " TEXT, "
                + SECOND_FIELD + " TEXT ,"
                + THIRD_FIELD + " TEXT, "
                + MULTIPLY_HOURS + " BOOLEAN, "
                + MEASUREMENT_TYPE + " TEXT, "
                + TITLE_CALCULATION +" TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME);
    }

    public boolean insertData(String firstInput, String secondInput, String thirdInput,
                              Boolean isMultiplying, String measurementType, String titleCalculation){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FIRST_FIELD, firstInput);
        contentValues.put(SECOND_FIELD, secondInput);
        contentValues.put(THIRD_FIELD, thirdInput);
        contentValues.put(MULTIPLY_HOURS, isMultiplying);
        contentValues.put(MEASUREMENT_TYPE, measurementType);
        contentValues.put(TITLE_CALCULATION, titleCalculation);

        long result = db.insert(TABLE_NAME, null, contentValues);
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public Cursor getTitleData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select title from " + TABLE_NAME,null);
        return res;
    }

    public Cursor getAllData(String title) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME + " WHERE title = '"+ title + "'", null );

        return res;
    }
}
