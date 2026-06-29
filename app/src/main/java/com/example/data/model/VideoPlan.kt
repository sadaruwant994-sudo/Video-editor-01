package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VideoPlanResponse(
    val masterPrompt: String,
    val concept: String,
    val musicPrompt: String,
    val tags: List<String>,
    val scenes: List<ScenePlan>,
    val colorPalette: List<String>,
    val targetModel: String? = null,
    val lightingPreset: String? = null,
    val cameraLens: String? = null,
    val motionPacing: String? = null,
    val characterConsistency: String? = null,
    val colorGrading: String? = null,
    val cinematicTransitions: String? = null,
    val specialEffects: String? = null,
    val cinematicFraming: String? = null,
    val environmentPhysics: String? = null,
    val multiCharacterInteraction: String? = null
)

@JsonClass(generateAdapter = true)
data class ScenePlan(
    val sceneNumber: Int,
    val timeStart: Int, // in seconds from start
    val timeEnd: Int, // in seconds from start
    val visualPrompt: String,
    val cameraMovement: String,
    val voiceoverText: String,
    val moodColorHex: String // e.g., "#00FFCC" or "#FF007F"
)
