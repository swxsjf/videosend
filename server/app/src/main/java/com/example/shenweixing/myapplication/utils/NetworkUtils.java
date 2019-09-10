package com.example.shenweixing.myapplication.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * 诚志海图
 * Created by ShenWeiXing on 2019/9/10.9:40
 * Description:
 */

public class NetworkUtils {

    public static final String DEFAULT_WIFI_ADDRESS = "00-00-00-00-00-00";
    public static final String WIFI = "Wi-Fi";
    public static final String TWO_OR_THREE_G = "2G/3G";
    public static final String UNKNOWN = "Unknown";

    private static String convertIntToIp(int paramInt) {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "." + (0xFF & paramInt >> 24);
    }

    /**
     * 获取网络类型
     * */
    public static String judgemNetwork(Context pContext) {
        String type = "";
        ConnectivityManager cm = (ConnectivityManager) pContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) {
            type = "0";// 没有网络
        } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            type = "1";// wifi网络
        } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
            int subType = info.getSubtype();
            if (subType == TelephonyManager.NETWORK_TYPE_CDMA || subType == TelephonyManager.NETWORK_TYPE_GPRS || subType == TelephonyManager.NETWORK_TYPE_EDGE) {
                type = "2";// 2g网络
            } else if (subType == 18 || subType == TelephonyManager.NETWORK_TYPE_UMTS || subType == TelephonyManager.NETWORK_TYPE_HSDPA || subType == TelephonyManager.NETWORK_TYPE_EVDO_A || subType == TelephonyManager.NETWORK_TYPE_EVDO_0 || subType == TelephonyManager.NETWORK_TYPE_EVDO_B) {
                type = "3";// 3g网络
            } else if (subType == TelephonyManager.NETWORK_TYPE_LTE) {// LTE是3g到4g的过渡，是3.9G的全球标准
                type = "4"; // 4g网络
            }
        }
        return type;
    }

    /**
     * 获取网络类型
     * */
    public static String judgeNetwork(Context pContext) { // 0wifi,1移动流量
        String type = "";
        ConnectivityManager cm = (ConnectivityManager) pContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) {
            type = "null";// 没有网络
        } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            type = "wifi";// wifi网络
        } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
            int subType = info.getSubtype();
            if (subType == TelephonyManager.NETWORK_TYPE_CDMA || subType == TelephonyManager.NETWORK_TYPE_GPRS || subType == TelephonyManager.NETWORK_TYPE_EDGE) {
                type = "2G";// 2g网络
            } else if (subType == 18 || subType == TelephonyManager.NETWORK_TYPE_UMTS || subType == TelephonyManager.NETWORK_TYPE_HSDPA || subType == TelephonyManager.NETWORK_TYPE_EVDO_A || subType == TelephonyManager.NETWORK_TYPE_EVDO_0 || subType == TelephonyManager.NETWORK_TYPE_EVDO_B) {
                type = "3G";// 3g网络
            } else if (subType == TelephonyManager.NETWORK_TYPE_LTE) {// LTE是3g到4g的过渡，是3.9G的全球标准
                type = "4G"; // 4g网络
            }
        }
        return type;
    }

    /***
     * 获取当前网络类型
     *
     * @param pContext
     * @return type[0] WIFI , TWO_OR_THREE_G , UNKNOWN type[0] SubtypeName
     */
    public static String[] getNetworkState(Context pContext) {
        String[] type = new String[2];
        type[0] = "Unknown";
        type[1] = "Unknown";
        if (pContext.getPackageManager().checkPermission("android.permission.ACCESS_NETWORK_STATE", pContext.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
            ConnectivityManager localConnectivityManager = (ConnectivityManager) pContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (localConnectivityManager == null)
                return type;

            NetworkInfo localNetworkInfo1 = localConnectivityManager.getNetworkInfo(1);
            if ((localNetworkInfo1 != null) && (localNetworkInfo1.getState() == NetworkInfo.State.CONNECTED)) {
                type[0] = "Wi-Fi";
                type[1] = localNetworkInfo1.getSubtypeName();
                return type;
            }
            NetworkInfo localNetworkInfo2 = localConnectivityManager.getNetworkInfo(0);
            if ((localNetworkInfo2 == null) || (localNetworkInfo2.getState() != NetworkInfo.State.CONNECTED))
                type[0] = "2G/3G";
            type[1] = localNetworkInfo2.getSubtypeName();
            return type;
        }
        return type;
    }

    /***
     * 获取wifi 地址
     *
     * @param pContext
     * @return
     */

    public static String getWifiAddress(Context pContext) {
        String address = DEFAULT_WIFI_ADDRESS;
        if (pContext != null) {
            WifiInfo localWifiInfo = ((WifiManager) pContext.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
            if (localWifiInfo != null) {
                address = localWifiInfo.getMacAddress();
                if (address == null || address.trim().equals(""))
                    address = DEFAULT_WIFI_ADDRESS;
                return address;
            }

        }
        return DEFAULT_WIFI_ADDRESS;
    }

    /***
     * 获取wifi ip地址
     *
     * @param pContext
     * @return
     */
    public static String getWifiIpAddress(Context pContext) {
        WifiInfo localWifiInfo = null;
        if (pContext != null) {
            localWifiInfo = ((WifiManager) pContext.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
            if (localWifiInfo != null) {
                String str = convertIntToIp(localWifiInfo.getIpAddress());
                return str;
            }
        }
        return "";
    }

    /**
     * 获取WifiManager
     *
     * @param pContext
     * @return
     */
    public static WifiManager getWifiManager(Context pContext) {
        return (WifiManager) pContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * 网络可用 android:name="android.permission.ACCESS_NETWORK_STATE"/>
     *
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isConnected());
    }

    /***
     * wifi状态
     *
     * @return
     */
    public static boolean isWifi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkINfo = cm.getActiveNetworkInfo();
        if (networkINfo != null
                && networkINfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    private static final String TAG = "NetUtil";

    /**
     * 网络连接是否可用
     */
    public static boolean isConnnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null != connectivityManager) {
            NetworkInfo networkInfo[] = connectivityManager.getAllNetworkInfo();

            if (null != networkInfo) {
                for (NetworkInfo info : networkInfo) {
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        Log.e(TAG, "the net is ok");
                        return true;
                    }
                }
            }
        }
        Toast.makeText(context, "连接网络失败，请稍后重试！", Toast.LENGTH_SHORT).show();
        return false;
    }

}
