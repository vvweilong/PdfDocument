package com.oo.pdfdocument.pdfbuilder

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.text.SpannableString
import android.util.Log
import java.io.File


class PdfBuilder(val context:Context) {
    var pdfDocument = PdfDocument()
    var pageWidth = 0
    var pageHeight = 0


    var document = Document()


    /**
     * 每次 add head 都要新建一个 page
     * */
    fun addHead(head:Part): PdfBuilder {

        return this
    }


    fun addContent(spannableString: SpannableString):PdfBuilder{
        val textPart = TextPart( spannableString,pageWidth)
        if (document.pages.isEmpty()) {
            document.pages.add(Page())
        }
        val lastestPage = document.pages.last()
        //高度是否还有富余
        val remainHeight = lastestPage.getRemainHeight(pageHeight)
        val measureHeight = textPart.measureSize()
        if(measureHeight<=remainHeight) {
            //如果可以绘制
            lastestPage.parts.add(textPart)
        }else {
            //如果不能绘制 看看最有一个 part 是否能通过拆分达到容量
            var tempPart = textPart.canSplit(remainHeight)
            if (tempPart== null) {//无法拆分 将 textpart 加入到下一页
                val newPage = Page()
                document.pages.add(newPage)
                newPage.parts.add(textPart)
            }else{
                //textPart 是原页面的
                lastestPage.parts.add(textPart)
                val newPage = Page()
                document.pages.add(newPage)
                newPage.parts.add(tempPart)
            }
        }
        return this
    }

    fun addContent(text:String):PdfBuilder{
        val spannableString = SpannableString(text)
        val textPart = TextPart( spannableString,pageWidth)
        if (document.pages.isEmpty()) {
            document.pages.add(Page())
        }
        val lastestPage = document.pages.last()
        //高度是否还有富余
        val remainHeight = lastestPage.getRemainHeight(pageHeight)
        val measureHeight = textPart.measureSize()
        if(measureHeight<=remainHeight) {
            //如果可以绘制
            lastestPage.parts.add(textPart)
        }else {
            //如果不能绘制 看看最有一个 part 是否能通过拆分达到容量
            var tempPart = textPart.canSplit(remainHeight)
            if (tempPart== null) {//无法拆分 将 textpart 加入到下一页
                val newPage = Page()
                document.pages.add(newPage)
                newPage.parts.add(textPart)
            }else{
                //textPart 是原页面的
                lastestPage.parts.add(textPart)
                val newPage = Page()
                document.pages.add(newPage)
                newPage.parts.add(tempPart)
            }
        }
        return this
    }
    fun setPageSize(w:Int,h:Int):PdfBuilder{
        pageWidth = w
        pageHeight = h
        return this
    }
    fun create(){
        Thread(){
            for (page in document.pages) {
                val builder = PdfDocument.PageInfo.Builder(
                    pageWidth,
                    pageHeight,
                    document.pages.indexOf(page) + 1
                )
                val currentPage = pdfDocument.startPage(builder.create())
                var lastMeasureSize = 0
                for (part in page.parts) {
                    val canvas = currentPage.canvas
                    canvas.translate(0f,lastMeasureSize.toFloat())
                    part.drawPdf(canvas)
                    lastMeasureSize = part.measureSize()
                }
                pdfDocument.finishPage(currentPage)
            }
            writeToFile()
        }.start()

    }

    private fun writeToFile() {

        val file = File("${context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.path}/${System.currentTimeMillis()}.pdf")
        file.createNewFile()
        pdfDocument.writeTo(file.outputStream())
        pdfDocument.close()
        Log.i("TAG","${file.path}")
    }


    inner class Document{
        val pages = ArrayList<Page>()

    }
    inner class Page{
        val parts = ArrayList<Part>()
        fun getRemainHeight(height:Int):Int{
            var h = height
            for (part in parts) {
                h -=part.measureSize()
            }
            return h
        }

    }


}