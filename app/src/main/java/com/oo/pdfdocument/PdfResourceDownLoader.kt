package com.oo.pdfdocument

import android.content.Context
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import java.io.File

/**
* create by 朱晓龙 2020/1/17 9:02 PM
 *
*/
object PdfResourceDownLoader {

    private val handlerThread  : HandlerThread = HandlerThread("pdfDownloader")
    private val mRequestHandler:Handler
    private val mainHandler : Handler


    init {
        handlerThread.start()
        mRequestHandler=Handler(handlerThread.looper)
        mainHandler = Handler(Looper.getMainLooper())
    }
    fun clearHistory(context: Context){
        val file = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        deleteChildFiles(file)
    }
    private fun deleteChildFiles(dir:File?):Boolean{
        if (dir==null) {
            return true
        }
        if (dir.listFiles().isNullOrEmpty()) {
            return dir.delete()
        }
        val iterator = dir.listFiles()?.iterator()
        var currentDir :File?=null
        while (iterator?.hasNext()==true){
            currentDir=iterator.next()
            if (deleteChildFiles(currentDir).not()) {
                return false
            }
        }
        return true
    }

    fun downloadImages(context: Context,urls:ArrayList<String>,callback: MultiRequestCallback){
        clearHistory(context)
        val resourceDownLoader = ResourceDownLoader(context)
        resourceDownLoader.setResUrls(urls);
        var time = System.currentTimeMillis()
        resourceDownLoader.setDownLoadListener {
            Log.i("PdfBuilder", "finish download ${System.currentTimeMillis()-time}")
            callback.success(it)
        }
        resourceDownLoader.startDownload()
    }

    interface MultiRequestCallback{
        fun success(paths:HashMap<String,String>)
    }
}