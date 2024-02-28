package com.example.demo2.ui.theme

import com.google.gson.annotations.SerializedName

data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)
