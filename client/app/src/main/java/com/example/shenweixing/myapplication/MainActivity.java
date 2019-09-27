package com.example.shenweixing.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.shenweixing.myapplication.utils.ChatRecord;
import com.example.shenweixing.myapplication.utils.FileUtils;
import com.example.shenweixing.myapplication.utils.NetworkUtils;
import com.example.shenweixing.myapplication.utils.T;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener{

    private boolean isFinish = true;
    private List<UserMessageBean> list = new ArrayList<>();
    private String name;
    private final List<Socket> socket3 = new ArrayList<>();
    private Button doorbell;
    private Button phone;
    private InetAddress address;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        doorbell = findViewById(R.id.doorbell);
        phone = findViewById(R.id.phone);
        doorbell.setOnClickListener(this);
        phone.setOnClickListener(this);
        initData();
    }



    /**
     * 初始化数据
     */
    private void initData() {
        if (NetworkUtils.isWifi(this)) {
            name = getIntent().getStringExtra("name");
            SendMessage();
            //GetMessage();
            // GetMessage1();
//            method();
            GetMessageFromTcp();
        } else {
            T.showLong(this, "当前非WiFi环境");
        }
    }

    /**
     *
     */
    private void GetMessageFromTcp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket socket = new ServerSocket(23335);
                    while (isFinish) {
                        Socket socket1 = socket.accept();
                        socket3.clear();
                        socket3.add(socket1);
                        GetMessageOne();
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
    private void GetMessageOne() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket socket2 = socket3.get(0);
                try {
                    BufferedReader bf = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket2.getOutputStream()));
                    while (isFinish) {
                        String data = bf.readLine();
                        Gson gson = new Gson();
                        final ChatRecord recode = gson.fromJson(data, ChatRecord.class);

                        if (recode != null) {
                            final Realm relm = Realm.getDefaultInstance();
                            recode.setSendType(false);
                            recode.setIP(socket2.getInetAddress().getHostAddress());
                            if (recode.getType().equals("2")) {//图片
                                String picturePath = FileUtils.savePicture(recode.getContent());
                                recode.setContent(picturePath);
                            } else if (recode.getType().equals("3")) {
                                if (MyApplication.list.size() == 0) {
                                    bw.write("true");
                                    bw.flush();
                                    for (int i = 0; i < list.size(); i++) {
                                        if (recode.getIP().equals(list.get(i).getIpAdress())) {
                                            Intent intent1 = new Intent(MainActivity.this, VoideoChatActivity.class);
                                            intent1.putExtra("data", list.get(i));
                                            intent1.putExtra("type", "2");
                                            startActivity(intent1);
                                        }
                                    }
                                }
                            } else if (recode.getType().equals("4")) {//语音
                                String voicePath = FileUtils.saveVoice(recode.getContent());
                                recode.setContent(voicePath);
                            }
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
                        if (socket2.isClosed()) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void method(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket("192.168.11.160", 23335);
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
                                        if (recode.getIP().equals(list.get(i).getIpAdress())) {
                                            Intent intent1 = new Intent(MainActivity.this, VoideoChatActivity.class);
                                            intent1.putExtra("data", list.get(i));
                                            intent1.putExtra("type", "2");
                                            startActivity(intent1);
                                        }
                                    }
                                }
                            } else if (recode.getType().equals("4")) {//语音
                                String voicePath = FileUtils.saveVoice(recode.getContent());
                                recode.setContent(voicePath);
                            }
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

    /**
     * 获取在线人员信息
     */


    /**
     * 获取在线人员信息
     */

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                    Log.e("aaa","自动获取的本机地址："+ip);
                    DatagramSocket socket = new DatagramSocket();
                    String data = "aaa," + name;
                    while (isFinish) {
                        for (int i = 2; i < 255; i++) {
                            ip = ip.substring(0, ip.lastIndexOf(".")) + "." + i;
                            //初始化地址
                            address = InetAddress.getByName(ip);
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
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    1);}
        else{

        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.doorbell:
                intent = new Intent(MainActivity.this, VoideoChatActivity.class);
                UserMessageBean userMessageBean = new UserMessageBean();
                userMessageBean.setIpAdress("192.168.11.130");
                intent.putExtra("data",userMessageBean);
                intent.putExtra("type","1");
                startActivity(intent);
                break;
            case R.id.phone:
                intent = new Intent(MainActivity.this, InputName.class);
                startActivity(intent);
                break;
        }
    }
}