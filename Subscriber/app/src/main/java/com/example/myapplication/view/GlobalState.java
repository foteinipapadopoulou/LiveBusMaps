package com.example.myapplication.view;

import android.app.Application;
import android.provider.Settings;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class GlobalState extends Application {
    Socket s=null;
    ObjectInputStream in=null;
    ObjectOutputStream out=null;
    public GlobalState(){
        super();
    }
    public GlobalState(Socket s, ObjectInputStream in, ObjectOutputStream out) {
        this.s = s;
        this.in = in;
        this.out = out;
    }

    public Socket getS() {
        return s;
    }

    public void setS(Socket s) {
        this.s = s;
    }

    public ObjectInputStream getIn() {
        return in;
    }

    public void setIn(ObjectInputStream in) {
        this.in = in;
    }

    public ObjectOutputStream getOut() {
        return out;
    }

    public void setOut(ObjectOutputStream out) {
        this.out = out;
    }

}
