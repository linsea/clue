package com.github.linsea.clue.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.github.linsea.clue.Clue;
import com.github.linsea.clue.ConsoleLog;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static final String TAG = "Monitor";

    public MainActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Clue.d("onCreate");
        findViewById(R.id.v).setOnClickListener(this);
        findViewById(R.id.d).setOnClickListener(this);
        findViewById(R.id.i).setOnClickListener(this);
        findViewById(R.id.w).setOnClickListener(this);
        findViewById(R.id.e).setOnClickListener(this);
        findViewById(R.id.a).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Clue.it(TAG, "onResume");
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.v) {
            Clue.v("Verbose message, view id=%d", id);
            Clue.vt(TAG, "Verbose message with TAG, TAG=%s, view id=%d", TAG, id);
        } else if (id == R.id.d) {
            Clue.d("Debug message, view id=%d", id);
            Clue.dt(TAG, "Debug message with TAG, TAG=%s, view id=%d", TAG, id);
        } else if (id == R.id.i) {
            Clue.i("Info message, view id=%d", id);
            Clue.it(TAG, "Info message with TAG, TAG=%s, view id=%d", TAG, id);
        } else if (id == R.id.w) {
            Clue.w("Warn message, view id=%d", id);
            Clue.wt(TAG, "Warn message with TAG, TAG=%s, view id=%d", TAG, id);
        } else if (id == R.id.e) {
            Clue.e("Error message, view id=%d", id);
            Clue.et(TAG, "Error message with TAG, TAG=%s, view id=%d", TAG, id);
            Clue.et(TAG, new Throwable("Something Error"), "Error message with TAG, TAG=%s, view id=%d", TAG, id);
        } else if (id == R.id.a) {
            Clue.wtf("Assert message, view id=%d", id);
            Clue.wtft(TAG, "Verbose message with TAG, TAG=%s, view id=%d", TAG, id);
        }
    }
}
