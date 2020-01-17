package com.oo.pdfdocument

/**
* create by 朱晓龙 2020/1/17 10:53 AM
 * 数据转换接口
*/
interface IDataTranslate<T> {
    fun translate(json:String?):T
}