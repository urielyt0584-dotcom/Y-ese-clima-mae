package com.example.data.network

import com.example.data.model.GeocodingResponse
import com.example.data.model.OpenMeteoResponse
import com.example.data.model.WeatherApiResponse
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface WeatherApiService {

    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code,wind_speed_10m,wind_direction_10m,uv_index",
        @Query("hourly") hourly: String = "temperature_2m,weather_code,precipitation_probability",
        @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,precipitation_probability_max,uv_index_max",
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoResponse
}

interface WeatherApiDotComService {
    @GET("v1/forecast.json")
    suspend fun getForecast(
        @Query("key") key: String,
        @Query("q") query: String,
        @Query("days") days: Int = 3,
        @Query("aqi") aqi: String = "no",
        @Query("alerts") alerts: String = "no"
    ): WeatherApiResponse
}

interface GeocodingApiService {

    @GET("v1/search")
    suspend fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = 6,
        @Query("language") language: String = "es",
        @Query("format") format: String = "json"
    ): GeocodingResponse
}

object NetworkClient {
    // Uses compile-time KSP-generated adapters with zero reflection
    private val moshi = Moshi.Builder()
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .writeTimeout(6, TimeUnit.SECONDS)
        .build()

    private val weatherRetrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val geocodingRetrofit = Retrofit.Builder()
        .baseUrl("https://geocoding-api.open-meteo.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val weatherApiDotComRetrofit = Retrofit.Builder()
        .baseUrl("https://api.weatherapi.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val weatherService: WeatherApiService = weatherRetrofit.create(WeatherApiService::class.java)
    val geocodingService: GeocodingApiService = geocodingRetrofit.create(GeocodingApiService::class.java)
    val weatherApiDotComService: WeatherApiDotComService = weatherApiDotComRetrofit.create(WeatherApiDotComService::class.java)
}
