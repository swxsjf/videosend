package com.example.shenweixing.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * 诚志海图
 * Created by ShenWeiXing on 2019/9/10.9:40
 * Description:
 */

public class SharePreHelper {
    private SharedPreferences sp;
    private SharedPreferences.Editor edit;

    private SharePreHelper() {
    }

    private static SharePreHelper helper;

    public static SharePreHelper getIns() {
        if (helper == null) {
            helper = new SharePreHelper();
        }
        return helper;
    }

    public void initialize(Context context, String name) {
        if (TextUtil.isValidate(name)) {
            sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        } else {
            sp = PreferenceManager.getDefaultSharedPreferences(context);
        }
        edit = sp.edit();
    }

    /**
     * 利用SharedPreferences储存数据
     * **/
    public void saveShrepreValue(String key, String valuse) {
        edit.putString(key, valuse);
        edit.commit();
    }

    /**
     * 利用SharedPreferences获取储存的数据
     * **/
    public String getShrepreValue(String key, String defaultValue) {
        String logjson = sp.getString(key, defaultValue);
        if (TextUtil.isValidate(logjson)) {
            return logjson;
        }
        return defaultValue;
    }

}
