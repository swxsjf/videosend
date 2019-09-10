package com.example.shenweixing.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.shenweixing.myapplication.utils.BitmapUtils;
import com.example.shenweixing.myapplication.utils.ChatRecord;
import com.example.shenweixing.myapplication.utils.DensityUtil;
import com.example.shenweixing.myapplication.utils.NetworkUtils;
import com.example.shenweixing.myapplication.utils.SystemUtil;
import com.example.shenweixing.myapplication.utils.T;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 诚志海图
 * Created by ShenWeiXing on 2019/9/10.10:09
 * Description:
 */

public class VoideoChatActivity extends AppCompatActivity implements View.OnClickListener{

    private String type;
    private int CameraType = 1;
    private int yuyingPort = 0;
    private int videoPort = 0;
    private int countryPort = 23340;

    private ImageView header, NoRecode, close, close1, NoVoice, other;
    private TextView name, statu;
    private RelativeLayout isagreen, agreen_yes;
    private LinearLayout agreen, not_agreen;
    private SurfaceView own;
    private SurfaceHolder holder;
    private Bitmap bitmap;

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

    private MediaPlayer mediaPlayer;
    private UserMessageBean bean;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voideo_chat);
        init();
        initData();
    }

    /**
     * 初始化控件
     */
    private void init() {
        header = (ImageView) findViewById(R.id.header);//头像
        //NoRecode = (ImageView) findViewById(R.id.NoRecode);
        close = (ImageView) findViewById(R.id.close);//中间挂断
        close1 = (ImageView) findViewById(R.id.close1);//拒绝接听的挂断
        other = (ImageView) findViewById(R.id.other);//显示别人视频的图片控件
        own = (SurfaceView) findViewById(R.id.own);//显示自己的预览的控件
        holder = own.getHolder();//获取surfaceview的holder
        NoVoice = (ImageView) findViewById(R.id.NoVoice);//转换摄像头
        name = (TextView) findViewById(R.id.name);//对方名字
        statu = (TextView) findViewById(R.id.statu);//状态，但是展示没用
        isagreen = (RelativeLayout) findViewById(R.id.isagreen);//来电接听容器
        agreen_yes = (RelativeLayout) findViewById(R.id.agreen_yes);//整个表层控件，隐藏之后就显示视频通话
        agreen = (LinearLayout) findViewById(R.id.agreen);//同意连接
        not_agreen = (LinearLayout) findViewById(R.id.not_agreen);//拒绝连接
        close.setOnClickListener(this);
        close1.setOnClickListener(this);

    }

    /**
     * 初始化数据
     */
    private void initData() {
        MyApplication.list.add(this);
        Intent intent = getIntent();
        if (intent != null) {
            bean = (UserMessageBean) intent.getSerializableExtra("data");
            type = intent.getStringExtra("type");
            try {
                videoPort = 23336;
                yuyingPort = 23337;
                socket1 = new DatagramSocket(23340);
                socket = new DatagramSocket(yuyingPort);//初始化socket
                socket2 = new DatagramSocket(videoPort);
                serverAddress = InetAddress.getByName(bean.getIpAdress());//初始化地址
                handler.sendEmptyMessage(9);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (SocketException e) {
                e.printStackTrace();
            }
            getCotry();
            if (type.equals("1")) { //主动呼叫别人
                statu.setText("等待对方接听...");
                isagreen.setVisibility(View.GONE);
                close1.setVisibility(View.VISIBLE);
                //发送消息到对方
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Socket socket4 = new Socket(bean.getIpAdress(), 23335);
                            ChatRecord chat = new ChatRecord();
                            chat.setIP(NetworkUtils.getWifiIpAddress(VoideoChatActivity.this));
                            chat.setType("3");
                            chat.setContent("发起语音聊天");
                            chat.setSendType(true);
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
                            chat.setSendTime(formatter.format(curDate));
                            Gson gson = new Gson();
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket4.getOutputStream()));
                            String json = gson.toJson(chat);
                            bw.write(json + "\n");
                            bw.flush();
                            BufferedReader bf = new BufferedReader(new InputStreamReader(socket4.getInputStream()));
                            String isReplay = bf.readLine();
                            if (!isReplay.equals("true")) {
                                T.showLong(VoideoChatActivity.this, "对方不方便接听..");
                                stop();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
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
    private void startVideoSend() {
        holder.addCallback(new SurfaceHolder.Callback() {//holder的监听，surfaceview显示就开始返回数据，隐藏不返回数据，方便控制
            @Override
            public void surfaceCreated(SurfaceHolder holder) {//srufaceview创建
                if (Camera.getNumberOfCameras() != 2) {//判断是否两个摄像头，是的话前置，不是的话后置
                    CameraType = 1;
                }
                camera = Camera.open(CameraType);//打开相机，因为要兼容到4.0版本，所有没用camera2
                try {
                    camera.setPreviewDisplay(holder);//设置预览
                    camera.startPreview();//开始返回数据
                    final Camera.Size csize = camera.getParameters().getPreviewSize();//获取返回预览图片的尺寸

                    ViewGroup.LayoutParams params = own.getLayoutParams();
                    params.width = DensityUtil.dip2px(0);
                    params.height = (csize.height * DensityUtil.dip2px(0)) / csize.height + DensityUtil.dip2px(30);
                    own.setLayoutParams(params);//根据尺寸动态设置预览大小，但是比较坑的是返回的大小就变形的，所以调整了一下

                    camera.setPreviewCallback(new Camera.PreviewCallback() {
                        @Override
                        public void onPreviewFrame(final byte[] data, Camera camera) {
                            long time1 = new Date().getTime();
                            if (time1 - time > 100) {//用于控制帧数，数值表示图片与图片之间的间隔
                                time = time1;
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (IsSend) {
                                            IsSend = false;
                                        }
                                        YuvImage image = new YuvImage(data, ImageFormat.NV21, csize.width, csize.height, null);//返回数据byte[]转换为的图片对象
                                        ByteArrayOutputStream by = new ByteArrayOutputStream();
                                        image.compressToJpeg(new Rect(0, 0, csize.width, csize.height), 30, by);//将yuvimage转换为jpeg
                                        byte[] image211 = by.toByteArray();
                                        // 设置矩阵数据
                                        float zoom = 384 / (float) csize.width;

                                        Matrix matrix = new Matrix();
                                        matrix.setScale(zoom, zoom);//设置尺寸压缩大小

                                        // 根据矩阵数据进行新bitmap的创建
                                        Bitmap bitmap1 = BitmapFactory.decodeByteArray(image211, 0, image211.length);//将图片数据转化为bitmap

                                        Bitmap resultBitmap = Bitmap.createBitmap(bitmap1, 0, 0, bitmap1.getWidth(), bitmap1.getHeight(), matrix, true);//按尺寸进行压缩
                                        if (CameraType == 0) {//判断是前置还是后置，后置旋转90度
                                            resultBitmap = BitmapUtils.rotaingImageView(90, resultBitmap);
                                        } else {
                                            if (SystemUtil.getSystemModel().trim().equals("MI 5s Plus")) {//测试过程中就这个手机比较特殊，不知为何，单独列出来，如果后面发现新的，继续加在后面
                                                resultBitmap = BitmapUtils.rotaingImageView(90, resultBitmap);
                                            } else {
                                                resultBitmap = BitmapUtils.rotaingImageView(270, resultBitmap);
                                            }
                                        }
                                        by.reset();//释放资源
                                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                                        resultBitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);//将bitmap转换为byte[],以50的质量转化
                                        byte[] image21 = out.toByteArray();
                                        out.reset();
                                        DatagramPacket packet = new DatagramPacket(image21, image21.length, serverAddress, videoPort);
                                        try {
                                            socket2.send(packet);
                                            Log.e("发送视频成功", image21.length + "----------------------" + serverAddress.toString() + "=====" + videoPort);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            by.close();
                                            out.close();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                            }
                        }
                    });
                } catch (Exception e) {

                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                camera.setDisplayOrientation(getDisplayOrientation()); //设定相机显示方向
                camera.autoFocus(null);//自动对焦
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (holder.getSurface() != null && camera != null) {
                    //holder.removeCallback(this);
                    camera.setPreviewCallback(null);//关闭相机
                    camera.stopPreview();
                    camera.release();//释放资源
                    camera = null;
                }
            }
        });
    }

    // 获取当前窗口管理器显示方向
    private int getDisplayOrientation() {
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int rotation = display.getRotation();//获取当前的屏幕状态
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        Camera.CameraInfo camInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(CameraType, camInfo);
        int result = 0;
        if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {//判断前置还是后置的角度，计算需要旋转的角度，具体原理不知道，代码搬运工
            result = (camInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;
            if (SystemUtil.getSystemModel().trim().equals("MI 5s Plus")) {
                result = (result + 180) % 360;
            }
        } else {
            result = (camInfo.orientation - degrees + 360) % 360;
        }
        return result;
    }

    /**
     * 开始接收视频
     */
    private void startVideoGet() {
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
                            //startVideoGet();//获取视频开始
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

    /**
     * 关闭界面
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            } else if (msg.what == 4) {
                T.showLong(VoideoChatActivity.this, "对方已挂断");
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
                startVideoSend();//开始
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close:
            case R.id.close1:
                stop();
                break;
        }
    }
}
