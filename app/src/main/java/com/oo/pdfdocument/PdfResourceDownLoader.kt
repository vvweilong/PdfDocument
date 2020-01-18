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

    private val linkedBlockingQueue = LinkedBlockingQueue<HttpURLConnection>()

    init {
        handlerThread.start()
        mRequestHandler=Handler(handlerThread.looper)
        mainHandler = Handler(Looper.getMainLooper())
    }

    fun downloadImage(context: Context,url:String,callback:RequestCallback){
        val buildConnect = buildConnect(url)
        startdownImage(context,buildConnect,callback)
    }


    fun downloadImages(context: Context,urls:ArrayList<String>,callback: MultiRequestCallback){
        for (url in urls) {
            linkedBlockingQueue.add(buildConnect(url))
        }

        val results = ArrayList<Pair<String,String>>()

        val multCallback=object :RequestCallback{
            override fun success(response: String?) {
                if (linkedBlockingQueue.isNotEmpty()) {
                    val poll = linkedBlockingQueue.take()
                    if (response != null) {
                        results.add(Pair(poll.url.toString(),response))
                    }
                    startdownImage(context,poll,this)
                }else{
                    callback.success(results)
                }
            }

            override fun failure(response: String?) {
                if (linkedBlockingQueue.isNotEmpty()) {
                    val poll = linkedBlockingQueue.take()
                    startdownImage(context,poll,this)
                }else{
                    callback.success(results)
                }
            }
        }
        val poll = linkedBlockingQueue.take()
        startdownImage(context,poll,multCallback )

    }

    private fun buildConnect(url:String):HttpURLConnection{
        val url = URL(url)
        val openConnection = url.openConnection() as HttpURLConnection
        openConnection.connectTimeout=15*1000
        openConnection.readTimeout=20*1000
        openConnection.requestMethod="GET"
        return openConnection
    }

    fun startdownImage(context: Context,connect: HttpURLConnection,callback: RequestCallback){
        mRequestHandler.post {
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
                    callback.success(file.path)
                }
            }else{
                mainHandler.post {
                    callback.failure("failure")
                }
            }
            } catch (e: Exception) {
                e.printStackTrace()
                callback.failure("connectfail")
            }
        }
    }

    interface MultiRequestCallback{
        fun success(paths:ArrayList<Pair<String,String>>)
    }

    fun startRequest(connect:HttpURLConnection,callback:RequestCallback){
        mRequestHandler.post {
            connect.connect()
            if (connect.responseCode == HttpURLConnection.HTTP_OK) {
                val inputStreamReader = InputStreamReader(connect.inputStream)
                val bufferedReader = BufferedReader(inputStreamReader)
                val readText = bufferedReader.readText()
                mainHandler.post {
                    callback.success(readText)
                }
            }else{
                val inputStreamReader = InputStreamReader(connect.inputStream)
                val bufferedReader = BufferedReader(inputStreamReader)
                val readText = bufferedReader.readText()
                mainHandler.post {
                    callback.failure(readText)
                }
            }
        }
    }
    interface RequestCallback{
        fun success(response:String?)
        fun failure(response:String?)
    }
    interface DownloadCallback{
        fun imageDownloaded()
    }
}