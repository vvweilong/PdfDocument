package com.oo.pdfdocument.pdfbuilder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.oo.pdfdocument.PdfResourceDownLoader
import com.oo.pdfdocument.bean.DataResponse
import com.oo.pdfdocument.bean.QuestionResp
import java.io.File
import kotlin.math.roundToInt


class PdfBuilder(val context: Context) {
    var pdfDocument = PdfDocument()
    var pageWidth = 0
    var pageHeight = 0


    var document = Document()
    val gson = Gson()

    val localPath = ArrayList<Pair<String,String>>()

    /**
     * 下载网络资源
     * 当前 数据中题目的部分是 json
     * */
    fun prepareImageResouce(
        context: Context,
        resp: DataResponse,
        callback: ResourcePrepareCallback
    ) {
        //将所有的图片资源转为本地路径
        val imageUrls = getImageUrls(resp)

        PdfResourceDownLoader.downloadImages(context, imageUrls, object : PdfResourceDownLoader.MultiRequestCallback {
                override fun success(paths: ArrayList<Pair<String, String>>) {
                    //
                    localPath.clear()
                    localPath.addAll(paths)
                    //根据数据 生成 spannablestring 以及 part
                    structPdfData(resp)
                    //回调--准备工作完成
                    callback.onPrepared()
                }

            })
    }


    private fun replaceUrlToPath(resp: DataResponse, paths: ArrayList<Pair<String, String>>) {
        resp.data?.forEach { questionData ->
            //题干 的组成
            questionData.questionStem?.forEach { typeTextData ->
                when (typeTextData.type) {
                    5 -> {//5 代表啥？
                        val sourceJson = typeTextData.text
                        val jsonArray = gson.fromJson(sourceJson, JsonArray::class.java)
                        for (jsonElement in jsonArray) {
                            val questionResp = gson.fromJson(jsonElement, QuestionResp::class.java)
                            when (questionResp.typeEnum) {
                                "latex" -> {//图片部分
                                    if (questionResp.style != null && questionResp?.style?.src != null) {
                                        //替换 url 为 本地 path
                                        for ((key, path) in paths) {
                                            if (TextUtils.equals(key, questionResp.style?.src)) {
                                                Log.i("pdfbuilder","$key   $path")
                                                typeTextData.text?.replace(key,path)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取需要下载的图片集合
     * */
    private fun getImageUrls(resp: DataResponse):ArrayList<String> {
        val result = ArrayList<String>()
        resp.data?.forEach { questionData ->
            //题干
            questionData.questionStem?.forEach { typeTextData ->
                when (typeTextData.type) {
                    5 -> {//5 代表啥？
                        val sourceJson = typeTextData.text
                        val jsonArray = gson.fromJson(sourceJson, JsonArray::class.java)
                        for (jsonElement in jsonArray) {
                            val questionResp = gson.fromJson(jsonElement, QuestionResp::class.java)
                            when (questionResp.typeEnum) {
                                "latex" -> {//图片部分
                                    if (questionResp.style != null && questionResp?.style?.src != null) {
                                        result.add(questionResp.style?.src!!)
                                    }
                                }
                            }
                        }

                    }
                }
            }
            // TODO: 2020/1/18  答案 解析等

        }
        return result
    }

    interface ResourcePrepareCallback {
        fun onPrepared()
    }

    fun transitBitmapForPdf(path: String, w: Int, h: Int): Bitmap {
        val options = BitmapFactory.Options()
        options.outWidth = (w*context.resources.displayMetrics.density).roundToInt()
        options.outHeight = (h*context.resources.displayMetrics.density).roundToInt()
        if (File(path).exists()) {
            return BitmapFactory.decodeFile(path, options)
        }else{
            return Bitmap.createBitmap(options.outWidth,options.outHeight,Bitmap.Config.RGB_565)
        }

    }


    /**
     * 将 数据转为 生成 pdf 文件所需要的结构列表
     * */
    fun structPdfData(resp: DataResponse) {
        //首先 按照题目进行循环

        val spannableList = ArrayList<SpannableStringBuilder>()
        resp.data?.forEach { questionData ->
            //题干
            val spannableStringBuilder = SpannableStringBuilder()
            questionData.questionStem?.forEach { typeTextData ->
                when (typeTextData.type) {
                    5 -> {//5 代表啥？
                        val sourceJson = typeTextData.text
                        val jsonArray = gson.fromJson(sourceJson, JsonArray::class.java)
                        for (jsonElement in jsonArray) {
                            val questionResp = gson.fromJson(jsonElement, QuestionResp::class.java)
                            when (questionResp.typeEnum) {
                                "text" -> {//文字部分
                                    spannableStringBuilder.append(questionResp.content)
                                }
                                "latex" -> {//图片部分
                                    val w = questionResp.style?.width?.toInt() ?: 0
                                    val h = questionResp.style?.height?.toInt() ?: 0

                                    for ((url, path) in localPath) {
                                        if (TextUtils.equals(url,questionResp.content)) {
                                            questionResp.content = path
                                        }
                                    }

                                    val pdfBitmap =
                                        transitBitmapForPdf(questionResp.content ?: "", w, h)
                                    spannableStringBuilder.append("图片")
                                    val centerImageSpan = CenterImageSpan(pdfBitmap)
                                    spannableStringBuilder.setSpan(
                                        centerImageSpan,
                                        spannableStringBuilder.length - 2,
                                        spannableStringBuilder.length,
                                        SpannableString.SPAN_INCLUSIVE_EXCLUSIVE
                                    )
                                }
                                else -> {
                                }
                            }
                        }
                    }
                }
            }
            spannableList.add(spannableStringBuilder)
        }

        for (spannableStringBuilder in spannableList) {
            addContent(spannableStringBuilder)
        }

    }


    fun addContent(spannableString: SpannableStringBuilder): PdfBuilder {
        val textPart = TextPart(spannableString, pageWidth)
        if (document.pages.isEmpty()) {
            document.pages.add(Page())
        }
        val lastestPage = document.pages.last()
        //高度是否还有富余
        val remainHeight = lastestPage.getRemainHeight(pageHeight)
        val measureHeight = textPart.measureSize()
        if (measureHeight <= remainHeight) {
            //如果可以绘制
            lastestPage.parts.add(textPart)
        } else {
            //如果不能绘制 看看最有一个 part 是否能通过拆分达到容量
            var tempPart = textPart.canSplit(remainHeight)
            if (tempPart == null) {//无法拆分 将 textpart 加入到下一页
                val newPage = Page()
                document.pages.add(newPage)
                newPage.parts.add(textPart)
            } else {
                //textPart 是原页面的
                lastestPage.parts.add(textPart)
                val newPage = Page()
                document.pages.add(newPage)
                newPage.parts.add(tempPart)
            }
        }
        return this
    }

    fun addContent(text: String): PdfBuilder {
        val spannableString = SpannableStringBuilder(text)
        val textPart = TextPart(spannableString, pageWidth)
        if (document.pages.isEmpty()) {
            document.pages.add(Page())
        }
        val lastestPage = document.pages.last()
        //高度是否还有富余
        val remainHeight = lastestPage.getRemainHeight(pageHeight)
        val measureHeight = textPart.measureSize()
        if (measureHeight <= remainHeight) {
            //如果可以绘制
            lastestPage.parts.add(textPart)
        } else {
            //如果不能绘制 看看最有一个 part 是否能通过拆分达到容量
            var tempPart = textPart.canSplit(remainHeight)
            if (tempPart == null) {//无法拆分 将 textpart 加入到下一页
                val newPage = Page()
                document.pages.add(newPage)
                newPage.parts.add(textPart)
            } else {
                //textPart 是原页面的
                lastestPage.parts.add(textPart)
                val newPage = Page()
                document.pages.add(newPage)
                newPage.parts.add(tempPart)
            }
        }
        return this
    }

    fun setPageSize(w: Int, h: Int): PdfBuilder {
        pageWidth = w
        pageHeight = h
        return this
    }

    fun create() {
        Thread() {
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
                    canvas.translate(0f, lastMeasureSize.toFloat())
                    part.drawPdf(canvas)
                    lastMeasureSize = part.measureSize()
                }
                pdfDocument.finishPage(currentPage)
            }
            writeToFile()
        }.start()
    }

    private fun writeToFile() {

        val file =
            File("${context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.path}/${System.currentTimeMillis()}.pdf")
        file.createNewFile()
        pdfDocument.writeTo(file.outputStream())
        pdfDocument.close()
        Log.i("TAG", "${file.path}")
    }


    inner class Document {
        val pages = ArrayList<Page>()

    }

    inner class Page {
        val parts = ArrayList<Part>()
        fun getRemainHeight(height: Int): Int {
            var h = height
            for (part in parts) {
                h -= part.measureSize()
            }
            return h
        }

    }


}