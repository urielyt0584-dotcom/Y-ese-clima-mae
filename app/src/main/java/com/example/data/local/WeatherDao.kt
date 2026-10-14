package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    @Query("SELECT * FROM cached_weather WHERE isLatest = 1 LIMIT 1")
    fun getLatestWeatherFlow(): Flow<CachedWeatherEntity?>

    @Query("SELECT * FROM cached_weather WHERE isLatest = 1 LIMIT 1")
    suspend fun getLatestWeatherOnce(): CachedWeatherEntity?

    @Query("SELECT * FROM cached_weather WHERE locationKey = :key LIMIT 1")
    suspend fun getWeatherByKey(key: String): CachedWeatherEntity?

    @Query("SELECT * FROM cached_weather WHERE cityName LIKE '%' || :name || '%' ORDER BY cachedAt DESC LIMIT 1")
    suspend fun getWeatherByCity(name: String): CachedWeatherEntity?

    @Query("SELECT * FROM cached_weather ORDER BY cachedAt DESC")
    fun getAllCachedWeather(): Flow<List<CachedWeatherEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: CachedWeatherEntity)

    @Query("UPDATE cached_weather SET isLatest = 0 WHERE isLatest = 1")
    suspend fun clearLatestFlag()

    @Transaction
    suspend fun saveLatestWeather(entity: CachedWeatherEntity) {
        clearLatestFlag()
        insertOrUpdate(entity.copy(isLatest = true))
    }

    @Query("DELETE FROM cached_weather WHERE cachedAt < :threshold")
    suspend fun deleteOlderThan(threshold: Long)
}
