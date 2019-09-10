package com.example.shenweixing.myapplication.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 诚志海图
 * Created by ShenWeiXing on 2019/9/10.9:38
 * Description:
 */

public class BitmapUtils {
    /**
     * 将bigmap转成byte[]
     *
     * @param bitmap 位图对象
     * @return
     */
    public static byte[] getBitmapByte(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    /**
     * 将byte[]转成bigmap
     *
     * @param temp byte[]
     * @return
     */
    public static Bitmap getBitmapFromByte(byte[] temp) {
        if (temp != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(temp, 0, temp.length);
            return bitmap;
        } else {
            return null;
        }
    }

    /**
     * 将drawable转为bitmap
     *
     * @param drawable drawable
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    private static int FREE_SD_SPACE_NEEDED_TO_CACHE = 1;
    private static int MB = 1024 * 1024;

    /**
     * 保存bitmap到sdcard
     * 注意：不要用线程，要使用请自己另开线程。在我要爆料时要依次序执行其他方法，使用线程会有问题
     *
     * @param dir 文件的跟目录路径
     * @param bm
     */
    public static void saveBitmap(final Bitmap bm, final String dir, final String filename) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
        if (bm == null) {
            //LogUtil.e("saveBitmap，bitmap空");
            return;
        }

        if (FREE_SD_SPACE_NEEDED_TO_CACHE > freeSpaceOnSd()) {
            bm.recycle();
            //LogUtil.e("Exception,内存超了。图片未保存成功：path：" + filename);
            return;
        }

        File dirPath = new File(dir);
        if (!exists(dir))
            dirPath.mkdirs();

        String thidDir = dir;
        if (!thidDir.endsWith(File.separator))
            thidDir += File.separator;

        File f = new File(thidDir + filename);
        try {
            f.createNewFile();
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            // LogUtil.i("saveBitmap，已经保存");
        } catch (FileNotFoundException e) {
            // LogUtil.e("FileNotFoundException,图片未保存成功：path：" + filename);
            e.printStackTrace();
        } catch (IOException e) {
            // LogUtil.e("IOException,图片未保存成功：path：" + filename);
            e.printStackTrace();
        }
//            }
//        }).start();
    }

    /**
     * 验证文件是否存在
     *
     * @param url
     */
    public static boolean exists(String url) {
        File file = new File(url);
        return file.exists();
    }

    /**
     * 检测sdcard可用空间
     */
    public static int freeSpaceOnSd() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
                .getPath());
        double sdFreeMB = ((double) stat.getAvailableBlocks() * (double) stat
                .getBlockSize()) / MB;
        return (int) sdFreeMB;
    }

    /**
     * 通过Uri获取文件
     *
     * @param ac
     * @param uri
     * @return
     */
    public static File getFileFromMediaUri(Context ac, Uri uri) {
        if (uri.getScheme().toString().compareTo("content") == 0) {
            ContentResolver cr = ac.getContentResolver();
            Cursor cursor = cr.query(uri, null, null, null, null);// 根据Uri从数据库中找
            if (cursor != null) {
                cursor.moveToFirst();
                String filePath = cursor.getString(cursor.getColumnIndex("_data"));// 获取图片路径
                cursor.close();
                if (filePath != null) {
                    return new File(filePath);
                }
            }
        } else if (uri.getScheme().toString().compareTo("file") == 0) {
            return new File(uri.toString().replace("file://", ""));
        }
        return null;
    }

    /**
     * 质量压缩方法
     *
     * @return
     */
    public static Bitmap compressImage1(Bitmap bmp) {

        // 首先进行一次大范围的压缩

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, output);
        float zoom = (float) Math.sqrt(200 * 1024 / (float) output.toByteArray().length); //获取缩放比例

        // 设置矩阵数据
        Matrix matrix = new Matrix();
        matrix.setScale(zoom, zoom);

        // 根据矩阵数据进行新bitmap的创建
        Bitmap resultBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

        output.reset();

        resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);

        // 如果进行了上面的压缩后，依旧大于32K，就进行小范围的微调压缩
        while (output.toByteArray().length > 200 * 1024) {
            matrix.setScale(0.9f, 0.9f);//每次缩小 1/10

            resultBitmap = Bitmap.createBitmap(
                    resultBitmap, 0, 0,
                    resultBitmap.getWidth(), resultBitmap.getHeight(), matrix, true);

            output.reset();
            resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        }
        Log.e("压缩之后的图片的大小", output.toByteArray().length / 1024 + "");
        ByteArrayInputStream isBm = new ByteArrayInputStream(output.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    /**
     * 质量压缩方法
     *
     * @return
     */
    public static Bitmap compressImage(Bitmap bmp, int size) {

        // 首先进行一次大范围的压缩

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, output);
        float zoom = (float) Math.sqrt(size * 1024 / (float) output.toByteArray().length); //获取缩放比例

        // 设置矩阵数据
        Matrix matrix = new Matrix();
        matrix.setScale(zoom, zoom);

        // 根据矩阵数据进行新bitmap的创建
        Bitmap resultBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

        output.reset();

        resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);

        // 如果进行了上面的压缩后，依旧大于32K，就进行小范围的微调压缩
        while (output.toByteArray().length > size * 1024) {
            matrix.setScale(0.9f, 0.9f);//每次缩小 1/10

            resultBitmap = Bitmap.createBitmap(
                    resultBitmap, 0, 0,
                    resultBitmap.getWidth(), resultBitmap.getHeight(), matrix, true);

            output.reset();
            resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        }
        Log.e("压缩之后的图片的大小", output.toByteArray().length / 1024 + "");
        ByteArrayInputStream isBm = new ByteArrayInputStream(output.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }


    public static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        // 判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
        if (baos.toByteArray().length / 1024 > 1024) {
            baos.reset();// 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 80, baos);// 这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;// 这里设置高度为800f
        float ww = 480f;// 这里设置宽度为480f
      /*  float hh = 512f;
        float ww = 512f;*/
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) { // 如果高度高的话根据高度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be; // 设置缩放比例
        // newOpts.inPreferredConfig = Config.RGB_565;//降低图片从ARGB888到RGB565

        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);

        return compressImage1(bitmap);// 压缩好比例大小后再进行质量压缩

        //return bitmap;
    }

    /**
     * bitmap旋转
     *
     * @param angle  旋转角度
     * @param bitmap
     * @return
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();

        matrix.postRotate(angle);
        System.out.println("angle2=" + angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }
}
