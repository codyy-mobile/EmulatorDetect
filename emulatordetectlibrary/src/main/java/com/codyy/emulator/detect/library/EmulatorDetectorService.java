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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lijian on 2017/3/10.
 */

public class EmulatorDetectorService extends Service implements SensorEventListener {
    private final IBinder mIBinder = new LocalBinder();
    private SensorManager mSensorManager;
    private boolean isEmulator = true;
    private List<Float> mFloatsX = new ArrayList<>();
    private List<Float> mFloatsY = new ArrayList<>();
    private List<Float> mFloatsZ = new ArrayList<>();
    private long mCurrentTime = System.currentTimeMillis();

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

    /**
     * 计算方法执行一次
     */
    private int mOnece = 0;

    @Override
    public void onSensorChanged(SensorEvent event) {
        //如果传感器类型为磁场传感器
        if (System.currentTimeMillis() - mCurrentTime <= 500 && event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mFloatsX.add(event.values[0]);
            mFloatsY.add(event.values[1]);
            mFloatsZ.add(event.values[2]);
        } else {
            if (mOnece == 0) {
                mOnece++;
                isEmulator = mFloatsX.size() <= 1 || mFloatsY.size() <= 1 || mFloatsZ.size() <= 1 || isXLinearCorrelation() || isYLinearCorrelation() || isZLinearCorrelation();
                mSensorManager.unregisterListener(this);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mIBinder;
    }

    public class LocalBinder extends Binder {
        public EmulatorDetectorService getService() {
            return EmulatorDetectorService.this;
        }
    }

    /**
     * 获取设备信息
     */
    public String getDeviceInfo(Context context) {
        return getEmulatorName(context) + "\n" + "Build.PRODUCT: " + Build.PRODUCT + "\n" +
                "Build.MANUFACTURER: " + Build.MANUFACTURER + "\n" +
                "Build.BRAND: " + Build.BRAND + "\n" +
                "Build.DEVICE: " + Build.DEVICE + "\n" +
                "Build.MODEL: " + Build.MODEL + "\n" +
                "Build.HARDWARE: " + Build.HARDWARE + "\n" +
                "Build.FINGERPRINT: " + Build.FINGERPRINT;
    }

    public String getEmulatorName(Context context) {
        String sEmulatorName = null;
        final PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> packages = packageManager
                .getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo packageInfo : packages) {
            String packageName = packageInfo.packageName;
            if (packageName.startsWith("com.vphone.") || packageName.startsWith("com.bignox.")) {
                sEmulatorName = context.getString(R.string.emulator_name_yeshen);
            } else if (packageName.startsWith("me.haima.")) {
                sEmulatorName = context.getString(R.string.emulator_name_haimawan);
            } else if (packageName.startsWith("com.bluestacks.")) {
                sEmulatorName = context.getString(R.string.emulator_name_bluestacks);
            } else if (packageName.startsWith("cn.itools.")&&(Build.PRODUCT.startsWith("iToolsAVM")||Build.MANUFACTURER.startsWith("iToolsAVM")||Build.DEVICE.startsWith("iToolsAVM")||Build.MODEL.startsWith("iToolsAVM")||Build.BRAND.startsWith("generic")||Build.HARDWARE.startsWith("vbox86"))) {
                sEmulatorName = context.getString(R.string.emulator_name_itools);
            } else if (packageName.startsWith("com.kop.") || packageName.startsWith("com.kaopu.")) {
                sEmulatorName = context.getString(R.string.emulator_name_tiantian);
            } else if (packageName.startsWith("com.microvirt.")) {
                sEmulatorName = context.getString(R.string.emulator_name_xiaoyao);
            } else if (packageName.equals("com.google.android.launcher.layouts.genymotion")) {
                sEmulatorName = getString(R.string.emulator_name_genymotion);
            }
        }
        if (sEmulatorName == null) {
            ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningServiceInfo> serviceInfos = manager.getRunningServices(30);
            for (ActivityManager.RunningServiceInfo serviceInfo : serviceInfos) {
                String serviceName = serviceInfo.service.getClassName();
                if (serviceName.startsWith("com.bluestacks.")) {
                    sEmulatorName = context.getString(R.string.emulator_name_bluestacks);
                }
            }
        }
        if (sEmulatorName == null && Build.PRODUCT.startsWith("sdk_google")) {
            sEmulatorName = context.getString(R.string.emulator_name_android);
        }
        return sEmulatorName == null ? "" : sEmulatorName;
    }
}
