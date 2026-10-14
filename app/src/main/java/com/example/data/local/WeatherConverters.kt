package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.DailyForecastItem
import com.example.data.model.HourlyForecastItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class WeatherConverters {

    // Uses KSP-generated adapters directly without reflection
    private val moshi = Moshi.Builder()
        .build()

    private val hourlyListType = Types.newParameterizedType(List::class.java, HourlyForecastItem::class.java)
    private val hourlyAdapter = moshi.adapter<List<HourlyForecastItem>>(hourlyListType)

    private val dailyListType = Types.newParameterizedType(List::class.java, DailyForecastItem::class.java)
    private val dailyAdapter = moshi.adapter<List<DailyForecastItem>>(dailyListType)

    @TypeConverter
    fun fromHourlyList(list: List<HourlyForecastItem>?): String {
        return if (list.isNullOrEmpty()) "[]" else hourlyAdapter.toJson(list)
    }

    @TypeConverter
    fun toHourlyList(json: String?): List<HourlyForecastItem> {
        if (json.isNullOrBlank() || json == "[]") return emptyList()
        return try {
            hourlyAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromDailyList(list: List<DailyForecastItem>?): String {
        return if (list.isNullOrEmpty()) "[]" else dailyAdapter.toJson(list)
    }

    @TypeConverter
    fun toDailyList(json: String?): List<DailyForecastItem> {
        if (json.isNullOrBlank() || json == "[]") return emptyList()
        return try {
            dailyAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
