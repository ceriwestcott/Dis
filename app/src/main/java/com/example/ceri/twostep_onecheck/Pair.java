package com.example.ceri.twostep_onecheck;

import android.graphics.Color;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by Ceri on 09-Feb-17.
 */

public class Pair<T extends TextView,U extends TextView> {
    private T t;
    private U u;

    public Pair(T t, U u){
        this.t = t;
        this.u = u;
    }

    public T getPointerSpace() {
        return t;
    }

    public void setPointerSpace(){
         t.setText("");
    }

    public U getValue() {
        return u;
    }

    public void setSelected() {
        u.setBackgroundColor(Color.rgb(60,73,87));
    }
}
