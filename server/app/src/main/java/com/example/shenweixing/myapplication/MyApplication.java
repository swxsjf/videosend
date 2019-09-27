package com.example.shenweixing.myapplication;

import android.app.Activity;
import android.app.Application;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Environment;

import com.example.shenweixing.myapplication.utils.DensityUtil;
import com.example.shenweixing.myapplication.utils.SharePreHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;

/**
 * 诚志海图
 * Created by ShenWeiXing on 2019/9/10.9:42
 * Description:
 */

public class MyApplication extends Application {
    public static DensityUtil densityUtil;
    public static int SoundId;
    public static SoundPool mSound;
    public static String uuid;//软件的唯一标识，用于检测账号重复登录
    public static String VoicePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "VideoDemo" + File.separator + "voice";
    public static String PicturePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "VideoDemo" + File.separator + "picture";
    public static String compressPic = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "VideoDemo" + File.separator + "compressPic";
    public static String originalPic = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "VideoDemo" + File.separator + "originalPic";
    public static List<Activity> list = new ArrayList<>();

    public static MyApplication myApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        densityUtil = new DensityUtil(getApplicationContext());
        mSound = new SoundPool(1, AudioManager.STREAM_MUSIC, 100);//初始化声音
        SoundId = mSound.load(getApplicationContext(), R.raw.cue_code, 1);//加载声音资源
        SharePreHelper.getIns().initialize(getApplicationContext(), "userInfo");//初始化共享参数存储
        creatDir();
        Realm.init(getApplicationContext());
        uuid = UUID.randomUUID().toString();
        myApplication = this;
    }


    public static MyApplication getInstance(){
        return myApplication;
    }

    /**
     * 创建两个存放录音或者图片的文件夹
     */
    private void creatDir() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file1 = new File(path, "VideoDemo" + File.separator + "picture");
        File file2 = new File(path, "VideoDemo" + File.separator + "voice");
        File file3 = new File(path, "VideoDemo" + File.separator + "compressPic");
        File file4 = new File(path, "VideoDemo" + File.separator + "originalPic");
        if (!file1.exists()) {
            file1.mkdirs();
        }
        if (!file2.exists()) {
            file2.mkdirs();
        }
        if (!file3.exists()) {
            file3.mkdirs();
        }
        if (!file4.exists()) {
            file4.mkdirs();
        }
    }
}
