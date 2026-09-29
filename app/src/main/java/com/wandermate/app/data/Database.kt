package com.wandermate.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TravelDao {
    @Query("SELECT * FROM trips ORDER BY rowid DESC") fun trips(): Flow<List<TripEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun save(trip: TripEntity)
    @Query("DELETE FROM trips WHERE id = :id") suspend fun deleteTrip(id: String)
    @Query("SELECT * FROM favorites") fun favorites(): Flow<List<FavoriteEntity>>
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun favorite(favorite: FavoriteEntity)
    @Query("DELETE FROM favorites WHERE placeId = :id") suspend fun unfavorite(id: String)
    @Query("SELECT * FROM weather WHERE destinationId = :id") suspend fun weather(id: String): WeatherEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveWeather(weather: WeatherEntity)
}
@Database(entities = [TripEntity::class, FavoriteEntity::class, WeatherEntity::class], version = 1, exportSchema = true)
abstract class TravelDatabase : RoomDatabase() { abstract fun dao(): TravelDao }
