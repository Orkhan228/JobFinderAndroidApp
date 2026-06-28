package com.example.jobfinderapp.utils

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val modifiedUrl = originalRequest.url.newBuilder()
            .addQueryParameter("app_id", ApiConst.APP_ID)
            .addQueryParameter("app_key", ApiConst.API_KEY )
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(modifiedUrl)
            .build()

        return chain.proceed(newRequest)
    }
}