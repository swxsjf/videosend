package com.example.shenweixing.myapplication.activity;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.shenweixing.myapplication.MyApplication;
import com.example.shenweixing.myapplication.R;
import com.example.shenweixing.myapplication.bean.UserMessageBean;
import com.example.shenweixing.myapplication.service.AnswerService;
import com.example.shenweixing.myapplication.utils.ChatRecord;
import com.example.shenweixing.myapplication.utils.FileUtils;
import com.example.shenweixing.myapplication.utils.NetworkUtils;
import com.example.shenweixing.myapplication.utils.T;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean isFinish = true;
    private List<UserMessageBean> list = new ArrayList<>();
    private String name;
    private Button btn;
    private ImageView imageView;
    private DatagramSocket datagramSocket;
    private boolean isReceive = true;
    private String address = "192.168.10.180";
//    private String address = "192.168.11.241";
    private Intent intentserver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
        }


        initData();
        onHide();

        startServer();

    }

    private void startServer() {
        intentserver = new Intent(this, AnswerService.class);
        intentserver.setAction("com.example.shenweixing.myapplication.Service;");
        //Android 5.0之后，隐式调用是除了设置setAction()外，还需要设置setPackage();
        intentserver.setPackage("com.com.example.shenweixing.myapplication");
        startService(intentserver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isReceive){
            receiveVideo();
        }
    }

    /**
     * 初始化数据
     */
    private void initData() {
//        if (NetworkUtils.isWifi(this)) {
            name = getIntent().getStringExtra("name");
            SendMessage();
//            GetMessage();
            GetMessage1();
//            GetMessageFromTcp();
//        } else {
//            T.showLong(this, "当前非WiFi环境");
//        }
    }


    /**
     *
     */
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
                            final Realm relm = Realm.getDefaultInstance();
                            recode.setSendType(false);
                            recode.setIP(socket.getInetAddress().getHostAddress());
                            if (recode.getType().equals("2")) {//图片
                                String picturePath = FileUtils.savePicture(recode.getContent());
                                recode.setContent(picturePath);
                            } else if (recode.getType().equals("3")) {
                                if (MyApplication.list.size() == 0) {
                                    bw.write("true");
                                    bw.flush();

                                    for (int i = 0; i < list.size(); i++) {
                                        if (recode.getIP().equals(list.get(0).getIpAdress())) {
                                            isReceive = false;
                                            PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
                                            //true为打开，false为关闭
                                            boolean ifOpen = powerManager.isScreenOn();
                                            if (!ifOpen){
                                                //把屏幕打开
//                                                Process p = null;
//                                                String cmd = "echo 150 > /sys/class/backlight/backlight/brightness";
//                                                try
//                                                {
//                                                    p = Runtime.getRuntime().exec("su");
//                                                    DataOutputStream os = new DataOutputStream(p.getOutputStream());
//                                                    os.writeBytes(cmd + "\n");
//                                                    os.writeBytes("exit\n");
//                                                    os.flush();
//                                                    p.waitFor();
//                                                } catch (IOException e) {
//                                                    e.printStackTrace();
//                                                } catch (InterruptedException e) {
//                                                    e.printStackTrace();
//                                                }
                                                PowerManager.WakeLock mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "PostLocationService");
                                                mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
                                                mWakeLock.acquire();
                                                mWakeLock.release();

                                                showDialogLock();

                                            }else {
                                                Intent intent1 = new Intent(MainActivity.this, VoideoChatActivity.class);
                                                intent1.putExtra("data", list.get(i));
                                                intent1.putExtra("type", "2");
                                                startActivity(intent1);
                                            }


                                        }
                                    }
                                }
                            } else if (recode.getType().equals("4")) {//语音
                                String voicePath = FileUtils.saveVoice(recode.getContent());
                                recode.setContent(voicePath);
                            }//1286005007
                            relm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    realm.copyToRealm(recode);
                                }
                            });
                            Intent intent = new Intent();
                            intent.setAction("notify");//发送至聊天页面
                            sendBroadcast(intent);
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

    /**
     * 获取在线人员信息
     */
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
                            String ip = NetworkUtils.getWifiIpAddress(MainActivity.this);
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

    /**
     * 获取在线人员信息
     */
    private void GetMessage1() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket socket = new DatagramSocket(23334);
                    while (isFinish) {
                        byte[] data = new byte[1024];
                        DatagramPacket packet = new DatagramPacket(data, data.length);
                        socket.receive(packet);
                        byte[] data1 = new byte[packet.getLength()];
                        System.arraycopy(data, 0, data1, 0, data1.length);
                        String lastData = new String(data1, "UTF-8");
//                        if (!TextUtil.isEmpty(lastData)) {
                        if (!"".equals(lastData)) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("life","onDestroy");
        isFinish = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String ip = NetworkUtils.getWifiIpAddress(MainActivity.this);
                    DatagramSocket socket = new DatagramSocket();
                    String data = "bbb" + "," + "名字";
                    for (int i = 2; i < 255; i++) {
                        ip = ip.substring(0, ip.lastIndexOf(".")) + "." + i;
                        InetAddress address = InetAddress.getByName(ip);//初始化地址
                        DatagramPacket packet = new DatagramPacket(data.getBytes(), data.getBytes().length, address, 23333);
                        socket.send(packet);
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        T.showShort("设备重启");

//        restartService();

//        Tools.restartDevices();
    }

    /**
     * 检测在线人员情况
     */
    private void SendMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String ip = NetworkUtils.getWifiIpAddress(MainActivity.this);
                    DatagramSocket socket = new DatagramSocket();
                    String data = "aaa," + name;
                    while (isFinish) {
                        for (int i = 2; i < 255; i++) {
                            ip = ip.substring(0, ip.lastIndexOf(".")) + "." + i;
                            InetAddress address = InetAddress.getByName(ip);//初始化地址
                            DatagramPacket packet = new DatagramPacket(data.getBytes(), data.getBytes().length, address, 23333);
                            socket.send(packet);
                        }
                        Thread.sleep(10 * 1000);
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("life","onStop");
//        restartService();

    }

    private void restartService() {
        stopService(intentserver);
        Intent intent=new Intent();
        intent.setAction("restartService");
        MainActivity.this.sendBroadcast(intent);
    }

    /**
     * 沉浸式状态栏
     */
    public void onHide() {


        //4.1及以上通用flags组合
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    flags | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    private void initView() {
        btn = (Button) findViewById(R.id.btn);
        imageView = findViewById(R.id.img);

        btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn:

                String text = (String) btn.getText();
                if (text.equals("开启")){
                    connectVoide(true);
                    btn.setText("关闭");
                    imageView.setVisibility(View.VISIBLE);


                }else {
                    connectVoide(false);
                    btn.setText("开启");
                    imageView.setVisibility(View.GONE);

                }


                break;
        }
    }

    private void connectVoide(final boolean flag){
        new Thread(new Runnable() {

            @Override
            public void run() {

                OutputStream os = null;
                BufferedReader br = null;

                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(address,12345),3000);

                    br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    os = socket.getOutputStream();

                    if (flag) {
                        os.write("观看视频\n".getBytes());
                        os.flush();
                    } else {
                        os.write("不看了\n".getBytes());
                        os.flush();
                    }

                    String s = br.readLine();
                    if ("打开".equals(s)){
                        Log.e("aaa", "收到返回消息：" + s);
                        receiveVideo();
                        isReceive = true;
                    }else if ("关闭".equals(s)){
                        Log.e("aaa", "收到返回消息：" + s);
                        disconnect();
                        isReceive = false;
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    if (os != null){
                        try {
                            os.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (br != null){
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }



            }
        }).start();

    }

    

    private void receiveVideo() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    datagramSocket = new DatagramSocket(33333);
                    while (true) {
                        byte[] data = new byte[50*1024];
                        DatagramPacket packet = new DatagramPacket(data, data.length);
                        datagramSocket.receive(packet);
                        Log.e("收到的视频内容", packet.getLength() + "");
                        //bitmap = BitmapFactory.decodeByteArray(packet.getData(), 0, packet.getData().length);
                        final byte[] data1 = new byte[packet.getLength()];
                        System.arraycopy(packet.getData(), 0, data1, 0, packet.getLength());//根据收到的大小进行切割
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap bitmap1 = BitmapFactory.decodeByteArray(data1, 0, data1.length);
                                imageView.setImageBitmap(bitmap1);
                            }
                        });
                        if (!isReceive)
                            break;
                    }

                } catch (SocketException e) {
                    connect();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }).start();

    }

    private void connect() {
        Log.e("aaa","重连");
        disconnect();
        try {
            datagramSocket = new DatagramSocket(33333);
            while (true) {
                byte[] data = new byte[50*1024];
                DatagramPacket packet = new DatagramPacket(data, data.length);
                datagramSocket.receive(packet);
                Log.e("收到的视频内容", packet.getLength() + "");
                //bitmap = BitmapFactory.decodeByteArray(packet.getData(), 0, packet.getData().length);
                final byte[] data1 = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), 0, data1, 0, packet.getLength());//根据收到的大小进行切割
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap1 = BitmapFactory.decodeByteArray(data1, 0, data1.length);
                        imageView.setImageBitmap(bitmap1);
                    }
                });
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void disconnect(){
        if (datagramSocket != null) {
            datagramSocket.disconnect();
            datagramSocket.close();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
