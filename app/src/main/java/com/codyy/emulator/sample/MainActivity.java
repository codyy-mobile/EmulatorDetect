package com.codyy.emulator.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.codyy.emulator.detect.library.EmulatorDetector;

public class MainActivity extends AppCompatActivity {
    TextView place;
    TextView textView;

    @Override
    protected void onStart() {
        super.onStart();
        EmulatorDetector.getDefault().bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        EmulatorDetector.getDefault().unbind(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        place = (TextView) findViewById(R.id.tv_placeholder);
        textView = (TextView) findViewById(R.id.tv_info);
        textView.postDelayed(new Runnable() {
            @Override
            public void run() {
                place.setVisibility(View.GONE);
                if (EmulatorDetector.getDefault().isEmulator()) {
                    textView.setText("This device is emulator\n" + EmulatorDetector.getDefault().getEmulatorName());
                } else {
                    textView.setText("This device is not emulator\n");
                }
            }
        }, 1000L);
    }

}
