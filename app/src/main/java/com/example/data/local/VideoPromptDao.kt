package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoPromptDao {
    @Query("SELECT * FROM video_prompts ORDER BY timestamp DESC")
    fun getAllPrompts(): Flow<List<VideoPromptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(prompt: VideoPromptEntity): Long

    @Query("DELETE FROM video_prompts WHERE id = :id")
    suspend fun deletePromptById(id: Int)
}
