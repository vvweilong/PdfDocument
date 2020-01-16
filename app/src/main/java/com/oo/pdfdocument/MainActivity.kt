package com.oo.pdfdocument

import android.content.pm.PackageManager
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ImageSpan
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.oo.pdfdocument.pdfbuilder.ImageRender
import com.oo.pdfdocument.pdfbuilder.PdfBuilder
import com.oo.pdfdocument.pdfbuilder.UrlImageSpan

class MainActivity : AppCompatActivity() {


    var pdfBuilder = PdfBuilder(this)

    var count = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        pdfBuilder.setPageSize(300, 300)


        val spannableString = SpannableString("在文本中添加表情（表情）在文本中添加表情（在文本中添加表情" +
                "（在文本中添加表情（在文本中添加表情（在文本中添加表情（在文本中添加表情（在文本中添加表情（在文本中添加表情（在文本中添加表"+
                "（在文本中添加表情（在文本中添加表情（在文本中添加表情（在文本中添加表情（在文本中添加表情（在文本中添加表情（在文本中添加表"+
                "（在文本中添加表情（在文本中添加表情（在文本中添加表情（在文本中添加表情（在文本中添加表情（在文本中添加表情（在文本中添加表end")
        val drawable = resources.getDrawable(R.mipmap.ic_launcher);
        val textView = TextView(this)
        textView.text = spannableString

        drawable.setBounds(0, 0, 42, 42);
        val imageSpan = ImageSpan(drawable);
        spannableString.setSpan(imageSpan, 8, 12, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

        //设置网络图片
        val imgUrl ="https://jyimgs.gsxcdn.com/upload/image/question/20190328/2019032813351415321439.png"
        val size = arrayOf("117","137")
        val imageSpanNet = UrlImageSpan(this, imgUrl,textView,2,300,size)
        spannableString.setSpan(imageSpanNet,30,40,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        findViewById<Button>(R.id.add_text).setOnClickListener {
            pdfBuilder.addContent(spannableString)
            count += 1
        }

        findViewById<Button>(R.id.create).setOnClickListener {

            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                pdfBuilder.create()
                pdfBuilder = PdfBuilder(this)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    0
                )
            }
        }
    }


    override fun onResume() {
        super.onResume()

    }
}
