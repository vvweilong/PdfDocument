package com.oo.pdfdocument.bean

data class DataResponse(
    var code: Int,
    var data: ArrayList<QuestionData>?,
    var error_info: String?,
    var msg: String?,
    var status: Int
)

data class QuestionData(
    var clazzLessonNumber: String?,
    var dataId: String?,
    var homeworkQuestionType: Int,
    var lastOption: String?,
    var lessonIdx: Int,
    var lessonName: String?,
    var questionAddSource: Int,
    var questionAnalysis: ArrayList<TypeTextData>?,
    var questionAnswer: ArrayList<TypeTextData>?,
    var questionBackId: String?,
    var questionBackType: Int,
    var questionOption: ArrayList<TypeTextData>?,
    var questionStem: ArrayList<TypeTextData>?,
    var similarProblem: Boolean,
    var wrongBookTag: ArrayList<WrongBookTag>?
)

data class TypeTextData(
    var text: String?,
    var type: Int
)


data class WrongBookTag(
    var code: Int,
    var desc: String?,
    var idx: Int
)