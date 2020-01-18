package com.oo.pdfdocument

import com.google.gson.Gson
import com.oo.pdfdocument.bean.PdfData

class PdfDataTranslater:IDataTranslate<PdfData> {
    override fun translate(json: String?): PdfData {
        return Gson().fromJson<PdfData>(json,
            PdfData::class.java)
    }
}