package com.example.shenweixing.myapplication.utils;

import android.util.SparseArray;
import android.view.View;

/**
 * 诚志海图
 * Created by ShenWeiXing on 2019/9/10.9:42
 * Description:
 */

public class ViewHolder {

    private final SparseArray<View> views;
    private View convertView;

    private ViewHolder(View convertView) {
        this.views = new SparseArray<View>();
        this.convertView = convertView;
        convertView.setTag(this);
    }

    public static ViewHolder get(View convertView) {
        if (convertView == null) {
            return new ViewHolder(convertView);
        }
        ViewHolder existedHolder = (ViewHolder) convertView.getTag();
        return existedHolder;
    }


    @SuppressWarnings({ "unchecked", "hiding" })
    public static <T extends View> T get(View view, int id) {
        SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
        if (viewHolder == null) {
            viewHolder = new SparseArray<View>();
            view.setTag(viewHolder);
        }
        View childView = viewHolder.get(id);
        if (childView == null) {
            childView = view.findViewById(id);
            viewHolder.put(id, childView);
        }
        return (T) childView;
    }

}
