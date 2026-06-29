package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ScenePlan

@Entity(tableName = "video_prompts")
data class VideoPromptEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val duration: Int,
    val aspectRatio: String,
    val style: String,
    val mood: String,
    val cameraMovement: String,
    val targetModel: String,
    val lightingPreset: String,
    val cameraLens: String = "Cinematic 35mm",
    val motionPacing: String = "Real-time Cinematic",
    val characterConsistency: String = "None",
    val colorGrading: String = "Hollywood Teal & Orange",
    val cinematicTransitions: String = "Seamless Whip Pan",
    val specialEffects: String = "None",
    val cinematicFraming: String = "Rule of Thirds Depth",
    val environmentPhysics: String = "Volumetric Fog",
    val multiCharacterInteraction: String = "None",
    val masterPrompt: String,
    val concept: String,
    val musicPrompt: String,
    val tags: List<String>,
    val scenes: List<ScenePlan>,
    val colorPalette: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)
