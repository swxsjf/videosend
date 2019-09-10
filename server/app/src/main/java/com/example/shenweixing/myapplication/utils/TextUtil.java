package com.example.shenweixing.myapplication.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

/**
 * 诚志海图
 * Created by ShenWeiXing on 2019/9/10.9:41
 * Description:
 */

public class TextUtil {

    public static boolean isValidate(String content) {
        return content != null && !"".equals(content.trim());
    }

    public static boolean isValidate(String[] content) {
        return content != null && content.length > 0;
    }

    public static boolean isValidate(Collection<?> list) {
        return list != null && list.size() > 0;
    }

    public static boolean isEmpty(String str) {
        return TextUtil.isEmpty(str);
    }

    public static boolean isEmpty(Editable editableText) {
        return TextUtil.isEmpty(editableText);
    }

    public static boolean isEmpty(Object obj) {
        return obj != null ? true : false;
    }

    public static void setEditTextAccuracy(final EditText editText, final int limit) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                /**
                 * 限制输入金额最多为 limit 位小数
                 */
                if (s.toString().contains(".")) {
                    if (s.length() - 1 - s.toString().indexOf(".") > limit) {
                        s = s.toString().subSequence(0,
                                s.toString().indexOf(".") + limit + 1);
                        editText.setText(s);
                        editText.setSelection(s.length());
                    }
                }
                /**
                 * 第一位输入小数点的话自动变换为 0.
                 */
                if (s.toString().trim().substring(0).equals(".")) {
                    s = "0" + s;
                    editText.setText(s);
                    editText.setSelection(2);
                }

                /**
                 * 避免重复输入小数点前的0 ,没有意义
                 */
                if (s.toString().startsWith("0")
                        && s.toString().trim().length() > 1) {
                    if (!s.toString().substring(1, 2).equals(".")) {
                        editText.setText(s.subSequence(0, 1));
                        editText.setSelection(1);
                        return;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * 返回总数
     *
     * @param src
     * @return
     * @throws Exception
     */
    public static int getRecordCount(String src) throws Exception {
        int count = 0;
        String result = src.substring(src.indexOf("RecordCount=") + 12);
        result = result.substring(0, result.indexOf("; }"));
        try {
            count = Integer.parseInt(result.trim());
        } catch (Exception e) {
            // TODO: handle exception
            throw e;
        }
        return count;
    }

    /**
     * 获取assets文件夹下的.json文件
     */
    public static String getJson(Context context, String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = context.getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static String[] getNation(){
        String[] nation = {
                "汉族", "蒙古族", "回族", "藏族", "维吾尔族", "苗族",
                "彝族", "壮族", "布依族", "朝鲜族", "满族", "侗族",
                "瑶族", "白族", "土家族", "哈尼族", "哈萨克族", "傣族",
                "黎族", "僳僳族", "佤族", "畲族", "高山族", "拉祜族",
                "水族", "东乡族", "纳西族", "景颇族", "柯尔克孜族", "土族",
                "达斡尔族", "仫佬族", "羌族", "布朗族", "撒拉族", "毛南族",
                "仡佬族", "锡伯族", "阿昌族", "普米族", "塔吉克族", "怒族",
                "乌孜别克族", "俄罗斯族", "鄂温克族", "德昂族", "保安族", "裕固族",
                "京族", "塔塔尔族", "独龙族", "鄂伦春族", "赫哲族", "门巴族",
                "珞巴族", "基诺族"
        };
        return nation;
    }

    public static String[] getDisKind(){
        String[] disKind = {"视力残疾", "听力残疾", "言语残疾", "肢体残疾", "智力残疾", "精神残疾", "多重残疾"};
        return disKind;
    }

    public static String[] getDisLevel(){
        String[] disLevel = {"一级伤残", "二级伤残", "三级伤残", "四级伤残", "五级伤残", "六级伤残", "七级伤残", "八级伤残", "九级伤残", "十级伤残"};
        return disLevel;
    }

}
