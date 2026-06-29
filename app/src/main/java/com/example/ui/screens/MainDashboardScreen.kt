package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.VideoPromptEntity
import com.example.data.model.ScenePlan
import com.example.data.model.VideoPlanResponse
import com.example.ui.viewmodel.ScreenState
import com.example.ui.viewmodel.UiState
import com.example.ui.viewmodel.VideoPromptViewModel
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    viewModel: VideoPromptViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Video Prompt Blueprint",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
            ) {
                NavigationBarItem(
                    selected = state.activeTab == 0,
                    onClick = { viewModel.setActiveTab(0) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Creator") },
                    label = { Text("Creator") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("tab_creator")
                )
                NavigationBarItem(
                    selected = state.activeTab == 1,
                    onClick = { viewModel.setActiveTab(1) },
                    icon = { Icon(Icons.Default.List, contentDescription = "Saved Library") },
                    label = { Text("Saved Library") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("tab_saved")
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (state.activeTab) {
                0 -> CreatorTab(state = state, viewModel = viewModel, focusManager = focusManager)
                1 -> SavedLibraryTab(state = state, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CreatorTab(
    state: ScreenState,
    viewModel: VideoPromptViewModel,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    // If we have an active blueprint to view (either live success, or chosen from history)
    val activeBlueprint = remember(state.apiState, state.selectedHistoryPlan) {
        when {
            state.selectedHistoryPlan != null -> {
                VideoPlanResponse(
                    masterPrompt = state.selectedHistoryPlan.masterPrompt,
                    concept = state.selectedHistoryPlan.concept,
                    musicPrompt = state.selectedHistoryPlan.musicPrompt,
                    tags = state.selectedHistoryPlan.tags,
                    scenes = state.selectedHistoryPlan.scenes,
                    colorPalette = state.selectedHistoryPlan.colorPalette,
                    targetModel = state.selectedHistoryPlan.targetModel,
                    lightingPreset = state.selectedHistoryPlan.lightingPreset,
                    cameraLens = state.selectedHistoryPlan.cameraLens,
                    motionPacing = state.selectedHistoryPlan.motionPacing,
                    characterConsistency = state.selectedHistoryPlan.characterConsistency,
                    colorGrading = state.selectedHistoryPlan.colorGrading,
                    cinematicTransitions = state.selectedHistoryPlan.cinematicTransitions,
                    specialEffects = state.selectedHistoryPlan.specialEffects,
                    cinematicFraming = state.selectedHistoryPlan.cinematicFraming,
                    environmentPhysics = state.selectedHistoryPlan.environmentPhysics,
                    multiCharacterInteraction = state.selectedHistoryPlan.multiCharacterInteraction
                )
            }
            state.apiState is UiState.Success -> {
                state.apiState.response
            }
            else -> null
        }
    }

    if (activeBlueprint != null) {
        BlueprintViewer(
            blueprint = activeBlueprint,
            onBack = { viewModel.clearActivePlan() }
        )
    } else {
        when (state.apiState) {
            is UiState.Loading -> {
                LoadingScreen()
            }
            else -> {
                CreatorForm(state = state, viewModel = viewModel, focusManager = focusManager)
            }
        }
    }
}

@Composable
fun CreatorForm(
    state: ScreenState,
    viewModel: VideoPromptViewModel,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
    ) {
        item {
            // Hero banner image loaded locally
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        ),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner),
                        contentDescription = "Futuristic video dashboard",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Visual semi-transparent dark overlay to make details pop
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = "AI Video Blueprint Builder",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Transform thoughts into high-converting master prompts & interactive scenes",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    text = "1. Enter Video Category or Concept description",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = state.category,
                    onValueChange = { viewModel.setCategory(it) },
                    placeholder = { Text("e.g. Cyberpunk ninja leaping across skyscrapers in digital rain...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_input"),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }

        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "2. Set Duration (15s - 45s)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${state.duration} Seconds",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                Slider(
                    value = state.duration.toFloat(),
                    onValueChange = { viewModel.setDuration(it.toInt()) },
                    valueRange = 15f..45f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .testTag("duration_slider")
                )
            }
        }

        item {
            Column {
                Text(
                    text = "3. Select Aspect Ratio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val ratios = listOf("9:16 (TikTok/Reels)", "16:9 (YouTube/Cinema)", "1:1 (Square)")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ratios.forEach { ratio ->
                        val isSelected = state.aspectRatio == ratio
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setAspectRatio(ratio) },
                            label = { Text(ratio, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    text = "3.1 Target AI Video Model",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val targetModels = listOf("Sora", "Runway Gen-3", "Kling AI")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    targetModels.forEach { model ->
                        val isSelected = state.targetModel == model
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setTargetModel(model) },
                            label = { Text(model, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    text = "4. Visual Style",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val styles = listOf(
                    "Cinematic Photorealistic",
                    "Anime / Manga",
                    "3D Pixar Animation",
                    "Futuristic Cyberpunk",
                    "Minimalist Lo-Fi Sketch"
                )
                FlowRow(
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    styles.forEach { style ->
                        val isSelected = state.style == style
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setStyle(style) },
                            label = { Text(style, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    text = "5. Mood & Atmosphere",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val moods = listOf("Dramatic & Epic", "Mystical & Cosmic", "Energetic & Fast", "Calm & Dreamy", "Retro Synthwave")
                FlowRow(
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    moods.forEach { mood ->
                        val isSelected = state.mood == mood
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setMood(mood) },
                            label = { Text(mood, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    text = "6. Camera Movement",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val cameraMovements = listOf("Dynamic Drone Shot", "Steadicam Follow", "Slow Atmospheric Zoom", "Cinematic Pan/Tilt")
                FlowRow(
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    cameraMovements.forEach { cam ->
                        val isSelected = state.cameraMovement == cam
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setCameraMovement(cam) },
                            label = { Text(cam, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    text = "6.1 Lighting & Aesthetic Preset",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val lightingPresets = listOf(
                    "Moody Cinematic Golden Hour",
                    "Neon Noir Cyber-Luminary",
                    "Dramatic High-Key Contrast",
                    "Low-Key Dark Suspense",
                    "Soft Dreamy Anamorphic"
                )
                FlowRow(
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    lightingPresets.forEach { preset ->
                        val isSelected = state.lightingPreset == preset
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setLightingPreset(preset) },
                            label = { Text(preset, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    text = "7. Camera Lens Type",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val lenses = listOf("Ultra-Wide 15mm", "Cinematic 35mm", "Portrait 85mm", "Extreme Macro", "Anamorphic 2x")
                FlowRow(
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    lenses.forEach { lens ->
                        val isSelected = state.cameraLens == lens
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setCameraLens(lens) },
                            label = { Text(lens, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    text = "8. Motion Pacing",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val pacings = listOf("Real-time Cinematic", "Slow Motion 120fps", "Hyperlapse", "Speed Ramp")
                FlowRow(
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    pacings.forEach { pace ->
                        val isSelected = state.motionPacing == pace
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setMotionPacing(pace) },
                            label = { Text(pace, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    text = "9. Character Consistency & Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = state.characterConsistency,
                    onValueChange = { viewModel.setCharacterConsistency(it) },
                    placeholder = { Text("e.g. Cyberpunk hero in leather jacket, robotic astronaut, etc.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("character_consistency_input"),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }

        item {
            Column {
                Text(
                    text = "10. Color Grading Style / LUT",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val gradings = listOf("Hollywood Teal & Orange", "Bleach Bypass", "Vintage Kodak Portra", "Monochromatic Noir", "Warm Sepia Sunset")
                FlowRow(
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    gradings.forEach { grading ->
                        val isSelected = state.colorGrading == grading
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setColorGrading(grading) },
                            label = { Text(grading, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    text = "11. Cinematic Transitions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val transitions = listOf("Seamless Whip Pan", "Glitch Distortion", "Light Leak Fade", "Match Cut", "Direct Cut")
                FlowRow(
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    transitions.forEach { transition ->
                        val isSelected = state.cinematicTransitions == transition
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setCinematicTransitions(transition) },
                            label = { Text(transition, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    text = "12. Special Effects & CGI Layering (VFX)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val vfxs = listOf("None", "Unreal Engine 5 Nanite", "Floating Embers & Dust", "Magical Particles", "Cyberpunk Holographic")
                FlowRow(
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    vfxs.forEach { vfx ->
                        val isSelected = state.specialEffects == vfx
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setSpecialEffects(vfx) },
                            label = { Text(vfx, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    text = "13. Cinematic Framing & Composition",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val framings = listOf("Rule of Thirds Depth", "Golden Ratio Depth", "Extreme Close-Up", "Low-Angle Hero Shot", "Wide Establishing Shot")
                FlowRow(
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    framings.forEach { framing ->
                        val isSelected = state.cinematicFraming == framing
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setCinematicFraming(framing) },
                            label = { Text(framing, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    text = "14. Environment Physics & Weather Dynamics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val physicsList = listOf("Volumetric Fog & Ray-Traced Light", "Rain Splashes & Wet Reflections", "Snow Dust & Wind Turbulence", "Cyberpunk Wet Puddles")
                FlowRow(
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    physicsList.forEach { physics ->
                        val isSelected = state.environmentPhysics == physics
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setEnvironmentPhysics(physics) },
                            label = { Text(physics, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    text = "15. Multi-Character Interaction & Emotion",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val interactions = listOf("None", "Intense Eye-Contact Showdown", "Dynamic High-Stakes Sword Clash", "Emotional Dialogue with Tears", "Dynamic Crowd Background")
                FlowRow(
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    interactions.forEach { interaction ->
                        val isSelected = state.multiCharacterInteraction == interaction
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setMultiCharacterInteraction(interaction) },
                            label = { Text(interaction, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        if (state.apiState is UiState.Error) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Blueprint Generation Failed",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = state.apiState.message,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.generateVideoPlan()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("generate_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Generate Video Blueprint",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    val quotes = listOf(
        "Directing cinematic visual elements...",
        "Structuring timeline segments chronologically...",
        "Composing production master prompts...",
        "Styling scene color codes...",
        "Writing speech scripts and SFX notes...",
        "Designing sound architecture drafts..."
    )

    var currentQuoteIndex by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(2500)
            currentQuoteIndex = (currentQuoteIndex + 1) % quotes.size
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(140.dp)
                .drawBehind {
                    drawCircle(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0x3300FFCC),
                                Color.Transparent
                            )
                        ),
                        radius = size.width * scale
                    )
                }
        ) {
            CircularProgressIndicator(
                strokeWidth = 6.dp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "AI Video Producer Is Creative",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = quotes[currentQuoteIndex],
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.height(40.dp)
        )
    }
}

@Composable
fun BlueprintViewer(
    blueprint: VideoPlanResponse,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Media simulator states
    var isPlaying by remember { mutableStateOf(false) }
    var currentMillis by remember { mutableStateOf(0L) }
    val durationSeconds = blueprint.scenes.lastOrNull()?.timeEnd ?: 30
    val durationMillis = durationSeconds * 1000L

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            val startTime = System.currentTimeMillis() - currentMillis
            while (isPlaying && currentMillis < durationMillis) {
                delay(30)
                currentMillis = (System.currentTimeMillis() - startTime).coerceAtMost(durationMillis)
                if (currentMillis >= durationMillis) {
                    isPlaying = false
                }
            }
        }
    }

    val currentSecond = currentMillis / 1000f
    val activeScene = remember(currentSecond) {
        blueprint.scenes.firstOrNull { currentSecond >= it.timeStart && currentSecond <= it.timeEnd }
            ?: blueprint.scenes.firstOrNull()
    }

    val animatedColor by animateColorAsState(
        targetValue = try {
            Color(android.graphics.Color.parseColor(activeScene?.moodColorHex ?: "#00FFCC"))
        } catch (e: Exception) {
            MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(600),
        label = "active_mood_color"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Video Blueprint Viewer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // Animated Simulated Player (Interactive Storyboard player)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .border(2.dp, animatedColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    // Moving visual effects inside player
                    AmbientVisualizer(
                        color = animatedColor,
                        isPlaying = isPlaying,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Text overlay for scene content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Player Top Overlay Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = animatedColor.copy(alpha = 0.25f)),
                                border = BorderStroke(1.dp, animatedColor),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "SCENE ${activeScene?.sceneNumber ?: 1}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Text(
                                text = String.format("00:%02d / 00:%02d", (currentMillis / 1000).toInt(), durationSeconds),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Active scene content
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "VISUAL:",
                                color = animatedColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = activeScene?.visualPrompt ?: "Loading visual parameters...",
                                color = Color.White,
                                fontSize = 12.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "VOICEOVER / SFX:",
                                color = Color.LightGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = activeScene?.voiceoverText ?: "Preparing speech elements...",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Timeline Seek and Play/Pause Actions
                        Column {
                            Slider(
                                value = currentMillis.toFloat(),
                                onValueChange = {
                                    currentMillis = it.toLong()
                                    if (currentMillis >= durationMillis) {
                                        currentMillis = durationMillis
                                    }
                                },
                                valueRange = 0f..durationMillis.toFloat(),
                                colors = SliderDefaults.colors(
                                    thumbColor = animatedColor,
                                    activeTrackColor = animatedColor,
                                    inactiveTrackColor = Color.DarkGray
                                ),
                                modifier = Modifier.height(18.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        currentMillis = 0L
                                        isPlaying = false
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Stop & Reset",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                FloatingActionButton(
                                    onClick = { isPlaying = !isPlaying },
                                    containerColor = animatedColor,
                                    contentColor = Color.Black,
                                    shape = CircleShape,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Text(
                                    text = activeScene?.cameraMovement ?: "Static Shot",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 120.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Hook/Concept card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CORE CONCEPT HOOK",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = blueprint.concept,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Unified Results Dashboard View
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                ),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("results_dashboard_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Dashboard Title Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🎬 CINEMATIC PRODUCTION DASHBOARD",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            letterSpacing = 1.2.sp
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // 1. Cinematic Production Specs
                    Column {
                        Text(
                            text = "PRODUCTION SPECIFICATIONS",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        val specs = listOf(
                            "AI Video Model" to (blueprint.targetModel ?: "Runway Gen-3"),
                            "Lighting & Aesthetic" to (blueprint.lightingPreset ?: "Moody Cinematic Golden Hour"),
                            "Camera Lens" to (blueprint.cameraLens ?: "Cinematic 35mm"),
                            "Motion Pacing" to (blueprint.motionPacing ?: "Real-time Cinematic"),
                            "Character Consistency" to (blueprint.characterConsistency ?: "None"),
                            "Color Grading Style" to (blueprint.colorGrading ?: "Hollywood Teal & Orange"),
                            "Cinematic Transitions" to (blueprint.cinematicTransitions ?: "Seamless Whip Pan"),
                            "VFX / CGI Layering" to (blueprint.specialEffects ?: "None"),
                            "Framing & Composition" to (blueprint.cinematicFraming ?: "Rule of Thirds Depth"),
                            "Environment Physics" to (blueprint.environmentPhysics ?: "Volumetric Fog & Ray-Traced Light"),
                            "Character Interaction" to (blueprint.multiCharacterInteraction ?: "None")
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            specs.forEach { (label, value) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.background.copy(alpha = 0.4f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = value,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.widthIn(max = 200.dp)
                                    )
                                }
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // 2. Visual Mood Color Palette (4 beautiful rounded/circular dots)
                    Column {
                        Text(
                            text = "VISUAL MOOD COLOR PALETTE",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        val finalPalette = remember(blueprint) {
                            if (!blueprint.colorPalette.isNullOrEmpty()) {
                                blueprint.colorPalette
                            } else {
                                blueprint.scenes.map { it.moodColorHex }.distinct().take(4)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            finalPalette.forEach { hex ->
                                val color = try {
                                    Color(android.graphics.Color.parseColor(hex))
                                } catch (e: Exception) {
                                    MaterialTheme.colorScheme.secondary
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable {
                                            copyToClipboard(context, "Color Hex", hex)
                                        }
                                        .padding(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(2.dp, Color.White, CircleShape)
                                            .border(4.dp, Color.Black.copy(alpha = 0.15f), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = hex.uppercase(),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // 3. Audio / Sound Music Prompt (Suno/Udio Optimized)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "AUDIO & SFX PROMPT",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // Musical Waveform visualizer
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val barHeights = listOf(8.dp, 16.dp, 12.dp, 20.dp, 10.dp, 14.dp)
                                    barHeights.forEach { height ->
                                        Box(
                                            modifier = Modifier
                                                .width(2.5.dp)
                                                .height(height)
                                                .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(1.dp))
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = {
                                    copyToClipboard(context, "Audio Prompt", blueprint.musicPrompt)
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Copy Audio Prompt",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.background,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = blueprint.musicPrompt,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                fontFamily = FontFamily.Serif,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // 4. Export Project Button (Download & Share Project blueprint)
                    Button(
                        onClick = {
                            exportBlueprintToFile(context, blueprint)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("export_project_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export icon",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "EXPORT CINEMATIC PROJECT",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // Master prompt copy container
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("master_prompt_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OPTIMIZED MASTER PROMPT",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )

                        Row {
                            IconButton(
                                onClick = {
                                    copyToClipboard(context, "Master Prompt", blueprint.masterPrompt)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Copy Prompt",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                            IconButton(
                                onClick = {
                                    shareText(context, "AI Master Prompt:\n${blueprint.masterPrompt}")
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.background,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = blueprint.masterPrompt,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }
        }

        // Tags item list
        item {
            Column {
                Text(
                    text = "INDEXING TAGS",
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                FlowRow(
                    mainAxisSpacing = 6.dp,
                    crossAxisSpacing = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    blueprint.tags.forEach { tag ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Timeline separator title
        item {
            Text(
                text = "PRODUCTION STORYBOARD TIMELINE",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Scenes iteration list
        items(blueprint.scenes) { scene ->
            val sceneColor = try {
                Color(android.graphics.Color.parseColor(scene.moodColorHex))
            } catch (e: Exception) {
                MaterialTheme.colorScheme.primary
            }

            val isActive = activeScene?.sceneNumber == scene.sceneNumber

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
                    else MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (isActive) 1.5.dp else 0.dp,
                        color = sceneColor,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // Left Timeline bar representation
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(42.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(sceneColor)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = String.format("%02d", scene.sceneNumber),
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(60.dp)
                                .background(sceneColor.copy(alpha = 0.4f))
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Details block
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Scene ${scene.sceneNumber}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Card(
                                colors = CardDefaults.cardColors(containerColor = sceneColor.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "0:${String.format("%02d", scene.timeStart)} - 0:${String.format("%02d", scene.timeEnd)}s",
                                    color = sceneColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "VISUAL PROMPT:",
                            color = sceneColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = scene.visualPrompt,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = sceneColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Camera: ${scene.cameraMovement}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            text = "VOICEOVER / SFX:",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        Text(
                            text = scene.voiceoverText,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AmbientVisualizer(
    color: Color,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient")
    
    // Wave animation parameters
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    val scaleFactor by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val cx = width / 2f
        val cy = height / 2f

        // Solid abstract background radial gradient
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.25f), Color.Transparent),
                center = Offset(cx, cy),
                radius = cx * 1.5f
            )
        )

        // Floating ambient rings representing timeline loop
        val ringCount = 3
        for (i in 1..ringCount) {
            val ringScale = scaleFactor * (i * 0.3f)
            val strokeWidth = if (isPlaying) 2.dp.toPx() else 1.dp.toPx()
            val ringRadius = (cx * 0.45f) * ringScale

            drawCircle(
                color = color.copy(alpha = (0.35f / i)),
                radius = ringRadius,
                center = Offset(cx, cy),
                style = Stroke(width = strokeWidth)
            )

            // Dynamic frequency line draw when active playing
            if (isPlaying) {
                val pointCount = 60
                val path = androidx.compose.ui.graphics.Path()
                for (p in 0 until pointCount) {
                    val angle = (2 * Math.PI * p / pointCount).toFloat()
                    val waveAmplitude = 12.dp.toPx() * sin((angle * 4 + waveOffset).toDouble()).toFloat()
                    val dynamicRadius = ringRadius + waveAmplitude
                    val px = cx + dynamicRadius * cos(angle.toDouble()).toFloat()
                    val py = cy + dynamicRadius * sin(angle.toDouble()).toFloat()

                    if (p == 0) {
                        path.moveTo(px, py)
                    } else {
                        path.lineTo(px, py)
                    }
                }
                path.close()
                drawPath(
                    path = path,
                    color = color.copy(alpha = (0.5f / i)),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }

        // Futuristic decorative visual frame border
        drawRect(
            color = color.copy(alpha = 0.2f),
            size = size,
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

@Composable
fun SavedLibraryTab(
    state: ScreenState,
    viewModel: VideoPromptViewModel
) {
    if (state.savedPrompts.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No saved blueprints yet",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Text(
                text = "Generate a video blueprint and it will be saved here automatically.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Text(
                    text = "Saved Production Blueprints (${state.savedPrompts.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            items(state.savedPrompts) { prompt ->
                SavedBlueprintCard(
                    prompt = prompt,
                    onSelect = { viewModel.selectHistoryPlan(prompt) },
                    onDelete = { viewModel.deletePrompt(prompt.id) }
                )
            }
        }
    }
}

@Composable
fun SavedBlueprintCard(
    prompt: VideoPromptEntity,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("saved_blueprint_card_${prompt.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${prompt.duration}s | ${prompt.aspectRatio.split(" ").firstOrNull() ?: ""}",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = prompt.category,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = prompt.concept,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )

            FlowRow(
                mainAxisSpacing = 6.dp,
                crossAxisSpacing = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                val modelText = if (prompt.targetModel.isNullOrBlank()) "Runway Gen-3" else prompt.targetModel
                val lightingText = if (prompt.lightingPreset.isNullOrBlank()) "Cinematic" else prompt.lightingPreset
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = modelText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = lightingText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = prompt.style,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = prompt.mood,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

// FlowRow layout helper for chip wrapping
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        val layoutWidth = constraints.maxWidth
        
        var rowWidth = 0
        var rowHeight = 0
        var currentY = 0
        
        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        
        val mainAxisSpacingPx = mainAxisSpacing.roundToPx()
        val crossAxisSpacingPx = crossAxisSpacing.roundToPx()

        placeables.forEach { placeable ->
            if (rowWidth + placeable.width + mainAxisSpacingPx > layoutWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                rowWidth = 0
            }
            currentRow.add(placeable)
            rowWidth += placeable.width + mainAxisSpacingPx
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

        val height = rows.sumOf { row -> row.maxOf { it.height } } + (rows.size - 1).coerceAtLeast(0) * crossAxisSpacingPx

        layout(layoutWidth, height) {
            var y = 0
            rows.forEach { row ->
                var x = 0
                val rowMaxHeight = row.maxOf { it.height }
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + mainAxisSpacingPx
                }
                y += rowMaxHeight + crossAxisSpacingPx
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
}

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share Video Blueprint"))
}

private fun exportBlueprintToFile(context: Context, blueprint: VideoPlanResponse) {
    val fileName = "Video_Blueprint_${System.currentTimeMillis()}.txt"
    val contentBuilder = StringBuilder()
    
    contentBuilder.append("=========================================\n")
    contentBuilder.append("       AI VIDEO PROMPT BLUEPRINT         \n")
    contentBuilder.append("       Brimbolt Production Engine        \n")
    contentBuilder.append("=========================================\n\n")
    
    contentBuilder.append("CONCEPT/HOOK:\n${blueprint.concept}\n\n")
    contentBuilder.append("-----------------------------------------\n")
    contentBuilder.append("CINEMATIC PRODUCTION SPECS:\n")
    contentBuilder.append("- Target AI Video Model: ${blueprint.targetModel ?: "Runway Gen-3"}\n")
    contentBuilder.append("- Lighting Preset: ${blueprint.lightingPreset ?: "Moody Cinematic Golden Hour"}\n")
    contentBuilder.append("- Camera Lens Type: ${blueprint.cameraLens ?: "Cinematic 35mm"}\n")
    contentBuilder.append("- Motion Pacing: ${blueprint.motionPacing ?: "Real-time Cinematic"}\n")
    contentBuilder.append("- Character Consistency: ${blueprint.characterConsistency ?: "None"}\n")
    contentBuilder.append("- Color Grading Style: ${blueprint.colorGrading ?: "Hollywood Teal & Orange"}\n")
    contentBuilder.append("- Cinematic Transitions: ${blueprint.cinematicTransitions ?: "Seamless Whip Pan"}\n")
    contentBuilder.append("- Special Effects (VFX): ${blueprint.specialEffects ?: "None"}\n")
    contentBuilder.append("- Cinematic Framing: ${blueprint.cinematicFraming ?: "Rule of Thirds Depth"}\n")
    contentBuilder.append("- Environment Physics: ${blueprint.environmentPhysics ?: "Volumetric Fog"}\n")
    contentBuilder.append("- Multi-Character Interaction: ${blueprint.multiCharacterInteraction ?: "None"}\n\n")
    
    contentBuilder.append("-----------------------------------------\n")
    contentBuilder.append("VISUAL MOOD COLOR PALETTE:\n")
    val palette = blueprint.colorPalette ?: blueprint.scenes.map { it.moodColorHex }.distinct().take(4)
    contentBuilder.append(palette.joinToString(", ").uppercase() + "\n\n")
    
    contentBuilder.append("-----------------------------------------\n")
    contentBuilder.append("MASTER PROMPT:\n${blueprint.masterPrompt}\n\n")
    
    contentBuilder.append("-----------------------------------------\n")
    contentBuilder.append("AUDIO & BGM PROMPT (Suno/Udio Optimized):\n${blueprint.musicPrompt}\n\n")
    
    contentBuilder.append("-----------------------------------------\n")
    contentBuilder.append("TAGS:\n${blueprint.tags.joinToString(", ")}\n\n")
    
    contentBuilder.append("=========================================\n")
    contentBuilder.append("               STORYBOARD                \n")
    contentBuilder.append("=========================================\n")
    blueprint.scenes.forEachIndexed { index, scene ->
        contentBuilder.append("Scene ${index + 1} (${scene.timeStart}-${scene.timeEnd}s):\n")
        contentBuilder.append("- Visual Prompt: ${scene.visualPrompt}\n")
        contentBuilder.append("- Camera Movement: ${scene.cameraMovement}\n")
        contentBuilder.append("- Voiceover / Audio FX: ${scene.voiceoverText}\n")
        contentBuilder.append("- Mood Color: ${scene.moodColorHex.uppercase()}\n\n")
    }
    
    val textToSave = contentBuilder.toString()
    
    // Attempt to write using MediaStore so it goes to Downloads
    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
    }
    
    var success = false
    try {
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(textToSave.toByteArray())
                success = true
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    
    if (success) {
        Toast.makeText(context, "Saved successfully to Downloads/$fileName!", Toast.LENGTH_LONG).show()
    } else {
        // Fallback to sharing the text directly
        Toast.makeText(context, "Saving file failed, opening Share Sheet instead...", Toast.LENGTH_SHORT).show()
    }
    
    // Also launch the share sheet so the user can easily copy/send/save it anywhere!
    shareText(context, textToSave)
}
