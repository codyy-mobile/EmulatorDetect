package com.codyy.emulator.detect.library;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;


import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;

public class EmulatorDetect {

    static {
        System.loadLibrary("native");
    }

    public static boolean detectPhoneNumber(Context context) {
        boolean isEmulator = false;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            String phoneNumber = telephonyManager.getLine1Number();
//			String networkOp = telephonyManager.getNetworkOperator();
//			String simNum = telephonyManager.getSimSerialNumber();
//			System.out.println("Test: " + phoneNumber + "-" + networkOp + "-" + simNum);
            int base = 5554;
            for (int i = 0; i < 16; i++) {
                int suffix = base + i * 2;
                if (phoneNumber.startsWith("1555521") && phoneNumber.endsWith(String.valueOf(suffix))) {
                    isEmulator = true;
                    break;
                }
            }

        }
        return isEmulator;
    }

    public static boolean detectDeviceId(Context context) {
        boolean isEmulator = false;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            String deviceId = telephonyManager.getDeviceId();
            if (deviceId.equals("000000000000000"))
                isEmulator = true;
            else
                isEmulator = false;
        }
        return isEmulator;
    }

    public static boolean detectBattery(Context context) {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, intentFilter);
        int batteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        if (batteryLevel == 50)
            return true;
        else
            return false;
    }

    public static boolean detectWifiMac(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String macAddr = wifiInfo.getMacAddress();
        if (macAddr == null)
            return true;
        else
            return false;
    }

    public static boolean detectBuildField() {
        String brand = Build.BRAND;
        if (brand.equals("generic"))
            return true;
        else
            return false;
    }

    public static boolean detectReflection() {
        boolean isEmulator = false;
       /* try {
            Class<?> systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method getMethod = systemPropertiesClass.getMethod("get", String.class);
            String property = (String) getMethod.invoke(systemPropertiesClass,
                    new Object[]{"ro.product.name"});
            if (property.equals("sdk"))
                isEmulator = true;
            else
                isEmulator = false;
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            try {
                Method method = c.getMethod("get", String.class);
                try {
                    isEmulator = "1".equals(method.invoke(c.newInstance(), "ro.kernel.qemu"));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return isEmulator;
    }

    public static boolean detectBuildPropFile() {
        boolean isEmulator = false;
        String filePath = "/system/build.prop";
        File file = new File(filePath);
        if (file.exists()) {
            String fileContent = readFile(file);
            if (fileContent.contains("ro.product.name=sdk"))
                isEmulator = true;
            else
                isEmulator = false;
        }
        return isEmulator;
    }

    public static boolean detectMonkey() {
        boolean isMonkey = ActivityManager.isUserAMonkey();
        if (isMonkey)
            return true;
        else
            return false;
    }

    public static boolean detectDrivers() {
        File file = new File("/proc/tty/drivers");
        String content = readFile(file);
        if (content.contains("goldfish"))
            return true;
        else
            return false;
    }


    public static boolean detectSpecialFile() {
        File qemu_file = new File("/dev/qemu_pipe");
        if (qemu_file.exists())
            return true;
        else
            return false;
    }

    public static boolean detectRuntime() {
        boolean isEmulator = false;
        Runtime runtime = Runtime.getRuntime();
        DataInputStream dataInputStream = null;
        try {
            Process process = runtime.exec("getprop");
            dataInputStream = new DataInputStream(process.getInputStream());

            process.waitFor();

            byte[] buffer = new byte[dataInputStream.available()];
            dataInputStream.read(buffer);
            String res = new String(buffer);
            if (res.contains("[ro.product.device]: [generic]"))
                isEmulator = true;
            else
                isEmulator = false;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (dataInputStream != null) {
            try {
                dataInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return isEmulator;
    }


    @SuppressLint("SdCardPath")
    public static boolean detectGetPropWithNative() {
        boolean isEmulator = false;
        detectGetprop();

        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        File file = new File("/data/data/com.codyy.emulator.detect.library/getprop_out.txt");
        if (file.exists()) {
            String content = readFile(file);
            if (content.contains("[ro.product.name]: [sdk]"))
                isEmulator = true;
            else
                isEmulator = false;
        } else
            isEmulator = false;
        return isEmulator;
    }

    public static boolean detectGetpropDirectlyWithNative() {
        if (detectGetpropDirectly())
            return true;
        else
            return false;
    }


    private static String readFile(File file) {
        String fileContent = "";

        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferReader = null;

        try {
            fileInputStream = new FileInputStream(file.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return fileContent;
        }

        inputStreamReader = new InputStreamReader(fileInputStream);
        bufferReader = new BufferedReader(inputStreamReader);

        try {
            String dataString = null;
            while ((dataString = bufferReader.readLine()) != null) {
                if (dataString.isEmpty())
                    continue;
                fileContent += dataString;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (bufferReader != null) {
                try {
                    bufferReader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            if (bufferReader != null) {
                try {
                    bufferReader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return fileContent;
    }

    public static boolean detectTaintDroid() {
        boolean isEmulator = false;
        try {
            Class.forName("dalvik.system.Taint");
            isEmulator = true;
        } catch (Exception ex) {
            isEmulator = false;
        }
        return isEmulator;
    }


    public static boolean detectCPUInfo() {
        File file = new File("/proc/cpuinfo");
        String content = readFile(file);
        return content.contains("Goldfish");
    }

    public static boolean detectBluetooth() {
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        return bluetooth == null;

    }

    public static boolean detectSpecialPackage(Context context) {
        // Query directly
        boolean isEmulator = false;
        PackageManager pkgManager = context.getPackageManager();
        try {
            PackageInfo pkgInfo = pkgManager.getPackageInfo("com.example.android.apis", PackageManager.GET_META_DATA);
            isEmulator = true;
        } catch (NameNotFoundException e) {
            isEmulator = false;
        }

        List<PackageInfo> pkgInfos = pkgManager.getInstalledPackages(PackageManager.GET_META_DATA);
        boolean pkgFound = false;
        for (PackageInfo pkgInfo : pkgInfos) {
            if (pkgInfo.packageName.equals("com.example.android.apis")) {
                pkgFound = true;
                break;
            }
        }
        isEmulator = pkgFound;

        return isEmulator;
    }


    public static boolean detectContacts(Context context) {
        Cursor cur = context.getContentResolver().
                query(ContactsContract.Contacts.CONTENT_URI,
                        null, null, null, null);
        int num = 0;
        if (cur != null) {
            num = cur.getCount();
            cur.close();
        }

        return num == 0;
    }

    public static boolean detectFileWithNative() {
        return detectFileExists();
    }

    public native static int detectGetprop();

    public native static boolean detectGetpropDirectly();

    public native static boolean detectFileExists();

    public native static String getPathFromFd(int pid, int fd);


    public static void testPath() {
        System.out.println(getPathFromFd(Binder.getCallingPid(), 15));
    }

    private static final String mIsEmulator = "Emulator";
    private static final String mIsNoEmulator = "RealDevice";

    public static DeviceInfo emulatorDetect(Context context) {
        int count = 0;
        String info = "";
        if (EmulatorDetect.detectFileWithNative()) {
            info += String.format("detectFileWithNative: %s", mIsEmulator);
            count++;
        } else {
            info += String.format("detectFileWithNative: %s", mIsNoEmulator);
        }
        info += "\n";
        if (EmulatorDetect.detectContacts(context)) {
            info += String.format("detectContacts: %s", mIsEmulator);
            count++;
        } else {
            info += String.format("detectContacts: %s", mIsNoEmulator);
        }
        info += "\n";
        if (EmulatorDetect.detectSpecialPackage(context)) {
            info += String.format("detectSpecialPackage: %s", mIsEmulator);
            count++;
        } else {
            info += String.format("detectSpecialPackage: %s", mIsNoEmulator);
        }
        info += "\n";
        if (EmulatorDetect.detectCPUInfo()) {
            info += String.format("detectCPUInfo: %s", mIsEmulator);
            count++;
        } else {
            info += String.format("detectCPUInfo: %s", mIsNoEmulator);
        }
        info += "\n";
        if (EmulatorDetect.detectBluetooth()) {
            info += String.format("detectBluetooth: %s", mIsEmulator);
            count++;
        } else {
            info += String.format("detectBluetooth: %s", mIsNoEmulator);
        }
        info += "\n";
        if (EmulatorDetect.detectTaintDroid()) {
            info += String.format("detectTaintDroid: %s", mIsEmulator);
            count++;
        } else {
            info += String.format("detectTaintDroid: %s", mIsNoEmulator);
        }
        info += "\n";
        if (EmulatorDetect.detectDeviceId(context)) {
            info += String.format("detectDeviceId: %s", mIsEmulator);
            count++;
        } else {
            info += String.format("detectDeviceId: %s", mIsNoEmulator);
        }
        info += "\n";
        if (EmulatorDetect.detectPhoneNumber(context)) {
            info += String.format("TelephonyManager-PhoneNumber: %s", mIsEmulator);
            count++;
        } else {
            info += String.format("TelephonyManager-PhoneNumber: %s", mIsNoEmulator);
        }
        info += "\n";
        if (EmulatorDetect.detectWifiMac(context)) {
            info += String.format("Wifi-MacAddress: %s", mIsEmulator);
            count++;
        } else {
            info += String.format("Wifi-MacAddress: %s", mIsNoEmulator);
        }
        info += "\n";
        if (EmulatorDetect.detectBuildField()) {
            info += String.format("Build Field: %s", mIsEmulator);
            count++;
        } else {
            info += String.format("Build Field: %s", mIsNoEmulator);
        }
        info += "\n";
        if (EmulatorDetect.detectReflection()) {
            info += String.format("Java Reflection: %s", mIsEmulator);
            count++;
        } else {
            info += String.format("Java Reflection: %s", mIsNoEmulator);
        }
        info += "\n";
        if (EmulatorDetect.detectBuildPropFile()) {
            info += String.format("/system/build.prop: %s", mIsEmulator);
            count++;
        } else {
            info += String.format("/system/build.prop: %s", mIsNoEmulator);
        }
        info += "\n";
        if (EmulatorDetect.detectDrivers()) {
            info += String.format("System Drivers: %s", mIsEmulator);
            count++;
        } else {
            info += String.format("System Drivers: %s", mIsNoEmulator);
        }
        info += "\n";
        if (EmulatorDetect.detectSpecialFile()) {
            info += String.format("/dev/qemu_pipe: %s", mIsEmulator);
            count++;
        } else {
            info += String.format("/dev/qemu_pipe: %s", mIsNoEmulator);
        }
        info += "\n";
        if (EmulatorDetect.detectRuntime()) {
            info += String.format("Runtime Shell: %s", mIsEmulator);
            count++;
        } else {
            info += String.format("Runtime Shell: %s", mIsNoEmulator);
        }
        info += "\n";
        if (EmulatorDetect.detectMonkey()) {
            info += String.format("Monkey: %s", mIsEmulator);
            count++;
        } else {
            info += String.format("Monkey: %s", mIsNoEmulator);
        }
        info += "\n";
        if (EmulatorDetect.detectBattery(context)) {
            info += String.format("Battery Status: %s", mIsEmulator);
            count++;
        } else {
            info += String.format("Battery Status: %s", mIsNoEmulator);
        }
        info += "\n";
        if (EmulatorDetect.detectGetPropWithNative()) {
            info += String.format("Native Code-getprop: %s", mIsEmulator);
            count++;
        } else {
            info += String.format("Native Code-getprop: %s", mIsNoEmulator);
        }
        info += "\n";
        if (EmulatorDetect.detectGetpropDirectlyWithNative()) {
            info += String.format("Native Code-__system_property_get: %s", mIsEmulator);
            count++;
        } else {
            info += String.format("Native Code-__system_property_get: %s", mIsNoEmulator);
        }
        info += "\n";
        info += "Build.PRODUCT: " + Build.PRODUCT + "\n" +
                "Build.MANUFACTURER: " + Build.MANUFACTURER + "\n" +
                "Build.BRAND: " + Build.BRAND + "\n" +
                "Build.DEVICE: " + Build.DEVICE + "\n" +
                "Build.MODEL: " + Build.MODEL + "\n" +
                "Build.HARDWARE: " + Build.HARDWARE + "\n" +
                "Build.FINGERPRINT: " + Build.FINGERPRINT + "\n" +
                "Build.TAGS: " + android.os.Build.TAGS + "\n";
        return new DeviceInfo(info, count > 0);
    }
/*"Build.PRODUCT: " + Build.PRODUCT + "\n" +
                "Build.MANUFACTURER: " + Build.MANUFACTURER + "\n" +
                "Build.BRAND: " + Build.BRAND + "\n" +
                "Build.DEVICE: " + Build.DEVICE + "\n" +
                "Build.MODEL: " + Build.MODEL + "\n" +
                "Build.HARDWARE: " + Build.HARDWARE + "\n" +
                "Build.FINGERPRINT: " + Build.FINGERPRINT + "\n" +
                "Build.TAGS: " + android.os.Build.TAGS + "\n" +
                "GL_RENDERER: " + android.opengl.GLES20.glGetString(android.opengl.GLES20.GL_RENDERER) + "\n" +
                "GL_VENDOR: " + android.opengl.GLES20.glGetString(android.opengl.GLES20.GL_VENDOR) + "\n" +
                "GL_VERSION: " + android.opengl.GLES20.glGetString(android.opengl.GLES20.GL_VERSION) + "\n" +
                "GL_EXTENSIONS: " + android.opengl.GLES20.glGetString(android.opengl.GLES20.GL_EXTENSIONS) + "\n";*/
}
