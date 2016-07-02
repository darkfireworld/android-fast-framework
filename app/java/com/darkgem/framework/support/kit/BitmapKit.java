package com.darkgem.framework.support.kit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by Administrator on 2015/6/9.
 */
public class BitmapKit {
    /**
     * 获得Bitmap
     *
     * @param imgFilePath 图片地址
     * @param height      图片的高度
     * @param width       图片宽度
     * @return 经过大小处理的bitmap
     */
    static public Bitmap getBitmap(String imgFilePath, int height, int width) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(imgFilePath, newOpts);//此时返回bm为空
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = height;
        float ww = width;
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w >= h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) Math.rint(w / ww);
        } else if (w <= h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) Math.rint(h / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(imgFilePath, newOpts);
        return bitmap;
    }

    /**
     * 获得经过大小处理的Bitmap
     *
     * @param image  原bitmap
     * @param height 高度
     * @param width  宽度
     * @return 进过大小处理的bitmap
     */
    public static Bitmap getBitmap(Bitmap image, int height, int width) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        if (baos.toByteArray().length / 1024 > 1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //输出bitmap的大小
        float hh = height;
        float ww = width;
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w >= h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (w / ww);
        } else if (w <= h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (h / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return bitmap;
    }

    /**
     * 压缩bitmap
     *
     * @param image   待压缩的图片
     * @param maxKb   图片大小限制, 以KB为单位
     * @param quality 初始化图片质量，0-100
     * @return 返回经过压缩的图片字节数组
     */
    public static byte[] compressBitmap(Bitmap image, int maxKb, int quality) {
        if (quality < 10) {
            quality = 20;
        }
        if (quality > 100) {
            quality = 100;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while (true) {
            //重置baos即清空baos
            bos.reset();
            //这里压缩options%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, quality, bos);
            //检测图片大小是否符合
            if (bos.toByteArray().length / 1024 <= maxKb) {
                break;
            }
            //保证20%的图片质量
            if (quality <= 20) {
                break;
            }
            //每次都减少10
            quality -= 10;
        }
        return bos.toByteArray();
    }

    /**
     * 压缩bitmap
     *
     * @param image 待压缩的图片
     * @param maxKb 图片大小限制, 以KB为单位
     * @return 返回经过压缩的图片字节数组
     */
    public static byte[] compressBitmap(Bitmap image, int maxKb) {
        return compressBitmap(image, maxKb, 80);
    }
}
