package com.binarybricks.pragya.filesizefinder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class FileScanService extends Service {

    private File root;
    private ArrayList<FileProperties> fileList;
    private Map<String, Integer> extensionMap;
    private BigDecimal sum = BigDecimal.ZERO;

    public static final String AVERAGE = "average";
    public static final String TOP_FILES= "TopFiles";
    public static final String TOP_EXTENSION = "TopExtension";

    private static final String LOG_TAG = "ForegroundService";
    public static boolean IS_SERVICE_RUNNING = false;

    private PendingIntent pendingIntent;
    private Intent notificationIntent;
    private NotificationCompat.Builder builder;
    private NotificationManager mNotification;

    @Override
    public void onCreate() {
        super.onCreate();
        mNotification = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Start Foreground Intent ");
            showNotification();
            fetchFilesData();
        } else if (intent.getAction().equals(
                Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    private void showNotification() {
        notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher);

        builder = new NotificationCompat.Builder(this);
        Notification notification = builder.
                setContentTitle(getString(R.string.notification_title)).
                setContentText(getString(R.string.notification_text)).
                setSmallIcon(R.mipmap.ic_launcher).
                setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false)).
                setContentIntent(pendingIntent).
                setOngoing(true).build();
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "In onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    private void fetchFilesData() {
        fileList = new ArrayList<>();
        extensionMap = new HashMap<>();

        //getting SDcard root path
        root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());

        builder.setProgress(3, 0, false);
        mNotification.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, builder.build());
        getFile(root);

        builder.setProgress(3, 1, false);
        mNotification.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, builder.build());
        sortFilesToBiggestSize(fileList);

        builder.setProgress(3, 2, false);
        mNotification.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, builder.build());
        extensionMap = getMostFrequentExtension(extensionMap);
        BigDecimal avg = sum.divide(BigDecimal.valueOf(fileList.size()), 2, RoundingMode.HALF_UP).divide(BigDecimal.valueOf(1024 * 1024), 2, RoundingMode.HALF_UP);

        builder.setProgress(3, 3, false);
        mNotification.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, builder.build());

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(Constants.mBroadcastScanFinish);
        broadcastIntent.putExtra(AVERAGE, avg.toString());
        broadcastIntent.putParcelableArrayListExtra(TOP_FILES, new ArrayList<Parcelable>(fileList.subList(0, 9)));
        broadcastIntent.putParcelableArrayListExtra(TOP_EXTENSION, getTopExtensions());

        builder.setProgress(0, 0, false);
        mNotification.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, builder.build());

        sendBroadcast(broadcastIntent);
        stopSelf();
    }


    /**
     * we will use a HashMap instead of TreeMap, even though we need a sorted map later.
     * This is due to the fact that we need faster lookup time when incrementing the count.
     * Also since the scan can be cancelled in between, sorting the results upfront is costly.
     **/

    private void getFile(File dir) {
        File listFile[] = dir.listFiles();
        if (listFile != null && listFile.length > 0) {
            for (int i = 0; i < listFile.length; i++) {
                //check if this file is a directory
                if (listFile[i].isDirectory()) {
                    getFile(listFile[i]);
                } else {
                    FileProperties fileProperties = new FileProperties();
                    String fileName = listFile[i].getName();
                    fileProperties.setFileName(fileName);
                    int index = fileName.lastIndexOf(".");
                    if (index > 0) {
                        String fileExtension = fileName.substring(index);
                        fileProperties.setFileExtention(fileExtension);

                        if (extensionMap.containsKey(fileExtension)) {
                            extensionMap.put(fileExtension, extensionMap.get(fileExtension) + 1);
                        } else {
                            extensionMap.put(fileExtension, 1);
                        }
                    }
                    fileProperties.setFileSize(listFile[i].length());
                    sum = sum.add(BigDecimal.valueOf(fileProperties.getFileSize()));

                    fileList.add(fileProperties);
                }
            }
        }
    }

    private void sortFilesToBiggestSize(ArrayList<FileProperties> filesList) {
        Collections.sort(filesList, new Comparator<FileProperties>() {
            @Override
            public int compare(FileProperties fp1, FileProperties fp2) {
                return fp2.getFileSize().compareTo(fp1.getFileSize());
            }
        });
    }

    public TreeMap<String, Integer> getMostFrequentExtension(Map<String, Integer> map) {
        Comparator<String> comparator = new ValueComparator(map);
        //TreeMap is a map sorted by its keys.
        //The comparator is used to sort the TreeMap by keys.
        TreeMap<String, Integer> result = new TreeMap<String, Integer>(comparator);
        result.putAll(map);
        return result;
    }

    class ValueComparator implements Comparator<String> {

        Map<String, Integer> map = new HashMap<String, Integer>();

        public ValueComparator(Map<String, Integer> map) {
            this.map.putAll(map);
        }

        @Override
        public int compare(String s1, String s2) {
            if (map.get(s1) >= map.get(s2)) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    private ArrayList<FileProperties> getTopExtensions() {
        //lets reuse the fileproperties since we make it parcelable
        ArrayList<FileProperties> filePropertiesList = new ArrayList<>();
        Iterator it = extensionMap.entrySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            if (i > 4) {
                break;
            }
            i++;
            Map.Entry pair = (Map.Entry) it.next();
            FileProperties fileProperties = new FileProperties();
            fileProperties.setFileExtention(String.valueOf(pair.getKey()));
            fileProperties.setFileSize(Long.valueOf((int) pair.getValue()));
            filePropertiesList.add(fileProperties);
        }
        return filePropertiesList;
    }
}
