package com.oo.pdfdocument;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.SyncStateContract;
import android.util.Log;

import androidx.core.graphics.BitmapCompat;

import java.io.File;

public class Transitor {

       public  static int PDF_PAGE_WIDTH=621;
        public static int PDF_PAGE_HEIGHT=791;
        public static float PDF_TEXT_SCALE=(float) PDF_PAGE_WIDTH/(float) PDF_PAGE_HEIGHT;
        public static float PDF_IMAGE_SCALE=0.7F;

        public static Bitmap transitOriginBitmapForPdf(String path, int w, int h){
            //目标显示尺寸
            Bitmap originBitmap = BitmapFactory.decodeFile(path);
            return originBitmap;
        }

    /**
     * 图片路径生成bitmap
     */
    public static  Bitmap transitBitmapForPdf(String path, int w, int h) {
        //目标显示尺寸
        int targetWidth = w;
        int targetHeight = h;
        //获取图片文件信息
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(path,options);
        options.inJustDecodeBounds =false;
        //获取保存图片的宽和高
        int orginWidth = options.outWidth;
        int orginHeight = options.outHeight;
        //判断图片大小与 pdfPage的关系
        if (orginWidth>PDF_PAGE_WIDTH-80||orginHeight>PDF_PAGE_HEIGHT-21-40) {
            //如果图片的尺寸超过了pdf可显示的范围  设置目标尺寸
            targetWidth = PDF_PAGE_WIDTH-80;
            targetHeight = PDF_PAGE_HEIGHT-21-40;
        }else {
            //如果图片在pdf的显示范围内  由于按照原始尺寸图片与文字显示比例有问题 所以也需要进行缩放
            targetWidth = Math.round(targetWidth*PDF_IMAGE_SCALE);
            targetHeight = Math.round(targetHeight*PDF_IMAGE_SCALE);
        }
        //需要的缩放比例
        int scale =Math.round( Math.max(((float)orginWidth/(float)targetWidth),((float)orginHeight/(float)targetHeight)));

        options.inSampleSize = scale;
        Bitmap resultBitmap = BitmapFactory.decodeFile(path, options);

        //这里采样率变化 会造成失真
        return resultBitmap;
    }
    /**
     * 图片路径生成bitmap
     */
    public static Bitmap transitScaledBitmapForPdf(String path, int w, int h) {
        //目标显示尺寸
        int targetWidth = w;
        int targetHeight = h;
        //获取图片文件信息
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(path,options);
        options.inJustDecodeBounds=false;
        //获取保存图片的宽和高
        int orginWidth = options.outWidth;
        int orginHeight = options.outHeight;
        //判断图片大小与 pdfPage的关系
        if (orginWidth>PDF_PAGE_WIDTH-80||orginHeight>PDF_PAGE_HEIGHT-21-40) {
            //如果图片的尺寸超过了pdf可显示的范围  设置目标尺寸
            targetWidth = PDF_PAGE_WIDTH-80;
            targetHeight = PDF_PAGE_HEIGHT-21-40;
        }else {
            //如果图片在pdf的显示范围内  由于按照原始尺寸图片与文字显示比例有问题 所以也需要进行缩放
            targetWidth = Math.round(targetWidth*PDF_IMAGE_SCALE);
            targetHeight = Math.round(targetHeight*PDF_IMAGE_SCALE);
        }
        //解析出原始图片 到bitmap
        Bitmap originBitmap = BitmapFactory.decodeFile(path, options);
        Bitmap resultBitmap = Bitmap.createScaledBitmap(originBitmap, targetWidth, targetHeight, true);
        return resultBitmap;
    }
}
