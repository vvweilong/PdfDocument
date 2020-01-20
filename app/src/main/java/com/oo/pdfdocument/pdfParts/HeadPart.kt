package com.oo.pdfdocument.pdfParts

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.oo.pdfdocument.R

/**
 * 每页的 顶部
 *        图片         1
 * */
class HeadPart(val context: Context, val pageIndex: Int,val pageWidth: Int,val iconw:Int,val iconh:Int) : Part() {

    val staticLayout:StaticLayout
    init {
        staticLayout = StaticLayout(
            "$pageIndex",
            TextPaint(),
            pageWidth,
            Layout.Alignment.ALIGN_OPPOSITE,
            1f,
            0f,
            true
        )



    }
    override fun measureSize(): Int {
        return Math.max(staticLayout.height,iconh)

    }

    override fun drawPdf(pdfCanvas: Canvas?) {
        pdfCanvas?.run {
            //图片
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher)
            drawBitmap(bitmap,null, Rect(width/2 - iconw/2,0,width/2 +iconw/2, iconh),null)
            //页号
            translate(0f,(iconh-staticLayout.height).toFloat())
            staticLayout.draw(this)
            translate(0f,-(iconh-staticLayout.height).toFloat())
        }
    }

    override fun canSplit(desierHeight: Int): Part? {
        return null
    }
}