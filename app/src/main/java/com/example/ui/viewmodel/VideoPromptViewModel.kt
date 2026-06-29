package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.VideoPromptEntity
import com.example.data.model.VideoPlanResponse
import com.example.data.repository.VideoPromptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface UiState {
    object Idle : UiState
    object Loading : UiState
    data class Success(val response: VideoPlanResponse) : UiState
    data class Error(val message: String) : UiState
}

data class ScreenState(
    val category: String = "",
    val duration: Int = 30, // 15 to 45 seconds
    val aspectRatio: String = "9:16 (TikTok/Reels)",
    val style: String = "Cinematic Photorealistic",
    val mood: String = "Dramatic & Epic",
    val cameraMovement: String = "Dynamic Drone Shot",
    val targetModel: String = "Runway Gen-3",
    val lightingPreset: String = "Moody Cinematic Golden Hour",
    val cameraLens: String = "Cinematic 35mm",
    val motionPacing: String = "Real-time Cinematic",
    val characterConsistency: String = "None (Environment/Abstract)",
    val colorGrading: String = "Hollywood Teal & Orange",
    val cinematicTransitions: String = "Seamless Whip Pan",
    val specialEffects: String = "None (Realistic Physical Depth)",
    val cinematicFraming: String = "Rule of Thirds Depth",
    val environmentPhysics: String = "Volumetric Fog & Ray-Traced Light",
    val multiCharacterInteraction: String = "None (Focus on Main Action)",
    val activeTab: Int = 0, // 0 = Prompt Creator, 1 = Saved Library
    val savedPrompts: List<VideoPromptEntity> = emptyList(),
    val apiState: UiState = UiState.Idle,
    val selectedHistoryPlan: VideoPromptEntity? = null
)

class VideoPromptViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VideoPromptRepository
    private val _state = MutableStateFlow(ScreenState())
    val state: StateFlow<ScreenState> = _state.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = VideoPromptRepository(database.videoPromptDao())

        // Load saved prompts
        viewModelScope.launch {
            repository.allPrompts.collect { prompts ->
                _state.update { it.copy(savedPrompts = prompts) }
            }
        }
    }

    fun setCategory(category: String) {
        _state.update { it.copy(category = category) }
    }

    fun setDuration(duration: Int) {
        _state.update { it.copy(duration = duration) }
    }

    fun setAspectRatio(aspectRatio: String) {
        _state.update { it.copy(aspectRatio = aspectRatio) }
    }

    fun setStyle(style: String) {
        _state.update { it.copy(style = style) }
    }

    fun setMood(mood: String) {
        _state.update { it.copy(mood = mood) }
    }

    fun setCameraMovement(cameraMovement: String) {
        _state.update { it.copy(cameraMovement = cameraMovement) }
    }

    fun setTargetModel(targetModel: String) {
        _state.update { it.copy(targetModel = targetModel) }
    }

    fun setLightingPreset(lightingPreset: String) {
        _state.update { it.copy(lightingPreset = lightingPreset) }
    }

    fun setCameraLens(cameraLens: String) {
        _state.update { it.copy(cameraLens = cameraLens) }
    }

    fun setMotionPacing(motionPacing: String) {
        _state.update { it.copy(motionPacing = motionPacing) }
    }

    fun setCharacterConsistency(characterConsistency: String) {
        _state.update { it.copy(characterConsistency = characterConsistency) }
    }

    fun setColorGrading(colorGrading: String) {
        _state.update { it.copy(colorGrading = colorGrading) }
    }

    fun setCinematicTransitions(cinematicTransitions: String) {
        _state.update { it.copy(cinematicTransitions = cinematicTransitions) }
    }

    fun setSpecialEffects(specialEffects: String) {
        _state.update { it.copy(specialEffects = specialEffects) }
    }

    fun setCinematicFraming(cinematicFraming: String) {
        _state.update { it.copy(cinematicFraming = cinematicFraming) }
    }

    fun setEnvironmentPhysics(environmentPhysics: String) {
        _state.update { it.copy(environmentPhysics = environmentPhysics) }
    }

    fun setMultiCharacterInteraction(multiCharacterInteraction: String) {
        _state.update { it.copy(multiCharacterInteraction = multiCharacterInteraction) }
    }

    fun setActiveTab(tab: Int) {
        _state.update { it.copy(activeTab = tab) }
    }

    fun generateVideoPlan() {
        val currentState = _state.value
        if (currentState.category.isBlank()) {
            _state.update { it.copy(apiState = UiState.Error("Please enter a category or prompt description.")) }
            return
        }

        _state.update { it.copy(apiState = UiState.Loading, selectedHistoryPlan = null) }

        viewModelScope.launch {
            val result = repository.generateVideoPlan(
                category = currentState.category,
                duration = currentState.duration,
                aspectRatio = currentState.aspectRatio,
                style = currentState.style,
                mood = currentState.mood,
                cameraMovement = currentState.cameraMovement,
                targetModel = currentState.targetModel,
                lightingPreset = currentState.lightingPreset,
                cameraLens = currentState.cameraLens,
                motionPacing = currentState.motionPacing,
                characterConsistency = currentState.characterConsistency,
                colorGrading = currentState.colorGrading,
                cinematicTransitions = currentState.cinematicTransitions,
                specialEffects = currentState.specialEffects,
                cinematicFraming = currentState.cinematicFraming,
                environmentPhysics = currentState.environmentPhysics,
                multiCharacterInteraction = currentState.multiCharacterInteraction
            )

            result.onSuccess { response ->
                _state.update { it.copy(apiState = UiState.Success(response)) }
                
                // Auto-save generated prompt plan to Room
                val entity = VideoPromptEntity(
                    category = currentState.category,
                    duration = currentState.duration,
                    aspectRatio = currentState.aspectRatio,
                    style = currentState.style,
                    mood = currentState.mood,
                    cameraMovement = currentState.cameraMovement,
                    targetModel = currentState.targetModel,
                    lightingPreset = currentState.lightingPreset,
                    cameraLens = currentState.cameraLens,
                    motionPacing = currentState.motionPacing,
                    characterConsistency = currentState.characterConsistency,
                    colorGrading = currentState.colorGrading,
                    cinematicTransitions = currentState.cinematicTransitions,
                    specialEffects = currentState.specialEffects,
                    cinematicFraming = currentState.cinematicFraming,
                    environmentPhysics = currentState.environmentPhysics,
                    multiCharacterInteraction = currentState.multiCharacterInteraction,
                    masterPrompt = response.masterPrompt,
                    concept = response.concept,
                    musicPrompt = response.musicPrompt,
                    tags = response.tags,
                    scenes = response.scenes,
                    colorPalette = response.colorPalette
                )
                repository.savePrompt(entity)
            }.onFailure { exception ->
                _state.update { it.copy(apiState = UiState.Error(exception.message ?: "An unknown error occurred.")) }
            }
        }
    }

    fun deletePrompt(id: Int) {
        viewModelScope.launch {
            repository.deletePrompt(id)
            if (_state.value.selectedHistoryPlan?.id == id) {
                _state.update { it.copy(selectedHistoryPlan = null) }
            }
        }
    }

    fun selectHistoryPlan(entity: VideoPromptEntity) {
        _state.update {
            it.copy(
                selectedHistoryPlan = entity,
                apiState = UiState.Idle, // Reset live generation tab state to clear active live card
                activeTab = 0 // Switch to the generator/viewer tab
            )
        }
    }

    fun clearActivePlan() {
        _state.update { it.copy(selectedHistoryPlan = null, apiState = UiState.Idle) }
    }
}
