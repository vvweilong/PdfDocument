package com.oo.pdfdocument.pdfbuilder

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.TextPaint
import com.oo.pdfdocument.R

/**
 * 每页的 顶部
 *        图片         1
 * */
class HeadPart(context: Context,val pageIndex:Int): Part() {
    private val drawable:Drawable = context.resources.getDrawable(R.mipmap.ic_launcher)
    override fun measureSize(): Int {
        //图片
        return drawable.bounds.height()
    }

    override fun drawPdf(pdfCanvas: Canvas?) {
        //todo  绘制图片 水平居中

        //todo 绘制页码
        val textPaint = TextPaint()
        val measureText = textPaint.measureText("$pageIndex")
        pdfCanvas?.drawText("$pageIndex",pdfCanvas.width - measureText,0f,textPaint)
    }

    override fun canSplit(desierHeight: Int): Part? {
        return null
    }
}