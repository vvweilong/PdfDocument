package com.oo.pdfdocument

import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.gson.Gson
import com.oo.pdfdocument.bean.DataResponse
import com.oo.pdfdocument.pdfbuilder.PdfBuilder
import java.io.BufferedReader
import java.io.InputStreamReader
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : AppCompatActivity(), TextWatcher {


    var pdfBuilder = PdfBuilder(this)

    var count = 0

    val gson = Gson()


    var originEt :EditText?=null
    var inputEt :TextView?=null
    var resultTv:TextView?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        pdfBuilder.setPageSize(720, 1280)

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
                        Toast.makeText(this@MainActivity, "资源下载完成", Toast.LENGTH_LONG).show()
                        pdfBuilder.create()
                    }
                })
            }else{
                val arrayOf = arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                ActivityCompat.requestPermissions(this,arrayOf,0)
            }
        }
    }

    override fun afterTextChanged(s: Editable?) {
        //当输入内容变化时
        if(s.isNullOrEmpty()||s.isBlank()){
            return
        }
        val toString = s.toString()
        val decimal = BigDecimal(toString)
        inputEt?.text = "${decimal.setScale(2,RoundingMode.DOWN)}"

        val inputDecimal = BigDecimal(inputEt?.text.toString())

        resultTv?.text = "${decimal.minus(inputDecimal).toPlainString()}  |  ${decimal.minus(inputDecimal).compareTo(
            BigDecimal.ZERO)}"

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }
}
