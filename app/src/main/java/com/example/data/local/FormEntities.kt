package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_forms")
data class SavedFormEntity(
    @PrimaryKey
    val url: String,
    val title: String,
    val lastVisitedTimestamp: Long,
    val isFavorite: Boolean = false
)

@Entity(tableName = "saved_answers")
data class SavedAnswerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val questionText: String,
    val recommendedAnswer: String,
    val explanation: String,
    val keyConcept: String,
    val formUrl: String,
    val timestamp: Long
)
