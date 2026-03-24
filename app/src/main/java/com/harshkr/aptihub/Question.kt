package com.harshkr.aptihub

import androidx.annotation.Keep

@Keep
data class Question(
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val topic: String = ""
)
