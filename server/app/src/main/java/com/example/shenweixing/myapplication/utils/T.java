package com.example.shenweixing.myapplication.utils;

import android.content.Context;
import android.widget.Toast;

import com.example.shenweixing.myapplication.MyApplication;

/**
 * 诚志海图
 * Created by ShenWeiXing on 2019/9/10.9:41
 * Description:
 */

public class T {

    private static Toast toast;

    /**
     * 短时间显示Toast
     *
     * @param message
     */
    public static void showShort( CharSequence message) {
        if (null == toast) {
            toast = Toast.makeText(MyApplication.getInstance(), message, Toast.LENGTH_SHORT);
        } else {
            toast.setText(message);
        }
        toast.show();
    }

    /**
     * 短时间显示Toast
     *
     * @param message
     */
    public static void showShort( int message) {
        if (null == toast) {
            toast = Toast.makeText(MyApplication.getInstance(), message, Toast.LENGTH_SHORT);
        } else {
            toast.setText(message);
        }
        toast.show();
    }

    /**
     * 长时间显示Toast
     *
     * @param message
     */
    public static void showLong(Context context, CharSequence message) {
        if (null == toast) {
            toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        } else {
            toast.setText(message);
        }
        toast.show();
    }

    /**
     * 长时间显示Toast
     *
     * @param message
     */
    public static void showLong(Context context, int message) {
        if (null == toast) {
            toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        } else {
            toast.setText(message);
        }
        toast.show();
    }

    /**
     * 自定义显示Toast时间
     *
     * @param message
     * @param duration
     */
    public static void show( CharSequence message, int duration) {
        if (null == toast) {
            toast = Toast.makeText(MyApplication.getInstance(), message, duration);
        } else {
            toast.setText(message);
        }
        toast.show();
    }

    /**
     * 自定义显示Toast时间
     *
     * @param message
     * @param duration
     */
    public static void show( int message, int duration) {
        if (null == toast) {
            toast = Toast.makeText(MyApplication.getInstance(), message, duration);
        } else {
            toast.setText(message);
        }
        toast.show();
    }

    /** Hide the toast, if any. */
    public static void hideToast() {
        if (null != toast) {
            toast.cancel();
        }
    }

}
