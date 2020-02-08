package com.oo.pdfdocument.pdfbuilder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.style.ImageSpan

class CenterImageSpan: ImageSpan {
    constructor(context: Context,b: Bitmap) : super(context,b)
    constructor(drawable: Drawable) : super(drawable)


    override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        val fm  = paint.fontMetricsInt
        val transY =(y + fm.descent + y + fm.ascent) / 2 - drawable.getBounds().bottom / 2//计算y方向的位移
        canvas.save();
        canvas.translate(x, transY.toFloat());//绘制图片位移一段距离
        drawable.draw(canvas);
        canvas.restore();
    }
}