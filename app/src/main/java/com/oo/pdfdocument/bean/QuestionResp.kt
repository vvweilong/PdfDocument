package com.oo.pdfdocument.bean

data class QuestionResp(
    var content: String?,
    var source: String?,
    var style: Style?,
    var typeEnum: String?
)

data class Style(
    var height: String?,
    var src: String?,
    var width: String?
)