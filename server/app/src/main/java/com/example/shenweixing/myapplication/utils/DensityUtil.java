package com.example.shenweixing.myapplication.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * 诚志海图
 * Created by ShenWeiXing on 2019/9/10.9:39
 * Description:
 */

public class DensityUtil {
    private static final String TAG = DensityUtil.class.getSimpleName();

    // 当前屏幕的densityDpi
    private static float dmDensityDpi = 0.0f;
    private static DisplayMetrics dm;
    private static float scale = 0.0f;

    public static int screenWidth;
    public static int screenHeigh;
    /**
     *
     * 根据构造函数获得当前手机的屏幕系数
     *
     * */
    public DensityUtil(Context context) {
        // 获取当前屏幕
        dm = new DisplayMetrics();
        dm = context.getApplicationContext().getResources().getDisplayMetrics();
        // 设置DensityDpi
        setDmDensityDpi(dm.densityDpi);
        // 密度因子
        scale = getDmDensityDpi() / 160;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        screenWidth = wm.getDefaultDisplay().getWidth();
        screenHeigh = wm.getDefaultDisplay().getHeight();
    }


    public static float getDmDensityDpi() {
        return dmDensityDpi;
    }


    public static void setDmDensityDpi(float dmDensityDpi) {
        DensityUtil.dmDensityDpi = dmDensityDpi;
    }

    /**
     * 密度转换像素
     * */
    public static int dip2px(float dipValue) {

        return (int) (dipValue * scale + 0.5f);

    }

    /**
     * 像素转换密度
     * */
    public static int px2dip(float pxValue) {
        return (int) (pxValue / scale + 0.5f);
    }

    @Override
    public String toString() {
        return " dmDensityDpi:" + dmDensityDpi;
    }
}
