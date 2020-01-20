package com.oo.pdfdocument.pdfParts

import android.graphics.Canvas

class ImagePart: Part() {
    override fun measureSize(): Int {
        return 0
    }

    override fun drawPdf(pdfCanvas: Canvas?) {
    }

    override fun canSplit(desierHeight:Int): Part? {
        //图片用于不能被拆分为两部分
        return null
    }
}