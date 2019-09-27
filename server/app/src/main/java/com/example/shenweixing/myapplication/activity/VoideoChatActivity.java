package com.example.shenweixing.myapplication.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.czht.face.recognition.Czhtdev;
import com.example.shenweixing.myapplication.MyApplication;
import com.example.shenweixing.myapplication.R;
import com.example.shenweixing.myapplication.bean.UserMessageBean;
import com.example.shenweixing.myapplication.utils.T;
import com.example.shenweixing.myapplication.utils.Tools;

import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class VoideoChatActivity extends AppCompatActivity implements View.OnClickListener{
    private String type = "2";
    private int CameraType = 1;
    private int yuyingPort = 0;
    private int videoPort = 0;
    private int countryPort = 23340;
    private static ZMQ.Context context;
    private static ZMQ.Socket requester;

    private ImageView header, NoRecode, close, close1, NoVoice, other;
    private TextView name, statu;
    private RelativeLayout isagreen, agreen_yes;
    private LinearLayout agreen, not_agreen;
    private SurfaceView own;
    private SurfaceHolder holder;
    private Bitmap bitmap;
    private Button openDoor;

    private int frequence = 8000; //录制频率，单位hz.这里的值注意了，写的不好，可能实例化AudioRecord对象的时候，会出错。我开始写成11025就不行。这取决于硬件设备
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private Camera camera;
    private long time = 0;//用于控制帧数

    private DatagramSocket socket;//用于语音通话
    private DatagramSocket socket1;//用于控制是否挂断等
    private DatagramSocket socket2;//用于视频通话
    private InetAddress serverAddress;
    private boolean flagRecode = true;
    private boolean flagPlay = true;
    private boolean IsSend = true;
    private boolean IsStart = true;//是否点击接听，不然十五秒之后会关闭
    private AudioRecord recorder;
    private AudioTrack track;
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;

    private MediaPlayer mediaPlayer;
    private UserMessageBean bean;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_voideo_chat);

        onHide();
        init();
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "PostLocationService");
        initData();

        startZMQ();

        Log.e("life","onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("life","onResume");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("life","onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("life","onStop");
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

    /**
     * 初始化控件
     */
    private void init() {
        header = (ImageView) findViewById(R.id.header);//头像
        close = (ImageView) findViewById(R.id.close);//中间挂断
        close1 = (ImageView) findViewById(R.id.close1);//拒绝接听的挂断
        other = (ImageView) findViewById(R.id.other);//显示别人视频的图片控件
        NoVoice = (ImageView) findViewById(R.id.NoVoice);//转换摄像头
        name = (TextView) findViewById(R.id.name);//对方名字
        statu = (TextView) findViewById(R.id.statu);//状态，但是展示没用
        openDoor = findViewById(R.id.open_door);//开门按钮
        isagreen = (RelativeLayout) findViewById(R.id.isagreen);//来电接听容器
        agreen_yes = (RelativeLayout) findViewById(R.id.agreen_yes);//整个表层控件，隐藏之后就显示视频通话
        agreen = (LinearLayout) findViewById(R.id.agreen);//同意连接
        not_agreen = (LinearLayout) findViewById(R.id.not_agreen);//拒绝连接
        //NoRecode.setOnClickListener(this);
        agreen.setOnClickListener(this);//监听
        not_agreen.setOnClickListener(this);
        close.setOnClickListener(this);
        close1.setOnClickListener(this);
        NoVoice.setOnClickListener(this);
        openDoor.setOnClickListener(this);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        MyApplication.list.add(this);
        Intent intent = getIntent();
        if (intent != null) {
//            bean = (UserMessageBean) intent.getSerializableExtra("data");TODO =====================================================================================================
            type = intent.getStringExtra("type");
            try {
                videoPort = 23336;
                yuyingPort = 23337;
                socket1 = new DatagramSocket(23340);
                socket = new DatagramSocket(yuyingPort);//初始化socket
                socket2 = new DatagramSocket(videoPort);
//                serverAddress = InetAddress.getByName(bean.getIpAdress());//初始化地址
                serverAddress = InetAddress.getByName("192.168.10.180");//初始化地址
//                serverAddress = InetAddress.getByName("192.168.11.241");//初始化地址
                handler.sendEmptyMessage(9);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (SocketException e) {
                e.printStackTrace();
            }
            getCotry();
            if (type.equals("2")) { //被动接收呼叫
                isagreen.setVisibility(View.VISIBLE);
                close1.setVisibility(View.GONE);
                handler.sendEmptyMessageDelayed(2, 10000);////开始录制并发送视频
                mediaPlayer = MediaPlayer.create(this, R.raw.video_voice);//播放来电铃声铃声
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mediaPlayer.reset();
                        mediaPlayer = MediaPlayer.create(VoideoChatActivity.this, R.raw.video_voice);
                        mediaPlayer.start();
                    }
                });
            }
        }

    }

    /**
     * 开始录音并发送
     */
    private void startRecode() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int bufferSize = AudioRecord.getMinBufferSize(frequence, channelConfig, audioEncoding); //获取缓存最小大小
                    recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION , 8000,
                            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);//初始化录音
                    recorder.startRecording();//开始录音
                    while (flagRecode) {
                        byte[] data = new byte[bufferSize];//创建接收音频信号的数组
                        recorder.read(data, 0, data.length);
                        int max = 0;
                        for (int i = 0; i < bufferSize; i++) {//判断最大音量
                            if (Math.abs(data[i]) > max) {
                                max = Math.abs(data[i]);
                            }
                        }
                        if (max > 20) {//音量小了不发送，不同手机这个值不一样，不太好断定
                            DatagramPacket packet1 = new DatagramPacket(data, data.length, serverAddress, yuyingPort);
                            socket.send(packet1);
                            Log.e("发送语音成功", data.length + "--------" + yuyingPort + "=======" + serverAddress.toString());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    /**
     * 开始接收并播放
     */
    private void startPlay() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int bufferSize = AudioRecord.getMinBufferSize(frequence, channelConfig, audioEncoding);
                track = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
                        AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize, AudioTrack.MODE_STREAM);//初始化语音播放控件
                track.play();//开始播放音频
                while (flagPlay) {
                    try {
                        byte[] data = new byte[1024 * 2];//写个初始大小用于接收
                        DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, yuyingPort);
                        socket.receive(packet);
                        Log.e("接收语音成功", "--------" + packet.getLength());
                        byte[] lastData = new byte[packet.getLength()];
                        System.arraycopy(data, 0, lastData, 0, packet.getLength());
                        track.write(lastData, 0, lastData.length);//送到播放器进行播放
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * 开始录制并发送视频
     */


    // 获取当前窗口管理器显示方向


    /**
     * 开始接收视频
     */
    private void startVideoGet() {
        agreen_yes.setVisibility(View.GONE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (flagPlay) {
                    try {
                        if (socket2 != null) {
                            final byte[] data = new byte[50 * 1024];//图片最大10k，但还是写大点，万一收不完岂不尴尬
                            final DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, videoPort);
                            socket2.receive(packet);
                            Log.e("收到的视频内容", packet.getLength() + "");
                            //bitmap = BitmapFactory.decodeByteArray(packet.getData(), 0, packet.getData().length);
                            byte[] data1 = new byte[packet.getLength()];
                            System.arraycopy(packet.getData(), 0, data1, 0, packet.getLength());//根据收到的大小进行切割
                            //final Bitmap bitmap1 = BitmapFactory.decodeByteArray(data1, 0, data1.length);
                            Message message = new Message();
                            message.what = 6;
                            Bundle bundle = new Bundle();
                            bundle.putByteArray("message", data1);
                            message.setData(bundle);
                            handler.sendMessage(message);//通过handler将数据发送显示
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }


    /**
     * 获取控制端口来的消息
     */
    private void getCotry() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (flagPlay) {
                    try {
                        byte[] data = new byte[10 * 1024];
                        DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, countryPort);
                        socket1.receive(packet);
                        byte[] data1 = new byte[packet.getLength()];
                        System.arraycopy(data, 0, data1, 0, data1.length);
                        String lastData = new String(data1);
                        Log.e("控制端口发回来的消息", lastData + " ");
                        if (lastData.equals("1")) {//对方挂断
                            handler.sendEmptyMessage(4);//吐司提示对方挂断，子线程吐司偶尔报错，哈哈
                            stop();//关闭界面
                        } else if (lastData.equals("2")) {//对方同意
                            startRecode();//开始录音发送
                            startPlay();//开始播放录音
                            IsStart = false;
                            handler.sendEmptyMessage(3);//开始发送视频
                            handler.sendEmptyMessage(7);//显示surfaceview，这样才有返回
                            handler.sendEmptyMessage(8);//开始显示对方视频
                        } else if (lastData.equals("3")) {//对方未接听
                            handler.sendEmptyMessage(5);//吐司
                            stop();//关闭界面
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("life","onPause");
//        flagRecode = false;//停止录音线程
//        flagPlay = false;//停止播放录音线程
//        if (recorder != null) {
//            recorder.stop();
//            recorder.release();
//            recorder = null;
//        }
//        if (track != null) {
//            track.stop();
//            track.release();
//            track = null;
//        }
//        if (mediaPlayer != null) {
//            mediaPlayer.release();
//        }

    }

    /**
     * 关闭界面
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("life","onDestroy");
        MyApplication.list.clear();
        flagRecode = false;//停止录音线程
        flagPlay = false;//停止播放录音线程
        if (socket != null) {//通知服务器退出
            socket.close();
        }
        if (socket1 != null) {//关闭socket1
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String str = "1";
                    DatagramPacket packet = new DatagramPacket(str.getBytes(), str.getBytes().length, serverAddress, countryPort);
                    try {
                        socket1.send(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    socket1.close();
                }
            }).start();
        }
        if (socket2 != null) {//通知服务器退出
            socket2.close();
        }
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
        if (track != null) {
            track.stop();
            track.release();
            track = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

    }

    /**
     * 关闭语音聊天通道
     */
    private void stop() {
        finish();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 2) {
                if (IsStart) {
                    stop();
                }
            } else if (msg.what == 3) {
                statu.setText("通话中..");
                openDoor.setVisibility(View.VISIBLE);
            } else if (msg.what == 4) {
                T.showLong(VoideoChatActivity.this, "对方已挂断");
                openDoor.setVisibility(View.GONE);
            } else if (msg.what == 5) {
                T.showLong(VoideoChatActivity.this, "对方未接听");
            } else if (msg.what == 6) {
                byte[] data1 = msg.getData().getByteArray("message");
                Bitmap bitmap1 = BitmapFactory.decodeByteArray(data1, 0, data1.length);
                other.setImageBitmap(bitmap1);
            } else if (msg.what == 7) {
                own.setVisibility(View.VISIBLE);
            } else if (msg.what == 8) {
                agreen_yes.setVisibility(View.GONE);
            } else if (msg.what == 9) {
                //startVideoSend();//开始
                if (!mPowerManager.isScreenOn()) {
                    mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
                    mWakeLock.acquire();
                    mWakeLock.release();
                }
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.agreen:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String str = "2";
                        DatagramPacket packet = new DatagramPacket(str.getBytes(), str.getBytes().length, serverAddress, countryPort);
                        try {
                            socket1.send(packet);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                startRecode();
                startPlay();
                startVideoGet();
                handler.sendEmptyMessage(3);
                IsStart = false;
                agreen_yes.setVisibility(View.GONE);
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
                break;
            case R.id.not_agreen:
                IsStart = false;
                stop();
                break;
            case R.id.close:
            case R.id.close1:
                stop();
                if (socket1 != null) {//关闭socket1
                    //TODO
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String str = "1";
                            DatagramPacket packet = new DatagramPacket(str.getBytes(), str.getBytes().length, serverAddress, countryPort);
                            try {
                                socket1.send(packet);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            socket1.close();
                        }
                    }).start();
                }
                break;
            case R.id.NoVoice:
                if (CameraType == 1) {
                    CameraType = 0;
                } else {
                    CameraType = 1;
                }
                own.setVisibility(View.GONE);
                own.setVisibility(View.VISIBLE);
                break;

            case R.id.open_door:
                try {
                    send(openDoor());
                } catch (Exception e) {
                    Tools.restartDevices();
                }
                break;
        }
    }
    /**
     * 开门
     *
     * @return
     */
    private byte[] openDoor() {
        Czhtdev.Message.Builder message = Czhtdev.Message.newBuilder();
        message.setType(Czhtdev.MessageType.MsgOpenDoor);
        return message.build().toByteArray();
    }

    private void send(final byte[] bytes) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    requester.send(bytes);
                    byte[] b = requester.recv();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            openDoor.setEnabled(true);
                        }
                    });

                    if (b==null){
                        //重新连接
                        startZMQ();
                    }

                }catch (ZMQException e){
                    Log.i("ZMQ",e.toString());
                    startZMQ();
                }

            }
        }).start();

    }

    private void startZMQ() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (requester!=null){
                    requester.close();
                }
                if (context!=null){
                    context.close();
                }
                context = ZMQ.context(1);
                requester = context.socket(ZMQ.REQ);
                requester.setLinger(0);
                requester.setSendTimeOut(5000);
                requester.setReceiveTimeOut(5000);
                requester.connect("tcp:/"+serverAddress+":5000");
            }
        }).start();


    }

}

