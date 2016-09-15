package com.binarybricks.pragya.filesizefinder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView lvTopFileList;
    private TextView tvAvgFileSize;
    private Button btnStartService;
    private LinearLayout llMostFrequentExtension;
    private LinearLayout llResultContainer;
    private IntentFilter mIntentFilter = new IntentFilter();
    private TopFileListAdapter mAdapter;
    private Intent service;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            llResultContainer.setVisibility(View.VISIBLE);
            if(btnStartService!=null) {
                FileScanService.IS_SERVICE_RUNNING = false;
                btnStartService.setText(R.string.button_start_scan);
            }
            String avgFileSize = intent.getStringExtra(FileScanService.AVERAGE);
            if (!TextUtils.isEmpty(avgFileSize) && tvAvgFileSize!=null){
                tvAvgFileSize.setText(avgFileSize +" MB");
            }
            ArrayList<FileProperties> fileList = intent.getParcelableArrayListExtra(FileScanService.TOP_FILES);

            if(fileList!=null && lvTopFileList!=null) {
                mAdapter = new TopFileListAdapter(MainActivity.this,fileList);
                lvTopFileList.setAdapter(mAdapter);
            }

            ArrayList<FileProperties> fileExtensionList = intent.getParcelableArrayListExtra(FileScanService.TOP_EXTENSION);

            if(fileList!=null && llMostFrequentExtension!=null) {
                LayoutInflater inflator = LayoutInflater.from(MainActivity.this);

                llMostFrequentExtension.removeAllViewsInLayout();
                for (FileProperties fileProperties : fileExtensionList) {
                    View view = inflator.inflate(R.layout.most_frequent_extension_item, null,false);
                    ((TextView) view.findViewById(R.id.tvExtension)).setText(fileProperties.getFileExtention());
                    ((TextView) view.findViewById(R.id.tvNoOfFiles)).setText(fileProperties.getFileSize()+" files"); //it is not filesize but since we are reusing parcelable
                    llMostFrequentExtension.addView(view);
                }
            }
            deregitserBroadcast();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvTopFileList = (ListView) findViewById(R.id.lvTopFileList);
        tvAvgFileSize = (TextView) findViewById(R.id.tvAvgSize);

        llMostFrequentExtension = (LinearLayout) findViewById(R.id.llMostFrequentExtension);
        llResultContainer = (LinearLayout) findViewById(R.id.llResultContainer);

        mIntentFilter.addAction(Constants.mBroadcastScanFinish);
    }

    public void buttonClicked(View v){
        btnStartService = (Button) v;
        service = new Intent(MainActivity.this, FileScanService.class);
        if (!FileScanService.IS_SERVICE_RUNNING){
            service.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
            FileScanService.IS_SERVICE_RUNNING = true;
            llResultContainer.setVisibility(View.GONE);
            btnStartService.setText(R.string.button_stop_scan);
            registerReceiver(mReceiver,mIntentFilter);
        }else {
            deregitserBroadcast();
            service.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
            FileScanService.IS_SERVICE_RUNNING = false;
            btnStartService.setText(R.string.button_start_scan);
        }

        startService(service);
    }

    @Override
    public void onBackPressed() {
        if (service!=null) {
            service.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
            startService(service);
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        deregitserBroadcast();

        super.onDestroy();
    }

    private void deregitserBroadcast() {
        //Since we cannot identify if the reciever is registered or not.
        try {
            unregisterReceiver(mReceiver);
        }
        catch (IllegalArgumentException ile){
            ile.printStackTrace();
        }
    }
}
