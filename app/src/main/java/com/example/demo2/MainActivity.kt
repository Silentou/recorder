package com.example.demo2

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.demo2.databinding.ActivityMainBinding
import com.example.demo2.ui.theme.Post
import com.example.demo2.ui.theme.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val call = RetrofitClient.instance.getPosts()

        call.enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                if (response.isSuccessful) {
                    val posts = response.body()
                    Log.d("onResponse", "onResponse: $posts")
                } else {
                    Log.d("onResponse", "onResponse: not working")
                }
            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                Log.d("onResponse", "onResponse: $t")
            }
        })
    }
}



