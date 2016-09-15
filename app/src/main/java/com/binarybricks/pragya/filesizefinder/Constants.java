package com.binarybricks.pragya.filesizefinder;

/**
 * Created by PRAGYA on 9/14/2016.
 */
public class Constants {

    public static final String mBroadcastScanFinish = "com.binarybricks.pragya.filesizefinder.scanfinish";

    public interface ACTION {
        public static String MAIN_ACTION = "com.binarybricks.pragya.filesizefinder.action.main";
        public static String STARTFOREGROUND_ACTION = "com.binarybricks.pragya.filesizefinder.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.binarybricks.pragya.filesizefinder.stopforeground";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}
