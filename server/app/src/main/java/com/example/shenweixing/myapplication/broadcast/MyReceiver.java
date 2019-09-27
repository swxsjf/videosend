package com.example.shenweixing.myapplication.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.shenweixing.myapplication.activity.MainActivity;
import com.example.shenweixing.myapplication.service.AnswerService;

/**
 * 诚志海图
 * Created by ShenWeiXing on 2019/9/12.9:32
 * Description:
 */

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);

            //启动服务
            Intent service = new Intent(context, AnswerService.class);
            context.startService(service);
        }
    }
}


