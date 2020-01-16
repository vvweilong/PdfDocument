package com.oo.pdfdocument.pdfbuilder

import android.graphics.Canvas
import android.graphics.Rect

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
     * 确定位置方法
     * */
    fun layoutPosition(l:Int,t:Int,r:Int,b:Int){
        drawRect.set(l,t,r,b)
    }

    /**
     * 自身的位置 rect
     * */
    protected var drawRect= Rect()

    abstract fun canSplit(desierHeight:Int):Part?

}