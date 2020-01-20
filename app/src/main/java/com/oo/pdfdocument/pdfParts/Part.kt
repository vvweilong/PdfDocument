package com.oo.pdfdocument.pdfParts

import android.graphics.Canvas
import android.graphics.RectF

/**
* create by 朱晓龙 2020/1/15 5:49 PM
 * pdf 的文档结构基类
 * 声明测量方法和绘制方法
*/
abstract class Part {
    /**
    * 计算自身尺寸方法
    * */
    abstract fun measureSize():Int
    /**
     * 绘制 pdf 方法
     * */
    abstract fun drawPdf(pdfCanvas: Canvas?)

    /**
     * 自身的位置 rect
     * */
    protected var drawRect= RectF()

    abstract fun canSplit(desierHeight:Int): Part?

}