package com.example.shenweixing.myapplication.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.shenweixing.myapplication.MyApplication;
import com.example.shenweixing.myapplication.R;
import com.example.shenweixing.myapplication.activity.VoideoChatActivity;
import com.example.shenweixing.myapplication.bean.UserMessageBean;
import com.example.shenweixing.myapplication.utils.ChatRecord;
import com.example.shenweixing.myapplication.utils.NetworkUtils;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * 诚志海图
 * Created by ShenWeiXing on 2019/9/26.14:28
 * Description:
 */

public class AnswerService extends Service {

    private final String TAG = "AnswerService";
    private boolean isFinish = true;
    private List<UserMessageBean> list = new ArrayList<>();
    private String name;
    private String address = "192.168.10.180";
    private IBinder iBinder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //Service被创建时调用
    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate方法被调用");
        super.onCreate();
        isFinish = true;

    }

    private void GetMessageFromTcp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(23335);
                    while (isFinish) {
                        Socket socket = serverSocket.accept();
                        GetMessageOne(socket);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     *
     */
    private void GetMessageOne(final Socket socket) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedReader bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    while (isFinish) {
                        String data = bf.readLine();
                        Gson gson = new Gson();
                        final ChatRecord recode = gson.fromJson(data, ChatRecord.class);
                        if (recode != null) {
                            if (recode.getType().equals("3")){
                                if (MyApplication.list.size() == 0) {
                                    bw.write("true");
                                    bw.flush();

                                    PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
//                                    //true为打开，false为关闭
                                    boolean ifOpen = powerManager.isScreenOn();
                                    if (!ifOpen){
                                        PowerManager.WakeLock mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "PostLocationService");
                                        mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
                                        mWakeLock.acquire();
                                        mWakeLock.release();

                                        showDialogLock();
                                    }else {
                                        Intent intent1 = new Intent(getApplicationContext(), VoideoChatActivity.class);
                                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Service跳转到Activity 要加这个标记
                                        intent1.putExtra("data", address);
                                        intent1.putExtra("type", "2");
                                        startActivity(intent1);
                                    }
                                }
                            }



//                            final Realm relm = Realm.getDefaultInstance();
//                            recode.setSendType(false);
//                            recode.setIP(socket.getInetAddress().getHostAddress());
//                            if (recode.getType().equals("2")) {//图片
//                                String picturePath = FileUtils.savePicture(recode.getContent());
//                                recode.setContent(picturePath);
//                            } else if (recode.getType().equals("3")) {
//                                if (MyApplication.list.size() == 0) {
//                                    bw.write("true");
//                                    bw.flush();
//
////                                    for (int i = 0; i < list.size(); i++) {
////                                        if (recode.getIP().equals(list.get(0).getIpAdress())) {
////                                    isReceive = false;
//                                    PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
//                                    //true为打开，false为关闭
//                                    boolean ifOpen = powerManager.isScreenOn();
//                                    if (!ifOpen){
////                                        把屏幕打开
////                                                Process p = null;
////                                                String cmd = "echo 150 > /sys/class/backlight/backlight/brightness";
////                                                try
////                                                {
////                                                    p = Runtime.getRuntime().exec("su");
////                                                    DataOutputStream os = new DataOutputStream(p.getOutputStream());
////                                                    os.writeBytes(cmd + "\n");
////                                                    os.writeBytes("exit\n");
////                                                    os.flush();
////                                                    p.waitFor();
////                                                } catch (IOException e) {
////                                                    e.printStackTrace();
////                                                } catch (InterruptedException e) {
////                                                    e.printStackTrace();
////                                                }
//                                        PowerManager.WakeLock mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "PostLocationService");
//                                        mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
//                                        mWakeLock.acquire();
//                                        mWakeLock.release();
//
//                                        showDialogLock();
//
//                                    }else {
//                                        Intent intent1 = new Intent(getApplicationContext(), VoideoChatActivity.class);
//                                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Service跳转到Activity 要加这个标记
////                                        intent1.putExtra("data", list.get(i));
//                                        intent1.putExtra("type", "2");
//                                        startActivity(intent1);
//                                    }
//
//
////                                        }
////                                    }
//                                }
//                            } else if (recode.getType().equals("4")) {//语音
//                                String voicePath = FileUtils.saveVoice(recode.getContent());
//                                recode.setContent(voicePath);
//                            }//1286005007
//                            relm.executeTransaction(new Realm.Transaction() {
//                                @Override
//                                public void execute(Realm realm) {
//                                    realm.copyToRealm(recode);
//                                }
//                            });

                        }
                        if (socket.isClosed()) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void GetMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket socket = new DatagramSocket(23333);
                    while (isFinish) {
                        byte[] data = new byte[1024];
                        DatagramPacket packet = new DatagramPacket(data, data.length);
                        socket.receive(packet);
                        byte[] data1 = new byte[packet.getLength()];
                        System.arraycopy(data, 0, data1, 0, data1.length);
                        String lastData = new String(data1, "UTF-8");
//                        if (!TextUtil.isEmpty(lastData)) {
                        if (!"".equals(lastData)) {
                            String ip = NetworkUtils.getWifiIpAddress(getApplicationContext());
                            address = packet.getAddress().getHostAddress();
                            if (!address.equals(ip)) {
                                String[] message = lastData.split(",");
                                if (message[0].equals("aaa")) {
                                    boolean flag = true;
                                    for (int i = 0; i < list.size(); i++) {
                                        if (list.get(i).getIpAdress().equals(address)) {
                                            list.get(i).setName(message[1]);
                                            flag = false;
                                            break;
                                        }
                                    }
                                    if (flag) {
                                        UserMessageBean bean = new UserMessageBean();
                                        bean.setName(message[1]);
                                        bean.setIpAdress(address);
                                        list.add(bean);
                                    }
                                    String data2 = "aaa," + name;
                                    DatagramSocket socket2 = new DatagramSocket();
                                    DatagramPacket packet1 = new DatagramPacket(data2.getBytes(), data2.getBytes().length, packet.getAddress(), 23334);
                                    socket2.send(packet1);
                                    Log.e("回访数据传输", "-------------");
                                } else if (message[0].equals("bbb")) {
                                    for (int i = 0; i < list.size(); i++) {
                                        if (list.get(i).getName().equals(message[1])) {
                                            list.remove(i);

                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }

                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void showDialogLock() {

        Notification.Builder messageNotification = new  Notification.Builder(getApplication());
        messageNotification.setDefaults(Notification.DEFAULT_ALL);
        messageNotification.setAutoCancel(true);
        messageNotification.setSmallIcon(R.drawable.ic_launcher_background);
        Notification noti = messageNotification.build();
        NotificationManager messageNotificatioManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        messageNotificatioManager.notify(101,noti);

        Intent intent = new Intent();
        intent.setClass(getApplicationContext(),VoideoChatActivity.class);
//        intent.putExtra("data", list.get(i));
        intent.putExtra("type", "2");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(intent);


    }


    //Service被启动时调用
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand方法被调用");
        GetMessage();
        GetMessageFromTcp();
        return super.onStartCommand(intent, flags, startId);
    }

    //Service被销毁时调用
    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy方法被调用");
        super.onDestroy();
        isFinish = false;
        Intent intent=new Intent();
        intent.setAction("restartService");
        getApplication().sendBroadcast(intent);
    }

}
