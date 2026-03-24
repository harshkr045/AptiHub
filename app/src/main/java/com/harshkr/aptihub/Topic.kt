package com.harshkr.aptihub

import androidx.annotation.DrawableRes

data class Topic(
    val id: String,
    val title: String,
    @param:DrawableRes val iconResource: Int,
    val questionCount: Int? = null
)
