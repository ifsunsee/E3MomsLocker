// Copyright (c) 2016 Soocheol Lee(soocheol.wind@lge.com). All rights reserved.
// Use of this source code is governed by a LGPL ver 3.0 license that can be
// found in the LICENSE file.

package com.laize.e3momslocker;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class LockerControlService extends Service {
    private LockerControlReceiver receiver_ = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        receiver_ = new LockerControlReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        filter.addAction(LockerControlReceiver.ACTION_SMS_RECEIVED);
        registerReceiver(receiver_, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);

        if(intent != null){
            if(intent.getAction()==null){
                if(receiver_==null){
                    receiver_ = new LockerControlReceiver();
                    IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
                    filter.addAction(LockerControlReceiver.ACTION_SMS_RECEIVED);
                    registerReceiver(receiver_, filter);
                }
            }
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if(receiver_ != null){
            unregisterReceiver(receiver_);
        }
    }
}
