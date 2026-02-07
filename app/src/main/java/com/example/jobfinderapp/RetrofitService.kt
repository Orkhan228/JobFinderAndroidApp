package com.example.jobfinderapp

import com.example.jobfinderapp.entity.TopCompaniesDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface RetrofitService {
    @GET("jobs/gb/top_companies")
    suspend fun getExample(
        @Query("app_id") appId: String,
        @Query("app_key") apiKey: String,
        @Query("what") what: String = "cook",
        @Query("content-type") contentType: String = "application/json"
    ): Response<TopCompaniesDTO>
}