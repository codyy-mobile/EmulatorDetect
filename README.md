# EmulatorDetect

[![](https://jitpack.io/v/CodyyAndroid/EmulatorDetect.svg)](https://jitpack.io/#CodyyAndroid/EmulatorDetect)

## How to
**Step 1. Add the JitPack repository to your build file**

Add it in your root build.gradle at the end of repositories:
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
**Step 2. Add the dependency**
```
dependencies {
	        compile 'com.github.CodyyAndroid:EmulatorDetect:v1.0.4'
	}
```
## Usage

模拟器检测器
```
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.codyy.emulator.detect.library.EmulatorDetectorService;

public class MainActivity extends AppCompatActivity {
    TextView place;
    TextView textView;
    EmulatorDetectorService mService;
    boolean mBound = false;

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, EmulatorDetectorService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            EmulatorDetectorService.LocalBinder binder = (EmulatorDetectorService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        place = (TextView) findViewById(R.id.tv_placeholder);
        textView = (TextView) findViewById(R.id.tv_info);
        textView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mBound && mService.isEmulator()) {
                    textView.setText("This device is emulator\n" + mService.getDeviceInfo(MainActivity.this));
                } else {
                    textView.setText("This device is not emulator\n" + mService.getDeviceInfo(MainActivity.this));
                }
            }
        }, 2000L);
    }

}

```
