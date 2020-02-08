package com.oo.pdfdocument.pdfParts

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.oo.pdfdocument.R
import kotlin.math.roundToInt

/**
 * 每页的 顶部
 *        图片         1
 * */
class HeadPart(val context: Context, val pageIndex: Int,val pageWidth: Int,val iconw:Int,val iconh:Int) : Part() {

    val staticLayout:StaticLayout
    init {
        val textPaint = TextPaint()
        textPaint.textSize = 36*context.getResources().getDisplayMetrics().density
        staticLayout = StaticLayout(
            "$pageIndex",
            textPaint,
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
            //进行居中放置
            val offset = (iconh - staticLayout.height)/2f
            val options = BitmapFactory.Options()
            options.outWidth = iconw
            options.outHeight = iconh
            val bitmap =
                BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher, options)
            val top = Math.abs(offset).roundToInt()
            if(offset>0){
                //图片高 先绘制图片 绘制位置
                drawBitmap(bitmap,null, Rect(width/2 - iconw/2,0,width/2 +iconw/2, iconh),null)
                //绘制文字 向下移动 offset
                translate(0f,Math.abs(offset))
                staticLayout.draw(this)
                pdfCanvas.translate(0f,-Math.abs(offset))
            }else{
                //文字高 先绘制文字
                staticLayout.draw(this)
                //绘制图片
                drawBitmap(bitmap,null, Rect(width/2 - iconw/2,top,width/2 +iconw/2, top+iconh),null)
            }
        }
    }

    override fun canSplit(desierHeight: Int): Part? {
        return null
    }
}