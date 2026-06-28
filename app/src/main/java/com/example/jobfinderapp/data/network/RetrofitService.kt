package com.example.jobfinderapp.data.network

import com.example.jobfinderapp.entity.JobDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface RetrofitService {
    //Метод, который получает подборку для показа при первом входе в приложение.
    @GET("jobs/{country}/search/{page}")
    suspend fun getGeneralList(
        @Path("country") countryCode: String,
        @Path("page") page: Int,
        @Query("app_id") appId: String,
        @Query("app_key") apiKey: String,
    ): Response<JobDTO>

    //Метод, для составления логики работы с фильтром и поиском.
    @GET("jobs/{country}/search/{page}")
    suspend fun getFilteredList(
        @Path("country") countryCode: String,
        @Path("page") page: Int,
        @Query("results_per_page") resultsPerPage: Int = 10,
        @Query("what") searchKeyWords: String?,
        @Query("category") categoryTag: String?,
        @Query("sort_dir") sortDirection: String?,
        @Query("sort_by") sortBy: String?,
        @Query("full_time") onlyFullTime: String?,
        @Query("part_time") onlyPartTime: String?,
        @Query("contract") onlyContractJobs: String?,
        @Query("permanent") onlyPermanentJobs: String?,
        @Query("location0") location0: String?,
        @Query("location1") location1: String?,
        @Query("location2") location2: String?,
        @Query("location3") location3: String?,
        @Query("location4") location4: String?,
        @Query("location5") location5: String?,
    ): Response<JobDTO>
}