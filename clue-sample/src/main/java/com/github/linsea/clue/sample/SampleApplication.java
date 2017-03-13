package com.github.linsea.clue.sample;

import android.app.Application;

import com.github.linsea.clue.Clue;
import com.github.linsea.clue.ConsoleLog;

public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Clue.addLog(new ConsoleLog()); //init Clue log with default logcat
//        Clue.addLog(new FileLog()); //add a log receiver

        Clue.d("Clue log init");
    }
}
