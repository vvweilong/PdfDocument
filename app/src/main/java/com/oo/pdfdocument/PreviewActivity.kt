package com.oo.pdfdocument

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.os.PersistableBundle
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.net.URI

class PreviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        findViewById<Button>(R.id.pick).setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, 1);
        }
    }

    override fun onResume() {
        super.onResume()
        val stringExtra = intent.getStringExtra("path")
        val file = File(stringExtra)
        if (!file.exists()) {
            Log.i("TAG", "onResume: ")
            return
        }

        val open = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)

        val pdfRenderer = PdfRenderer(open)
        val pdfPageAdapter = PdfPageAdapter(pdfRenderer)
        val list = findViewById<RecyclerView>(R.id.recycler_view)
            list.adapter =pdfPageAdapter

    }

//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (data != null) {
//            findViewById<TextView>(R.id.result_tv).text = data.data?.path
//            val uri = data.data
//
//
//            val docId = DocumentsContract.getDocumentId(uri);
//            val split = docId.split(":");
//            val type = split[0];
//            if ("primary".equals(type)) {
//                 Environment.getExternalStorageDirectory().path + "/" + split[1];
//            }
//
//
//
//        }
//
//    }


}