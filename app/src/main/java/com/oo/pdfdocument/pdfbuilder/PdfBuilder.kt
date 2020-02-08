package com.oo.pdfdocument.pdfbuilder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.ImageSpan
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.oo.pdfdocument.PdfResourceDownLoader
import com.oo.pdfdocument.bean.DataResponse
import com.oo.pdfdocument.bean.QuestionResp
import com.oo.pdfdocument.bean.TypeTextData
import com.oo.pdfdocument.pdfParts.HeadLinePart
import com.oo.pdfdocument.pdfParts.HeadPart
import com.oo.pdfdocument.pdfParts.Part
import com.oo.pdfdocument.pdfParts.TextPart
import java.io.File
import kotlin.math.roundToInt


class PdfBuilder(val context: Context) {
    val TAG = "PdfBuilder"
    var pdfDocument = PdfDocument()
    var pageWidth = 0
    var pageHeight = 0


    var document = Document()
    val gson = Gson()

    val localPath = HashMap<String, String>()

    /**
     * 下载网络资源
     * 当前 数据中题目的部分是 json
     * */
    fun prepareImageResouce(context: Context, resp: DataResponse, callback: ResourcePrepareCallback) {
        //将所有的图片资源转为本地路径
        val imageUrls = getImageUrls(resp)

        PdfResourceDownLoader.downloadImages(
            context,
            imageUrls,
            object : PdfResourceDownLoader.MultiRequestCallback {
                override fun success(paths: HashMap<String, String>) {
                    //
                    localPath.clear()
                    localPath.putAll(paths)
                    //根据数据 生成 spannablestring 以及 part
                    structPdfData(resp)
                    //回调--准备工作完成
                    callback.onPrepared()
                }

            })
    }


    /**
     * 获取需要下载的图片集合
     * */
    private fun getImageUrls(resp: DataResponse): ArrayList<String> {
        val result = ArrayList<String>()
        resp.data?.forEach { questionData ->
            //题干
            getPdfUrls(questionData.questionStem, result)
            //选项
            getOptionUrls(questionData.questionOption, result)
            //答案
            getPdfUrls(questionData.questionAnswer,result)
            //分析
            getPdfUrls(questionData.questionAnalysis,result)
        }
        return result
    }

    private fun getOptionUrls(serverData: ArrayList<TypeTextData>?, result: ArrayList<String>) {
        serverData?.forEach { typeTextData ->
            when (typeTextData.type) {
                5 -> {//5 代表啥？
                    val sourceJson = typeTextData.text
                    val jsonArray = gson.fromJson(sourceJson, JsonArray::class.java)
                    for (outElement in jsonArray) {
                        for (jsonElement in outElement.asJsonArray) {
                            val questionResp =
                                gson.fromJson(jsonElement, QuestionResp::class.java)
                            when (questionResp.typeEnum) {
                                "latex" -> {//图片部分
                                    if (questionResp.style != null && questionResp?.style?.src != null) {
                                        result.add(questionResp.style?.src!!)
                                    }
                                }
                                "image"->{
                                    if (questionResp.style != null && questionResp?.style?.src != null) {
                                        result.add(questionResp.style?.src!!)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getPdfUrls(serverData: ArrayList<TypeTextData>?, result: ArrayList<String>) {
        serverData?.forEach { typeTextData ->
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
                            "image"->{
                                if (questionResp.style != null && questionResp?.style?.src != null) {
                                    result.add(questionResp.style?.src!!)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    interface ResourcePrepareCallback {
        fun onPrepared()
    }

    fun transitBitmapForPdf(path: String, w: Int, h: Int): Bitmap {
        val options = BitmapFactory.Options()
        options.outWidth = (w*context.resources.displayMetrics.density).roundToInt()
        options.outHeight = (h*context.resources.displayMetrics.density).roundToInt()
        Log.i(TAG,"w = ${w} h = ${h}")
        Log.i(TAG,"w = ${options.outWidth} h = ${options.outHeight}")
        if (File(path).exists()) {
            return BitmapFactory.decodeFile(path, options)
        } else {
            return Bitmap.createBitmap(options.outWidth, options.outHeight, Bitmap.Config.RGB_565)
        }
    }
    /**
     * 将 数据转为 生成 pdf 文件所需要的结构列表
     * */
    fun structPdfData(resp: DataResponse) {
        //首先 按照题目进行循环
        val spannableList = ArrayList<SpannableStringBuilder>()
        resp.data?.forEach { questionData ->
            Log.i(TAG, "structPdfData: 题干")
            //题干
            buildSpannableString(questionData.questionStem,spannableList)
            //选项
            Log.i(TAG, "structPdfData: 选项")
            buildOptionSpannableString(questionData.questionOption, spannableList)
            //答案
            Log.i(TAG, "structPdfData: 答案")
            buildSpannableString(questionData.questionAnswer,spannableList)
            //解析
            Log.i(TAG, "structPdfData: 解析")
            buildSpannableString(questionData.questionAnalysis,spannableList)
        }
        for (spannableStringBuilder in spannableList) {
            addContent(spannableStringBuilder)
        }

    }

    private fun buildSpannableString(questionData: ArrayList<TypeTextData>?,partList:ArrayList<SpannableStringBuilder>) {
        val stemSsb = SpannableStringBuilder()
        val imageSsbList = ArrayList<SpannableStringBuilder>()
        questionData?.forEach { typeTextData ->
            when (typeTextData.type) {
                5 -> {//5 代表啥？
                    val sourceJson = typeTextData.text
                    val jsonArray = gson.fromJson(sourceJson, JsonArray::class.java)
                    for (jsonElement in jsonArray) {
                        val questionResp = gson.fromJson(jsonElement, QuestionResp::class.java)
                        when (questionResp.typeEnum) {
                            "text" -> {//文字部分
                                stemSsb.append(questionResp.content)
                            }
                            "latex" -> {//图片部分
                                val w = questionResp.style?.width?.toInt() ?: 0
                                val h = questionResp.style?.height?.toInt() ?: 0

                                localPath.get(questionResp.content)?.run {
                                    questionResp.content = this
                                }
                                val pdfBitmap =
                                    transitBitmapForPdf(questionResp.content ?: "", w, h)
                                stemSsb.append("图片")
                                val centerImageSpan = CenterImageSpan(context,pdfBitmap)
                                stemSsb.setSpan(
                                    centerImageSpan,
                                    stemSsb.length - 2,
                                    stemSsb.length,
                                    SpannableString.SPAN_INCLUSIVE_EXCLUSIVE
                                )
                            }
                            "image"->{
                                if (questionResp.style != null && questionResp?.style?.src != null) {
                                    localPath.get(questionResp.content)?.run {
                                        questionResp.content = this
                                    }
                                    val w = questionResp.style?.width?.toInt() ?: 0
                                    val h = questionResp.style?.height?.toInt() ?: 0
                                    val imageSsb = SpannableStringBuilder()
                                    imageSsb.append("图片")
                                    val transitBitmapForPdf =
                                        transitBitmapForPdf(questionResp.content ?: "", w, h)
                                    imageSsb.setSpan(ImageSpan(context,transitBitmapForPdf),0,imageSsb.length,Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                                    imageSsbList.add(imageSsb)
                                }
                            }
                            "br"->{//？？换行？
                                stemSsb.append("\n")
                            }
                            else -> {
                            }
                        }
                    }
                }
            }
        }
        stemSsb.setSpan(AbsoluteSizeSpan(Math.round(12*context.resources.displayMetrics.density)),0,stemSsb.length,Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        partList.add(stemSsb)
        partList.addAll(imageSsbList)
    }

    private fun buildOptionSpannableString(questionData: ArrayList<TypeTextData>?, spannableList: ArrayList<SpannableStringBuilder>) {
        questionData?.forEach { typeTextData ->
            when (typeTextData.type) {
                5 -> {//5 代表啥？
                    val sourceJson = typeTextData.text
                    val jsonArray = gson.fromJson(sourceJson, JsonArray::class.java)
                    for (outElement in jsonArray) {
                        val optionSsb = SpannableStringBuilder()
                        for (jsonElement in outElement.asJsonArray) {
                            val questionResp =
                                gson.fromJson(jsonElement, QuestionResp::class.java)
                            when (questionResp.typeEnum) {
                                "text" -> {//文字部分
                                    optionSsb.append(questionResp.content)
                                }
                                "latex","image" -> {//图片部分
                                    val w = questionResp.style?.width?.toInt() ?: 0
                                    val h = questionResp.style?.height?.toInt() ?: 0

                                    localPath.get(questionResp.content)?.run {
                                        questionResp.content = this
                                    }

                                    val pdfBitmap =
                                        transitBitmapForPdf(questionResp.content ?: "", w, h)
                                    optionSsb.append("图片")
                                    val centerImageSpan = CenterImageSpan(context,pdfBitmap)
                                    optionSsb.setSpan(
                                        centerImageSpan,
                                        optionSsb.length - 2,
                                        optionSsb.length,
                                        SpannableString.SPAN_INCLUSIVE_EXCLUSIVE
                                    )
                                }
                                "image" -> {//图片部分
                                    val w = questionResp.style?.width?.toInt() ?: 0
                                    val h = questionResp.style?.height?.toInt() ?: 0

                                    localPath.get(questionResp.content)?.run {
                                        questionResp.content = this
                                    }

                                    val pdfBitmap =
                                        transitBitmapForPdf(questionResp.content ?: "", w, h)
                                    optionSsb.append("图片")
                                    val centerImageSpan = CenterImageSpan(context,pdfBitmap)
                                    optionSsb.setSpan(
                                        centerImageSpan,
                                        optionSsb.length - 2,
                                        optionSsb.length,
                                        SpannableString.SPAN_INCLUSIVE_EXCLUSIVE
                                    )
                                }
                                "br"->{//？？换行？
                                    optionSsb.append("\n")
                                }
                                else -> {
                                }
                            }
                        }
                        optionSsb.setSpan(AbsoluteSizeSpan(Math.round(12*context.resources.displayMetrics.density)),0,optionSsb.length,Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                        spannableList.add(optionSsb)
                    }
                }
            }
        }
    }


    fun addContent(spannableString: SpannableStringBuilder): PdfBuilder {
        val textPart =
            TextPart(spannableString, pageWidth)
        if (document.pages.isEmpty()) {
            document.pages.add(Page(1,20,20))
        }
        val lastestPage = document.pages.last()
        //高度是否还有富余
        val remainHeight = lastestPage.getRemainHeight(pageHeight)
        val measureHeight = textPart.measureSize()
        if (measureHeight <= remainHeight - 10) {
            //如果可以绘制
            lastestPage.parts.add(textPart)
        } else {
            //如果不能绘制 看看最有一个 part 是否能通过拆分达到容量
            var tempPart = textPart.canSplit(remainHeight)
            if (tempPart == null) {//无法拆分 将 textpart 加入到下一页
                val newPage = Page(lastestPage.pageIndex + 1,20,20)
                document.pages.add(newPage)
                newPage.parts.add(textPart)
            } else {
                //textPart 是原页面的
                lastestPage.parts.add(textPart)
                val newPage = Page(lastestPage.pageIndex + 1,20,20)
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
                builder.setContentRect(Rect(0,20,pageWidth,pageHeight-20))
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
            document.pages.clear()
            writeToFile()
        }.start()
    }

    private fun writeToFile() {
        val file =
            File("${context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.path}/${System.currentTimeMillis()}.pdf")
        file.createNewFile()
        pdfDocument.writeTo(file.outputStream())
        pdfDocument.close()
        pdfDocument = PdfDocument()
        Log.i("TAG", "${file.path}")
    }


    inner class Document {
        val pages = ArrayList<Page>()
    }

    inner class Page(val pageIndex: Int,val topPadding:Int,val bottomPadding:Int) {
        val parts = ArrayList<Part>()
        init {
            //每一初次创建 添加页首
            parts.add(
                HeadPart(
                    context,
                    pageIndex,
                    pageWidth,
                    50,
                    25
                )
            )
            parts.add(HeadLinePart())
        }
        fun getRemainHeight(height: Int): Int {
            var h = height-topPadding-bottomPadding
            for (part in parts) {
                h -= part.measureSize()
            }
            return h
        }
    }
}