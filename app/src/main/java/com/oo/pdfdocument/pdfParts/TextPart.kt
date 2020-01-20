package com.oo.pdfdocument.pdfParts

import android.graphics.Canvas
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log

class TextPart(conten: SpannableStringBuilder, width: Int) : Part() {


    var staticLayout:StaticLayout
    var content: SpannableStringBuilder
    var pageWidth:Int= width


    init {
        this.content = conten
    }


    init {
        val textPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG)
        staticLayout = StaticLayout(
            content,
            textPaint,
            pageWidth,
            Layout.Alignment.ALIGN_NORMAL,
            1f,
            0f,
            true
        )
    }

    private fun reinit(contenSS: SpannableStringBuilder) {
        this.content = contenSS
        val textPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG)
        staticLayout = StaticLayout(
            content,
            textPaint,
            pageWidth,
            Layout.Alignment.ALIGN_NORMAL,
            1f,
            0f,
            true
        )
    }

    override fun measureSize(): Int {
        Log.i("textPart", "measureSize: ${staticLayout.lineCount}")
        return staticLayout.height
    }

    override fun drawPdf(pdfCanvas: Canvas?) {
        staticLayout.draw(pdfCanvas)
    }

    override fun canSplit(desierHeight:Int): Part? {
        //首先 查看当前 part 有多少行
        if (staticLayout.lineCount == 1) {
            //只有一行不能被拆分
            return null
        }
        //当超过一行时 判断一行的高度是否满足
        val lineBottom = staticLayout.getLineBottom(0)
        if(lineBottom>desierHeight){
            //第一行无法满足拆分条件 也不能进行拆分
            return null
        }
        //走到这里 至少可以拆分一行出来 找出可拆分到的行数
        var splitLine = 0
        Log.i("1111", "desier $desierHeight ")
        for (lineNum in 0 until  staticLayout.lineCount){
            val lb = staticLayout.getLineBottom(lineNum)
            Log.i("1111", "lineBottom $lb ")
            if(lb > desierHeight){
                //如果高度超了
                splitLine = lineNum-1
                break
            }
        }
        val splitStrPosition = staticLayout.getLineEnd(Math.max(0,splitLine))
        val remainContent = SpannableStringBuilder(content.substring(0, Math.min(Math.max(0,splitStrPosition),content.length)))
        val splitedContent = SpannableStringBuilder(content.substring(splitStrPosition, content.length))
        reinit(remainContent)
        Log.i("tag", "canSplit: ${measureSize()}")
        return TextPart(splitedContent, pageWidth)
    }
}