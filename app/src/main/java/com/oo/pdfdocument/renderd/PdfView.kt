package com.oo.pdfdocument.renderd

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.util.AttributeSet
import android.view.ViewGroup

class PdfView:ViewGroup {



    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )
    init {

    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {

    }
}