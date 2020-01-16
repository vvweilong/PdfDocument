package com.oo.pdfdocument.pdfbuilder

import android.graphics.Canvas
import android.graphics.Region
import android.text.Layout
import android.text.SpannableString
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log

class TextPart(conten: SpannableString, width: Int) : Part() {


    var staticLayout:StaticLayout
    var content: SpannableString
    var pageWidth:Int= width


    init {
        content = SpannableString(conten)
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

    private fun reinit(contenSS: SpannableString) {
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
        val region = Region(drawRect)
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
        for (lineNum in 0 until staticLayout.lineCount){
            val lb = staticLayout.getLineBottom(lineNum)
            if(lb>desierHeight){
                break;
            }
            splitLine+=1
        }
        val splitStrPosition = staticLayout.getLineEnd(splitLine)
        val remainContent = SpannableString(content.substring(0, splitStrPosition))
        val splitedContent = SpannableString(content.substring(splitStrPosition, content.length))
        reinit(remainContent)
        return TextPart(splitedContent,pageWidth)
    }
}