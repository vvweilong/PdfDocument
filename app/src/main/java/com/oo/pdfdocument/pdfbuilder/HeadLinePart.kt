package com.oo.pdfdocument.pdfbuilder

import android.graphics.Canvas
/**
* create by 朱晓龙 2020/1/19 5:28 PM
 *
 * 顶部分割线
*/
class HeadLinePart: Part() {
    override fun measureSize(): Int {
        return 3
    }

    override fun drawPdf(pdfCanvas: Canvas?) {
        //drawline
    }

    override fun canSplit(desierHeight: Int): Part? {
        return null
    }
}