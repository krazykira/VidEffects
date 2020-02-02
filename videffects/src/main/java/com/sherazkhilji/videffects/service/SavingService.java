package com.sherazkhilji.videffects.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.ResultReceiver;
import android.util.Log;

import com.sherazkhilji.videffects.interfaces.ConvertResultListener;
import com.sherazkhilji.videffects.interfaces.Filter;
import com.sherazkhilji.videffects.model.Converter;

public class SavingService extends IntentService {

    public static final int EXCEPTION_WHILE_SAVING = 101;
    public static final int SUCCESSFUL_SAVING = 102;

    public static final String PATH = "path";
    public static final String OUT_PATH = "outPath";
    public static final String FILTER = "FILTER";
    public static final String RECEIVER = "RECEIVER";
    public static final String TAG = "SavingService";

    public SavingService() {
        super(SavingService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String path = intent.getStringExtra(PATH);

        if (path == null) {
            Log.e(TAG, "Path to video is empty");
            return;
        }

        String outPath = intent.getStringExtra(OUT_PATH);

        if (outPath == null) {
            Log.e(TAG, "Out path is empty");
            return;
        }

        Filter filter = intent.getParcelableExtra(FILTER);

        if (filter == null) {
            Log.e(TAG, "Filter is empty");
            return;
        }

        final ResultReceiver receiver = intent.getParcelableExtra(RECEIVER);

        if (receiver == null) {
            Log.e(TAG, "Receiver is null");
            return;
        }

        Converter converter = new Converter(path);
        converter.startConverter(filter, outPath, new ConvertResultListener() {
            @Override
            public void onSuccess() {
                receiver.send(SUCCESSFUL_SAVING, null);
            }

            @Override
            public void onFail() {
                receiver.send(EXCEPTION_WHILE_SAVING, null);
            }
        });
    }
}