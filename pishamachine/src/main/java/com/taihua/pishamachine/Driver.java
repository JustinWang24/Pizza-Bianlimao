package com.taihua.pishamachine;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Justin Wang from SmartBro on 19/12/17.
 */

public class Driver {
    private static final String TAG = Driver.class.getSimpleName();

    private String mDriverName;
    private String mDeviceRoot;

    public Driver(String name, String root) {
        mDriverName = name;
        mDeviceRoot = root;
    }

    public ArrayList<File> getDevices(){
        ArrayList<File> devices = new ArrayList<>();

        File dev = new File("/dev");

        if (!dev.exists()) {
            return devices;
        }
        if (!dev.canRead()) {
            return devices;
        }

        File[] files = dev.listFiles();

        int i;
        for (i = 0; i < files.length; i++) {
            if (files[i].getAbsolutePath().startsWith(mDeviceRoot)) {
                devices.add(files[i]);
            }
        }

        return devices;
    }

    public String getName() {
        return mDriverName;
    }
}
