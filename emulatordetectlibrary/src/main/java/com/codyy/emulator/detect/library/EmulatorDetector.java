package com.codyy.emulator.detect.library;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;

/**
 * Created by lijian on 2017/6/6.
 */

public class EmulatorDetector {
    private static volatile EmulatorDetector INSTANCE;
    /**
     * emulator service was bounded
     */
    private static volatile boolean bound;

    /**
     * single task
     *
     * @return EmulatorDetector
     */
    public static EmulatorDetector getDefault() {
        if (INSTANCE == null)
            synchronized (EmulatorDetector.class) {
                INSTANCE = new EmulatorDetector();
            }
        return INSTANCE;
    }

    private ServiceConnection mServiceConnection;
    private EmulatorDetectorService mEmulatorDetectorService;

    /**
     * bind emulator detector service
     *
     * @param context context
     */
    public void bind(Context context) {
        if (context == null) return;
        if (bound) return;
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mEmulatorDetectorService = ((EmulatorDetectorService.LocalBinder) service).getService();
                bound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                bound = false;
            }
        };
        Intent intent = new Intent(context, EmulatorDetectorService.class);
        context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Is Emulator
     *
     * @return true:emulator or false:not emulator
     */
    public boolean isEmulator() {
        return mEmulatorDetectorService != null && mEmulatorDetectorService.isEmulator() && !TextUtils.isEmpty(mEmulatorDetectorService.getEmulatorName(mEmulatorDetectorService.getApplicationContext()));
    }

    /**
     * Get emulator name
     *
     * @return emulator name
     */
    public String getEmulatorName() {
        if (mEmulatorDetectorService != null)
            return mEmulatorDetectorService.getEmulatorName(mEmulatorDetectorService.getApplicationContext());
        else {
            return "";
        }
    }

    /**
     * unbind emulator service
     *
     * @param context context
     */
    public void unbind(Context context) {
        if (context == null) return;
        bound = false;
        if (mServiceConnection != null) {
            context.unbindService(mServiceConnection);
        }
        mServiceConnection = null;
        mEmulatorDetectorService = null;
    }

}
