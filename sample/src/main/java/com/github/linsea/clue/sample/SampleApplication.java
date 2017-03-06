package com.github.linsea.clue.sample;

import android.app.Application;

import com.github.linsea.clue.Clue;
import com.github.linsea.clue.ConsoleLog;

/**
 * Created by kexiwei on 2017-03-06.
 * Description:
 */

public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Clue.addLog(new ConsoleLog()); //init Clue log
//        Clue.addLog(new FileLog()); //add a Clue log

        Clue.d("Clue log init");
    }
}
