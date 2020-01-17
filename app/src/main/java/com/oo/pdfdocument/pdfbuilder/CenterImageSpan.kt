package com.oo.pdfdocument.pdfbuilder

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.style.ImageSpan

class CenterImageSpan(drawable: Drawable): ImageSpan(drawable) {

    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        val drawable = getDrawable()
        val rect = drawable.getBounds()
        if (fm != null) {
            val fmPaint = paint.getFontMetricsInt()
            val fontHeight = fmPaint.bottom - fmPaint.top;
            val drHeight = rect.bottom - rect.top;
            val top = drHeight / 2 - fontHeight / 4;
            val bottom = drHeight / 2 + fontHeight / 4;
            fm.ascent = -bottom;
            fm.top = -bottom;
            fm.bottom = top;
            fm.descent = top;
        }
        return rect.right;
    }


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
        val drawable = getDrawable()
        canvas.save();
        var transY = ((bottom - top) - drawable.getBounds().bottom) / 2 + top;
        canvas.translate(x, transY.toFloat());
        drawable.draw(canvas);
        canvas.restore();
    }
}