package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VinSearchDao {
    @Query("SELECT * FROM vin_searches ORDER BY searchedAt DESC")
    fun getAllSearches(): Flow<List<VinSearchEntity>>

    @Query("SELECT * FROM vin_searches WHERE isFavorite = 1 ORDER BY searchedAt DESC")
    fun getFavoriteSearches(): Flow<List<VinSearchEntity>>

    @Query("SELECT * FROM vin_searches WHERE vin = :vin LIMIT 1")
    suspend fun getSearchByVin(vin: String): VinSearchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: VinSearchEntity)

    @Query("UPDATE vin_searches SET isFavorite = :isFavorite WHERE vin = :vin")
    suspend fun updateFavorite(vin: String, isFavorite: Boolean)

    @Query("DELETE FROM vin_searches WHERE vin = :vin")
    suspend fun deleteSearchByVin(vin: String)

    @Query("DELETE FROM vin_searches")
    suspend fun clearAllSearches()
}
