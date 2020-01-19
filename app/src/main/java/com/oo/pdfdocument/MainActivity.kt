package com.oo.pdfdocument

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.gson.Gson
import com.oo.pdfdocument.bean.DataResponse
import com.oo.pdfdocument.pdfbuilder.PdfBuilder
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {


    var pdfBuilder = PdfBuilder(this)

    var count = 0

    val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        pdfBuilder.setPageSize(300, 300)

        findViewById<Button>(R.id.add_text).setOnClickListener {
            val writePermission = ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            val readPermission = ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

            if (writePermission && readPermission) {
                val open = assets.open("response.json")
                val readText = BufferedReader(InputStreamReader(open)).readText()
                val response = gson.fromJson(readText, DataResponse::class.java)
                pdfBuilder.prepareImageResouce(this,response,object :PdfBuilder.ResourcePrepareCallback{
                    override fun onPrepared() {
                        Toast.makeText(this@MainActivity, "资源下载完成", Toast.LENGTH_SHORT).show()
                        pdfBuilder.create()
                    }
                })
            }
        }

//        findViewById<Button>(R.id.create).setOnClickListener {
//
//            if (ActivityCompat.checkSelfPermission(
//                    this,
//                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
//                ) == PackageManager.PERMISSION_GRANTED
//            ) {
//                pdfBuilder.create()
//                pdfBuilder = PdfBuilder(this)
//            } else {
//                ActivityCompat.requestPermissions(
//                    this,
//                    arrayOf(
//                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                        android.Manifest.permission.READ_EXTERNAL_STORAGE
//                    ),
//                    0
//                )
//            }
//        }
    }


    override fun onResume() {
        super.onResume()

    }
}
