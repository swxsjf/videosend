package com.example.shenweixing.myapplication.utils;

import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;

/**
 * 诚志海图
 * Created by ShenWeiXing on 2019/9/10.9:40
 * Description:
 */

public class RecordUtil {

    private static final int SAMPLE_RATE_IN_HZ = 8000;
    private MediaRecorder recorder = new MediaRecorder();
    // 录音的路径
    private String mPath;

    public RecordUtil(String path) {
        mPath = path;
    }

    /**
     * 开始录音
     *
     * @throws IOException
     */
    public void start() throws IOException {
        String state = android.os.Environment.getExternalStorageState();
        if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) {
            throw new IOException("SD Card is not mounted,It is  " + state
                    + ".");
        }
        File directory = new File(mPath).getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Path to file could not be created");
        }
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//		recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);// AAC_ADTS格式在API级别16加入，并只能在Android
//																		// 4.1版本+。
//		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);//
        recorder.setAudioSamplingRate(SAMPLE_RATE_IN_HZ);
        recorder.setOutputFile(mPath);
        recorder.prepare();
        recorder.start();
    }

    /**
     * 结束录音
     *
     * @throws IOException
     */
    public void stop() throws IOException {
        if (recorder != null) {
            try {
                recorder.stop();
            } catch (Exception e) {
                // TODO 如果当前java状态和jni里面的状态不一致，
                //e.printStackTrace();
                recorder = null;
                recorder = new MediaRecorder();
            }
            recorder.release();
            recorder = null;
        }
    }

    /**
     * @return
     */
    public double getAmplitude() {
        if (recorder != null) {
            return (recorder.getMaxAmplitude());
        }
        return 0;
    }

    public void fileDetele(String path) {
        if (!TextUtil.isEmpty(path)) {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        }
    }
}
