package com.codyy.emulator.detect.library;

/**
 * EmulatorSample
 * Created by lijian on 2017/01/12.
 */

public class DeviceInfo {
    /**
     * 配置信息
     */
    private String info;
    /**
     * 是否是模拟器,true:模拟器,false:真机
     */
    private boolean isEmulator;

    public DeviceInfo(String info, boolean isEmulator) {
        this.info = info;
        this.isEmulator = isEmulator;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public boolean isEmulator() {
        return isEmulator;
    }

    public void setEmulator(boolean emulator) {
        isEmulator = emulator;
    }
}
