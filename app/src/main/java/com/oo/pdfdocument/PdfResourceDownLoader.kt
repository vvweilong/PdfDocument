package com.oo.pdfdocument

import android.content.Context
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.LinkedBlockingQueue

/**
* create by 朱晓龙 2020/1/17 9:02 PM
 *
*/
object PdfResourceDownLoader {

    private val handlerThread  : HandlerThread = HandlerThread("pdfDownloader")
    private val mRequestHandler:Handler
    private val mainHandler : Handler

    private val linkedBlockingQueue = LinkedBlockingQueue<String>()

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
        for (url in urls) {
            linkedBlockingQueue.add(url)
        }
        val result = HashMap<String,String>()
        val requestCallback = object : RequestCallback {
            override fun success(url: String, response: String?) {
                if (response != null) {
                    result.put(url,response)
                }
                if(linkedBlockingQueue.isNullOrEmpty()){
                    callback.success(result)
                    return
                }
                val takeUrl = linkedBlockingQueue.take()
                startdownImage(context,takeUrl,this)
            }

            override fun failure(url: String, response: String?) {
                if(linkedBlockingQueue.isNullOrEmpty()){
                    callback.success(result)
                    return
                }
                val takeUrl = linkedBlockingQueue.take()
                startdownImage(context,takeUrl,this)
            }
        }

        val takeUrl = linkedBlockingQueue.take()
        startdownImage(context,takeUrl,requestCallback)

    }

    private fun buildConnect(url:String):HttpURLConnection{
        val url = URL(url)
        val openConnection = url.openConnection() as HttpURLConnection
        openConnection.connectTimeout=15*1000
        openConnection.readTimeout=20*1000
        openConnection.requestMethod="GET"
        return openConnection
    }

    fun startdownImage(context: Context,url: String,callback: RequestCallback){
        mRequestHandler.post {
            val connect = buildConnect(url)
            try {
                connect.connect()
            if (connect.responseCode == HttpURLConnection.HTTP_OK) {
                val readBytes = connect.inputStream.readBytes()

                val file = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                    "${System.currentTimeMillis()}.png"
                )
                file.createNewFile()
                val outputStream = file.outputStream()
                outputStream.write(readBytes)
                outputStream.flush()
                outputStream.close()

                connect.inputStream.close()
                connect.disconnect()

                mainHandler.post {
                    callback.success(url,file.path)
                }
            }else{
                mainHandler.post {
                    callback.failure(url,"failure")
                }
            }
            } catch (e: Exception) {
                e.printStackTrace()
                callback.failure(url,"connectfail")
            }
        }
    }

    interface MultiRequestCallback{
        fun success(paths:HashMap<String,String>)
    }

    fun startRequest(connect:HttpURLConnection,callback:RequestCallback){
        mRequestHandler.post {
            connect.connect()
            if (connect.responseCode == HttpURLConnection.HTTP_OK) {
                val inputStreamReader = InputStreamReader(connect.inputStream)
                val bufferedReader = BufferedReader(inputStreamReader)
                val readText = bufferedReader.readText()
                mainHandler.post {
                    callback.success(connect.url.toString(),readText)
                }
            }else{
                val inputStreamReader = InputStreamReader(connect.inputStream)
                val bufferedReader = BufferedReader(inputStreamReader)
                val readText = bufferedReader.readText()
                mainHandler.post {
                    callback.failure(connect.url.toString(),readText)
                }
            }
        }
    }
    interface RequestCallback{
        fun success(url:String,response:String?)
        fun failure(url:String,response:String?)
    }
    interface DownloadCallback{
        fun imageDownloaded()
    }
}