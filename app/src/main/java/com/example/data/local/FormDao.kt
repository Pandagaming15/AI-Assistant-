package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FormDao {
    @Query("SELECT * FROM saved_forms ORDER BY lastVisitedTimestamp DESC")
    fun getAllSavedForms(): Flow<List<SavedFormEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForm(form: SavedFormEntity)

    @Query("DELETE FROM saved_forms WHERE url = :url")
    suspend fun deleteForm(url: String)

    @Query("UPDATE saved_forms SET isFavorite = :isFavorite WHERE url = :url")
    suspend fun updateFavorite(url: String, isFavorite: Boolean)

    @Query("SELECT * FROM saved_answers ORDER BY timestamp DESC LIMIT 50")
    fun getRecentAnswers(): Flow<List<SavedAnswerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: SavedAnswerEntity)

    @Query("DELETE FROM saved_answers")
    suspend fun clearAnswerHistory()
}
