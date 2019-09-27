package com.example.shenweixing.myapplication.utils;

import android.os.Looper;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 诚志海图
 * Created by ShenWeiXing on 2019/9/12.10:13
 * Description:
 */

public class Tools {

    /**
     *
     * 重启设备
     *
     */
    public static void restartDevices(){

        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                T.showShort("设备例行自检，正准备自动重启");
                Looper.loop();

            }
        }.start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        if (Config.IS_RESTART) {
            //TODO：设备是否重启
//            if (Config.DEVICES_TYPE == 3){
                String cmd = "reboot";
                Process process = null;
                try {
                    process = Runtime.getRuntime().exec("su");
                    DataOutputStream stream = new DataOutputStream(process.getOutputStream());
                    stream.writeBytes(cmd + "\n");
                    stream.writeBytes("exit\n");
                    stream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
//            } else {
//                try {
//                    Process process =Runtime.getRuntime().exec(new String[]{"su","-c","reboot "});
//                    process.waitFor();
//                }catch (Exception ex){
//                    ex.printStackTrace();
//                }
//            }
//        }


    }

}
