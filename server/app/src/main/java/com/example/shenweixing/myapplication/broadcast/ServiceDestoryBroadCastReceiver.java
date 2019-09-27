package com.example.shenweixing.myapplication.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.shenweixing.myapplication.service.AnswerService;

/**
 * 诚志海图
 * Created by ShenWeiXing on 2019/9/26.16:13
 * Description:
 */

public class ServiceDestoryBroadCastReceiver extends BroadcastReceiver {

        public ServiceDestoryBroadCastReceiver() {
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("restartService")){
                Intent nIntent=new Intent();
                nIntent.setClass(context, AnswerService.class);
                context.startService(nIntent);
            }
        }
}
