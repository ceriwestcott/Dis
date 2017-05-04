package com.example.ceri.twostep_onecheck;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Ceri on 11-Apr-17.
 */

public class NumericDatabase extends SQLiteOpenHelper {

    /**
     * Created by Ceri on 26-Mar-17.
     */


        public static final String DATABASE_NAME = "numeric.db";
        public static final String TABLE_NAME = "numeric_recording";

        public static final String ID = "_ID";
        public static final String FIRST_FIELD = "NUMERIC_RECALCULATE";
        public static final String SECOND_FIELD = "NUMERIC_DELETE";




        public NumericDatabase(Context context) {
            super(context, DATABASE_NAME, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL("create table "+
                    TABLE_NAME + " ("
                    + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + FIRST_FIELD + " INTEGER, "
                    + SECOND_FIELD + " INTEGER )");

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i1) {
            db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME);
        }

    public void deleteAllRecords(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME,null,null);

    }

    public int getRecalculate(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select SUM (" + FIRST_FIELD + ") FROM " + TABLE_NAME, null);
        if(res.moveToNext()){
            return  res.getInt(0);
        }else{
            return -1;
        }
    }

    public int getNumericDelete(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select SUM (" + SECOND_FIELD + ") FROM " + TABLE_NAME, null);
        if(res.moveToNext()){
            return  res.getInt(0);
        }else{
            return -1;
        }

    }


        public boolean insertData(int recalculate, int delete){
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(FIRST_FIELD, recalculate);
            contentValues.put(SECOND_FIELD, delete);


            long result = db.insert(TABLE_NAME, null, contentValues);
            if(result == -1){
                Log.d("Unsuccessful","");
                return false;
            }else{
                Log.d("Successful","");
                return true;
            }
        }

    }







