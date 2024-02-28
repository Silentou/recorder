package com.example.demo2.ui.theme

import retrofit2.Call
import retrofit2.http.GET

interface Api {

    @GET("/posts")
    fun getPosts(): Call<List<Post>>

}
