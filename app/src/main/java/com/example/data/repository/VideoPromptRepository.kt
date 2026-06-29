package com.example.data.repository

import com.example.BuildConfig
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiConfig
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import com.example.data.api.RetrofitClient
import com.example.data.local.VideoPromptDao
import com.example.data.local.VideoPromptEntity
import com.example.data.model.VideoPlanResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class VideoPromptRepository(private val videoPromptDao: VideoPromptDao) {

    val allPrompts: Flow<List<VideoPromptEntity>> = videoPromptDao.getAllPrompts()

    suspend fun savePrompt(prompt: VideoPromptEntity): Long = withContext(Dispatchers.IO) {
        videoPromptDao.insertPrompt(prompt)
    }

    suspend fun deletePrompt(id: Int) = withContext(Dispatchers.IO) {
        videoPromptDao.deletePromptById(id)
    }

    suspend fun generateVideoPlan(
        category: String,
        duration: Int, // 15 to 45
        aspectRatio: String,
        style: String,
        mood: String,
        cameraMovement: String,
        targetModel: String,
        lightingPreset: String,
        cameraLens: String,
        motionPacing: String,
        characterConsistency: String,
        colorGrading: String,
        cinematicTransitions: String,
        specialEffects: String,
        cinematicFraming: String,
        environmentPhysics: String,
        multiCharacterInteraction: String
    ): Result<VideoPlanResponse> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Gemini API Key is missing. Please set it in the AI Studio Secrets panel."))
        }

        val prompt = """
            Create a highly creative, expert-level cinematic video generation blueprint for:
            - Category/Theme/Subject: $category
            - Target AI Video Model: $targetModel
            - Duration: $duration seconds (Chronological, exact 5-second consecutive scene blocks!)
            - Aspect Ratio: $aspectRatio
            - Visual Style: $style
            - Mood/Tone: $mood
            - Core Camera Movement Style (Hollywood preset): $cameraMovement
            - Lighting & Artistic Preset: $lightingPreset
            - Camera Lens Type: $cameraLens
            - Motion Pacing Speed: $motionPacing
            - Character Consistency details: $characterConsistency
            - Color Grading Style / LUT: $colorGrading
            - Cinematic Transitions style: $cinematicTransitions
            - Special Effects & CGI Layering (VFX): $specialEffects
            - Cinematic Framing & Composition: $cinematicFraming
            - Environment Physics & Weather Dynamics: $environmentPhysics
            - Multi-Character Interaction & Emotion: $multiCharacterInteraction
            
            Based on these parameters, perform the following tasks:
            1. Master Prompt: Craft a single, cohesive, paragraph-length 'Master Prompt' optimized specifically for the target model '$targetModel'.
               - If Sora is selected: emphasize hyper-realistic fluid physics, complex interactions, atmospheric depths, volumetric lighting, and extreme micro-details.
               - If Runway Gen-3 is selected: use technical camera motion tags (e.g., "[Camera: Slow Pan Left]", "[Speed: 1.0]", "high-speed shutter"), sharp digital texture, and cinematic tracking terms.
               - If Kling AI is selected: emphasize hyper-detailed rendering, realistic texture details, dynamic physical interactions, and exceptional consistency.
               Incorporate professional Hollywood film-industry terms and Unreal Engine 5 tags matching all inputs: '$cameraMovement', '$lightingPreset', '$cameraLens', '$motionPacing', '$colorGrading', '$specialEffects', '$cinematicFraming', '$environmentPhysics', and '$multiCharacterInteraction' seamlessly. Make it look like a highly technical prompt engineered for the best video AI models!
            2. Concept: Write a 1-sentence engaging description or marketing hook of this video.
            3. Music/Audio Prompt: Suggest a text prompt for AI music generators (like Suno or Udio) to compose fitting background music or soundscapes. It MUST include structural music tags inside brackets, e.g., [Drop], [Heavy Synth], [Intro], [Build-up], [Fade-out], to make it directly usable.
            4. Tags: List 4 to 6 relevant tags/keywords for indexing this video.
            5. Color Palette: Return exactly 4 cohesive Hex color codes in 'colorPalette' that match the visual mood of the theme, lighting preset, and color grading style.
            6. Scenes Storyboard: Break down the $duration seconds video into precise consecutive 5-second scene blocks (Scene 1: 0-5s, Scene 2: 5-10s, Scene 3: 10-15s, etc. until $duration seconds is reached).
               For each scene, specify:
               - The start and end times in seconds (e.g., Scene 1: 0-5, Scene 2: 5-10, etc.).
               - A specific detailed visual prompt for generating that individual shot using Hollywood film-industry and UE5 terms matching all selected inputs: '$cameraMovement', '$lightingPreset', '$cameraLens', '$colorGrading', '$specialEffects', and '$cinematicFraming'. Integrate the '$cinematicTransitions' naturally between shots to describe how the camera transitions.
               - Recommended camera movement.
               - Voiceover script or rich atmospheric audio/sound effect descriptions.
               - A vibrant 'moodColorHex' matching the emotional state of that scene (selected from or complementing the color palette).
               
            Respond ONLY in valid, well-formatted JSON that exactly matches the requested structure.
        """.trimIndent()

        val systemInstruction = """
            You are an elite Hollywood Video Director, AI Video Architect, and Prompt Master.
            You must output a single, valid JSON object matching this schema:
            {
              "masterPrompt": "detailed paragraph for the chosen text-to-video generator",
              "concept": "1-sentence visual hook/concept",
              "musicPrompt": "highly descriptive prompt for audio generators with [Drop], [Intro] etc tags",
              "tags": ["tag1", "tag2", "tag3"],
              "scenes": [
                {
                  "sceneNumber": 1,
                  "timeStart": 0,
                  "timeEnd": 5,
                  "visualPrompt": "detailed shot visual description with Hollywood terms",
                  "cameraMovement": "camera movement details",
                  "voiceoverText": "voiceover words or sound effects details",
                  "moodColorHex": "#FF007F"
                }
              ],
              "colorPalette": ["#HEX1", "#HEX2", "#HEX3", "#HEX4"]
            }
            Do NOT include any markdown code blocks, backticks (```json), or extra text outside of the JSON object.
            Ensure the scenes chronologically span from 0 to $duration seconds in EXACT 5-second increments (0-5, 5-10, 10-15, etc.) without any gaps or overlaps.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = prompt)
                    )
                )
            ),
            generationConfig = GeminiConfig(
                responseMimeType = "application/json",
                temperature = 0.75
            ),
            systemInstruction = GeminiContent(
                parts = listOf(
                    GeminiPart(text = systemInstruction)
                )
            )
        )

        try {
            // Using gemini-3.5-flash which is the default for general/structured tasks
            val response = RetrofitClient.geminiService.generateContent(
                model = "gemini-3.5-flash",
                apiKey = apiKey,
                request = request
            )

            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return@withContext Result.failure(Exception("Empty response from AI model"))

            // Clean the raw text from any potential markdown wrapping if returned
            val cleanJson = rawText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val adapter = RetrofitClient.moshiInstance.adapter(VideoPlanResponse::class.java)
            val parsedResponse = adapter.fromJson(cleanJson)
                ?: return@withContext Result.failure(Exception("Failed to parse JSON response from AI. Response was:\n$cleanJson"))

            Result.success(parsedResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
