// Copyright (c) 2016 Soocheol Lee(soocheol.wind@lge.com). All rights reserved.
// Use of this source code is governed by a LGPL ver 3.0 license that can be
// found in the LICENSE file.

package com.laize.e3momslocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class LockerControlReceiver extends BroadcastReceiver {
    static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    public LockerControlReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Intent new_intent = new Intent(context, LockerMainScreenActivity.class);
            new_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(new_intent);
        } else if (intent.getAction().equals(ACTION_SMS_RECEIVED)) {
            //Bundel null check
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }
            //pdu object null check
            Object[] pdusObj = (Object[]) bundle.get("pdus");
            if (pdusObj == null) {
                return;
            }

            String control_key_number = PreferenceManager
                    .getDefaultSharedPreferences(context.getApplicationContext())
                    .getString(String.valueOf(R.string.control_key_number),"");

            String message_for_unlock = PreferenceManager
                    .getDefaultSharedPreferences(context.getApplicationContext())
                    .getString(String.valueOf(R.string.message_for_unlock),"go to unlock");

            if (control_key_number.isEmpty()) {
                return;
            }

            SmsMessage[] smsMessages = new SmsMessage[pdusObj.length];
            for (int i = 0; i < pdusObj.length; i++) {
                smsMessages[i] = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                String address = smsMessages[i].getOriginatingAddress();
                if (address.length() < control_key_number.length()) {
                    continue;
                }

                String match_key_number =
                        address.substring(address.length() - control_key_number.length());

                if (!match_key_number.equals(control_key_number)) {
                    continue;
                }

                String message = smsMessages[i].getMessageBody().toString();
                if (message_for_unlock.equals(message)) {
                    Toast.makeText(context,
                            "Unlocked by key user!!",
                            Toast.LENGTH_LONG).show();
                    int pid = android.os.Process.myPid();
                    android.os.Process.killProcess(pid);
                }
            }
        }
    }
}
