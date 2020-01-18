package com.oo.pdfdocument.pdfbuilder;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.oo.pdfdocument.R;

import java.lang.reflect.Field;

/**
 * 获取网络图片的ImageSpan
 *
 * @author zhenxuewen
 */
public class UrlImageSpan extends ImageSpan {

  private final String[] size;
  private Context context;
  private int screenWidth;
  private String url;
  private TextView tv;
  private boolean picShowed;
  //自定义对齐方式--与文字中间线对齐
  public static final int ALIGN_FONTCENTER = 2;

  public UrlImageSpan(Context context, String url, TextView tv, int verticalAlignment,
                      int screenWidth, String[] size) {
    super(context, R.mipmap.exercise_next, verticalAlignment);
    this.size = size;
    this.url = url;
    this.tv = tv;
    this.context = context;
    this.screenWidth = screenWidth;
  }

  @Override
  public Drawable getDrawable() {
    if (!picShowed) {
      ImageRender.getInstance().loadImage(url, new ImageLoadingListener() {
        @Override
        public void onLoadingStarted(String s, View view) {

        }

        @Override
        public void onLoadingFailed(String s, View view, FailReason failReason) {

        }

        @Override
        public void onLoadingComplete(String s, View view, Bitmap bitmap) {
          try {
            Resources resources = context.getResources();
            //int targetWidth = (int) (resources.getDisplayMetrics().widthPixels*0.2 );
            //Bitmap zoom = zoom(bitmap, targetWidth);
            //BitmapDrawable b = new BitmapDrawable(resources, zoom);
            BitmapDrawable b = new BitmapDrawable(resources, bitmap);
            int realWidth = 0;
            int realHeight = 0;
            int width = (size != null && Integer.parseInt(size[0]) > 0) ? Integer.parseInt(size[0])
                : b.getIntrinsicWidth();
            int height = (size != null && Integer.parseInt(size[1]) > 0) ? Integer.parseInt(size[1])
                : b.getIntrinsicHeight();
            if (DisplayUtils.dp2px(context, width)
                > screenWidth - DisplayUtils.dp2px(context, 66)) {
              realWidth =
                  screenWidth - DisplayUtils.dp2px(context, 66);
              realHeight =
                  (screenWidth - DisplayUtils.dp2px(context, 66))
                      * height / width;
            } else {
              realWidth = DisplayUtils.dp2px(context, width);
              realHeight = DisplayUtils.dp2px(context, height);
            }
            b.setBounds(0, 0, realWidth,
                realHeight);
            Field mDrawable;
            Field mDrawableRef;
            mDrawable = ImageSpan.class.getDeclaredField("mDrawable");
            mDrawable.setAccessible(true);
            mDrawable.set(UrlImageSpan.this, b);
            mDrawableRef = DynamicDrawableSpan.class.getDeclaredField("mDrawableRef");
            mDrawableRef.setAccessible(true);
            mDrawableRef.set(UrlImageSpan.this, null);
            picShowed = true;
            tv.setText(tv.getText());
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        @Override
        public void onLoadingCancelled(String s, View view) {

        }
      });
    }
    return super.getDrawable();
  }

  /**
   * 按宽度缩放图片
   *
   * @param bmp 需要缩放的图片源
   * @param newW 需要缩放成的图片宽度
   * @return 缩放后的图片
   */
  public static Bitmap zoom(@NonNull Bitmap bmp, int newW) {

    // 获得图片的宽高
    int width = bmp.getWidth();
    int height = bmp.getHeight();

    // 计算缩放比例
    float scale = ((float) newW) / width;
    scale = 1;

    // 取得想要缩放的matrix参数
    Matrix matrix = new Matrix();
    matrix.postScale(scale, scale);

    // 得到新的图片
    Bitmap newbm = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);

    return newbm;
  }

  @Override
  public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y,
                   int bottom, Paint
      paint) {

    //draw 方法是重写的ImageSpan父类 DynamicDrawableSpan中的方法，在DynamicDrawableSpan类中，虽有getCachedDrawable()，
    // 但是私有的，不能被调用，所以调用ImageSpan中的getrawable()方法，该方法中 会根据传入的drawable ID ，获取该id对应的
    // drawable的流对象，并最终获取drawable对象
    Drawable drawable = getDrawable(); //调用imageSpan中的方法获取drawable对象
    canvas.save();

    //获取画笔的文字绘制时的具体测量数据
    Paint.FontMetricsInt fm = paint.getFontMetricsInt();

    //系统原有方法，默认是Bottom模式)
    int transY = bottom - drawable.getBounds().bottom;
    if (mVerticalAlignment == ALIGN_BASELINE) {
      transY -= fm.descent;
    } else if (mVerticalAlignment == ALIGN_FONTCENTER) {    //此处加入判断， 如果是自定义的居中对齐
      //与文字的中间线对齐（这种方式不论是否设置行间距都能保障文字的中间线和图片的中间线是对齐的）
      // y+ascent得到文字内容的顶部坐标，y+descent得到文字的底部坐标，（顶部坐标+底部坐标）/2=文字内容中间线坐标
      transY = ((y + fm.descent) + (y + fm.ascent)) / 2 - drawable.getBounds().bottom / 2;
    }

    canvas.translate(x, transY);
    drawable.draw(canvas);
    canvas.restore();
  }

  /**
   * 重写getSize方法，只有重写该方法后，才能保证不论是图片大于文字还是文字大于图片，都能实现中间对齐
   */
  @Override
  public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
    Drawable d = getDrawable();
    Rect rect = d.getBounds();
    if (fm != null) {
      Paint.FontMetricsInt fmPaint = paint.getFontMetricsInt();
      int fontHeight = fmPaint.bottom - fmPaint.top;
      int drHeight = rect.bottom - rect.top;

      int top = drHeight / 2 - fontHeight / 4;
      int bottom = drHeight / 2 + fontHeight / 4;

      fm.ascent = -bottom;
      fm.top = -bottom;
      fm.bottom = top;
      fm.descent = top;
    }
    return rect.right;
  }
}

