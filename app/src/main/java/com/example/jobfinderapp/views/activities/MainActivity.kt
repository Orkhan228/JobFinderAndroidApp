package com.example.jobfinderapp.views.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.jobfinderapp.ApiConst
import com.example.jobfinderapp.R
import com.example.jobfinderapp.RetrofitService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val retrofit = Retrofit.Builder()
            .baseUrl(ApiConst.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create<RetrofitService>(RetrofitService::class.java)

        lifecycleScope.launch {
            val res = service.getExample(ApiConst.APP_ID, ApiConst.API_KEY)
            if (res.isSuccessful) {
                val body = res.body()
                println("!!! $body")
            } else {
                println("!!! ${res.errorBody()}")
            }
        }
    }
}