package com.codyy.emulator.detect.library;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by lijian on 2017/3/10.
 */

public class EmulatorDetectorService extends Service implements SensorEventListener {
    private final IBinder mIBinder = new LocalBinder();
    private SensorManager mSensorManager;
    private boolean isEmulator;
    private boolean isCalc = true;
    private List<Float> mFloatsX = new ArrayList<>();
    private List<Float> mFloatsY = new ArrayList<>();
    private List<Float> mFloatsZ = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        //获取传感器服务
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mSensorManager == null) {
            isEmulator = true;//如果传感器服务获取失败,则判定为传感器
            return;
        }
        //监听磁场传感器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
        startTimer();
    }

    private void startTimer() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                isCalc = false;
                isEmulator = mFloatsX.size() <= 1 || mFloatsY.size() <= 1 || mFloatsZ.size() <= 1 || isXLinearCorrelation() || isYLinearCorrelation() || isZLinearCorrelation();
            }
        }, 1000L);
    }

    /**
     * x轴坐标是否线性相关
     *
     * @return true 线性相关,模拟器,false 非线性相关,真机
     */
    private boolean isXLinearCorrelation() {
        float xAverage;
        float xTotal = 0;
        float xDiffTotal = 0;
        for (Float f : mFloatsX) {
            xTotal += f;
        }
        xAverage = xTotal / mFloatsX.size();
        for (Float f : mFloatsX) {
            xDiffTotal += (f - xAverage);
        }
        return xDiffTotal == 0;
    }

    /**
     * y轴坐标是否线性相关
     *
     * @return true 线性相关,模拟器,false 非线性相关,真机
     */
    private boolean isYLinearCorrelation() {
        float yAverage;
        float yTotal = 0;
        float yDiffTotal = 0;
        for (Float f : mFloatsY) {
            yTotal += f;
        }
        yAverage = yTotal / mFloatsY.size();
        for (Float f : mFloatsY) {
            yDiffTotal += (f - yAverage);
        }
        return yDiffTotal == 0;
    }

    /**
     * z轴坐标是否线性相关
     *
     * @return true 线性相关,模拟器,false 非线性相关,真机
     */
    private boolean isZLinearCorrelation() {
        float zAverage;
        float zTotal = 0;
        float zDiffTotal = 0;
        for (Float f : mFloatsZ) {
            zTotal += f;
        }
        zAverage = zTotal / mFloatsZ.size();
        for (Float f : mFloatsX) {
            zDiffTotal += (f - zAverage);
        }
        return zDiffTotal == 0;
    }

    public boolean isEmulator() {
        return isEmulator;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //如果传感器类型为磁场传感器
        if (isCalc && event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mFloatsX.add(event.values[0]);
            mFloatsY.add(event.values[1]);
            mFloatsZ.add(event.values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mIBinder;
    }

    public class LocalBinder extends Binder {
        public EmulatorDetectorService getService() {
            return EmulatorDetectorService.this;
        }
    }

    private String sEmulatorName;

    /**
     * 获取设备信息
     */
    public String getDeviceInfo(Context context) {
        final PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> packages = packageManager
                .getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo packageInfo : packages) {
            String packageName = packageInfo.packageName;
            if (packageName.startsWith("com.vphone") || packageName.startsWith("com.bignox")) {
                sEmulatorName = "夜神模拟器";
            } else if (packageName.startsWith("me.haima")) {
                sEmulatorName = "海马玩模拟器";
            } else if (packageName.startsWith("com.bluestacks")) {
                sEmulatorName = "BlueStacks模拟器";
            } else if (packageName.startsWith("cn.itools")) {
                sEmulatorName = "iTools 模拟器";
            } else if (packageName.startsWith("com.kop") || packageName.startsWith("com.kaopu")) {
                sEmulatorName = "天天模拟器";
            } else if (packageName.startsWith("com.microvirt")) {
                sEmulatorName = "逍遥模拟器";
            } else if (packageName.equals("com.google.android.launcher.layouts.genymotion")) {
                sEmulatorName = "Genymotion模拟器";
            }
        }
        if (sEmulatorName == null) {
            ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningServiceInfo> serviceInfos = manager.getRunningServices(30);
            for (ActivityManager.RunningServiceInfo serviceInfo : serviceInfos) {
                String serviceName = serviceInfo.service.getClassName();
                if (serviceName.startsWith("com.bluestacks")) {
                    sEmulatorName = "BlueStacks模拟器";
                }
            }
        }
        return (sEmulatorName == null ? "" : sEmulatorName) + "\n" + "Build.PRODUCT: " + Build.PRODUCT + "\n" +
                "Build.MANUFACTURER: " + Build.MANUFACTURER + "\n" +
                "Build.BRAND: " + Build.BRAND + "\n" +
                "Build.DEVICE: " + Build.DEVICE + "\n" +
                "Build.MODEL: " + Build.MODEL + "\n" +
                "Build.HARDWARE: " + Build.HARDWARE + "\n" +
                "Build.FINGERPRINT: " + Build.FINGERPRINT;
    }
}
