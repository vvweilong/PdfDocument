package com.oo.pdfdocument

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.util.LruCache
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class PdfPageAdapter(val pdfRenderer: PdfRenderer): RecyclerView.Adapter<PageAdapter>() {

    val lruCache = LruCache<String,Bitmap>(2)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageAdapter {
        val imageView = ImageView(parent.context)
        return PageAdapter(imageView)
    }

    override fun onBindViewHolder(holder: PageAdapter, position: Int) {
        val imageView = holder.itemView as ImageView
        val openPage = pdfRenderer.openPage(position)
        val bitmapWidth=openPage.width
        val bitmapHeight=openPage.height
        val createBitmap = getBitmap(bitmapWidth,bitmapHeight,position)
        openPage.render(createBitmap,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        imageView.setImageBitmap(createBitmap)
        openPage.close()
    }

    private fun getBitmap(w:Int,h:Int,p:Int):Bitmap{
        var getBitmap:Bitmap? = lruCache.get("$w-$h-${p%2}")
        if (getBitmap == null) {
            getBitmap= Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            lruCache.put("$w-$h-${p%2}",getBitmap)
        }else{
            getBitmap.eraseColor(Color.TRANSPARENT)
        }
        return getBitmap!!
    }

    override fun getItemCount(): Int {
        return pdfRenderer.pageCount
    }
}



class PageAdapter(itemView: View) : RecyclerView.ViewHolder(itemView) {

}