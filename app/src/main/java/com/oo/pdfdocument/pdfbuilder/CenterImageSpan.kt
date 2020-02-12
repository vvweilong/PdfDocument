package com.oo.pdfdocument.pdfbuilder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan

class CenterImageSpan : ImageSpan {

    //自定义对齐方式--与文字中间线对齐
    companion object {
       const val ALIGN_FONT_CENTER = 2

    }
    constructor(context: Context, b: Bitmap) : super(context, b)
    constructor(drawable: Drawable) : super(drawable)

    constructor(context: Context, b: Bitmap, verticalAlignment: Int):super(context, b, verticalAlignment)

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val fm = paint.fontMetricsInt

        //系统原有方法，默认是Bottom模式)
        var transY = bottom - drawable.bounds.bottom
        if (mVerticalAlignment == DynamicDrawableSpan.ALIGN_BASELINE) {
            transY -= fm.descent
        } else if (mVerticalAlignment == ALIGN_FONT_CENTER) {
            //此处加入判断， 如果是自定义的居中对齐
            //与文字的中间线对齐（这种方式不论是否设置行间距都能保障文字的中间线和图片的中间线是对齐的）
            // y+ascent得到文字内容的顶部坐标，y+descent得到文字的底部坐标，（顶部坐标+底部坐标）/2=文字内容中间线坐标
            transY = (y + fm.descent + (y + fm.ascent)) / 2 - drawable.bounds.bottom / 2
        }
        //int transY =(y + fm.descent + y + fm.ascent) / 2 - drawable.getBounds().bottom / 2;//计算y方向的位移
        canvas.save()
        canvas.translate(x, transY.toFloat())//绘制图片位移一段距离
        drawable.draw(canvas)
        canvas.restore()
    }

    /**
     * 重写getSize方法，只有重写该方法后，才能保证不论是图片大于文字还是文字大于图片，都能实现中间对齐
     */
    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        val d = drawable
        val rect = d.bounds
        if (fm != null) {
            val fmPaint = paint.fontMetricsInt
            val fontHeight = fmPaint.bottom - fmPaint.top
            val drHeight = rect.bottom - rect.top

            val top = drHeight / 2 - fontHeight / 4
            val bottom = drHeight / 2 + fontHeight / 4

            fm.ascent = -bottom
            fm.top = -bottom
            fm.bottom = top
            fm.descent = top
        }
        return rect.right
    }
}