package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.scale
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.HighlightedPassage
import com.example.model.RetentionSegment
import com.example.model.ScriptAnalysisReport
import com.example.ui.theme.*
import com.example.viewmodel.ScriptScoreViewModel
import com.example.viewmodel.UiState
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScriptScoreApp(
    viewModel: ScriptScoreViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scriptText by viewModel.scriptText.collectAsState()
    val context = LocalContext.current
    var showAboutDialog by remember { mutableStateOf(false) }
    
    // File upload contract integration
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val stringBuilder = StringBuilder()
                    var line: String? = reader.readLine()
                    while (line != null) {
                        stringBuilder.append(line).append("\n")
                        line = reader.readLine()
                    }
                    inputStream.close()
                    
                    val contentText = stringBuilder.toString()
                    if (contentText.isNotBlank()) {
                        viewModel.setScript(contentText)
                        Toast.makeText(context, "Successfully loaded script content!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Selected file is empty.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to parse file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = AccentIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "About ScriptScore AI",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    text = "ScriptScore AI is a local-first YouTube script evaluation analytics deck.\n\nOptimized for high retention metrics, hook clarity, storytelling progression, and pacing scores. Helps you refine content before recording.",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Got it", color = AccentIndigo, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkCard,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        containerColor = DarkBg,
        topBar = {
            TopAppBarSection(
                onBackClicked = {
                    when (uiState) {
                        is UiState.Input -> viewModel.navigateToLanding()
                        is UiState.Dashboard -> viewModel.navigateToInput()
                        else -> {}
                    }
                },
                showBackButton = uiState is UiState.Input || uiState is UiState.Dashboard
            )
        },
        bottomBar = {
            if (uiState !is UiState.Analyzing) {
                BottomMenuBarSection(
                    activeTab = when (uiState) {
                        is UiState.Landing -> 0
                        is UiState.Input -> 1
                        is UiState.Dashboard -> 2
                        else -> 0
                    },
                    onTabSelected = { index ->
                        when (index) {
                            0 -> viewModel.navigateToLanding()
                            1 -> viewModel.navigateToInput()
                            2 -> {
                                viewModel.navigateToLatestDashboardOrInput()
                            }
                            3 -> {
                                showAboutDialog = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF131131), DarkBg),
                        center = Offset(200f, -200f),
                        radius = 1200f
                    )
                )
        ) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) togetherWith
                    fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow))
                },
                label = "Navigation"
            ) { state ->
                when (state) {
                    is UiState.Landing -> {
                        LandingScreen(
                            onStartClicked = { viewModel.navigateToInput() }
                        )
                    }
                    is UiState.Input -> {
                        InputScreen(
                            scriptText = scriptText,
                            onScriptChange = { viewModel.setScript(it) },
                            onFilePickRequested = { filePickerLauncher.launch("*/*") },
                            onAnalyzeRequested = { viewModel.startAnalysis() },
                            templates = viewModel.templates,
                            onLoadTemplate = { templateName, templateText ->
                                viewModel.setScript(templateText)
                                // Auto-analyze the selected template immediately for quick-starts!
                                viewModel.startAnalysis(title = templateName)
                            }
                        )
                    }
                    is UiState.Analyzing -> {
                        AnalyzingScreen(statusMessage = state.message)
                    }
                    is UiState.Dashboard -> {
                        DashboardScreen(
                            report = state.report,
                            onRunAnotherClicked = { viewModel.navigateToInput() }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarSection(
    onBackClicked: () -> Unit,
    showBackButton: Boolean
) {
    Column {
        CenterAlignedTopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Small gradient icon container resembling custom logo in HTML
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(AccentIndigo, Color(0xFF4F46E5))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .border(1.5.dp, Color.White, RoundedCornerShape(2.1.dp))
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = buildAnnotatedString {
                            append("ScriptScore ")
                            withStyle(SpanStyle(color = AccentIndigo, fontWeight = FontWeight.Bold)) {
                                append("AI")
                            }
                        },
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = (-0.4).sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AccentPink.copy(alpha = 0.15f))
                            .border(1.0.dp, AccentPink.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "MVP",
                            color = AccentPink,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            },
            navigationIcon = {
                if (showBackButton) {
                    IconButton(
                        onClick = onBackClicked,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(DarkCard)
                            .border(1.dp, DarkBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back",
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            actions = {
                // Profile container on right
                Box(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(DarkCard)
                        .border(1.dp, DarkBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(TextPrimary.copy(alpha = 0.25f))
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = DarkBg,
                titleContentColor = TextPrimary
            )
        )
        HorizontalDivider(color = DarkBorder, thickness = 1.dp)
    }
}

@Composable
fun LandingScreen(
    onStartClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Large Badge with soft pulsing indicator
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(AccentIndigo.copy(alpha = 0.15f))
                .border(1.dp, AccentIndigo.copy(alpha = 0.3f), RoundedCornerShape(50))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(ColorGood)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Optimize For YouTube Retention",
                    color = AccentIndigo,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Hero Heading
        Text(
            text = "Analyze Your Script Before You Hit Record",
            color = TextPrimary,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.SansSerif,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.95f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Supporting Subtext
        Text(
            text = "Paste or upload transcript files to immediately identify pacing slumps, hook drops, and delivery risks in structured graphs with AI checklists.",
            color = TextSecondary,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.9f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Action CTA
        Button(
            onClick = onStartClicked,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(56.dp)
                .testTag("analyze_my_script_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(AccentIndigo, AccentPink)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Analyze My Script",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Interactive Dashboard Preview Container
        Text(
            text = "DASHBOARD INTERACTIVE PREVIEW",
            color = AccentIndigo,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Micro top section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Report: MrBeast Challenge Pitch",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ColorGood.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Score: 92/100",
                            color = ColorGood,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bar simulated preview chart
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val barHeights = listOf(0.9f, 0.4f, 0.85f, 0.7f, 0.95f, 0.6f, 0.3f, 0.8f, 0.88f)
                    barHeights.forEachIndexed { i, heightFactor ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(heightFactor)
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 0.dp, bottomEnd = 0.dp))
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            if (heightFactor < 0.5f) ColorDanger else if (heightFactor < 0.75f) ColorWarning else ColorGood,
                                            AccentIndigo.copy(alpha = 0.3f)
                                        )
                                    )
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0:00 (Hook)", color = TextSecondary, fontSize = 10.sp)
                    Text("2:30 (Retentive Climax)", color = TextSecondary, fontSize = 10.sp)
                    Text("5:00 (CTA)", color = TextSecondary, fontSize = 10.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun InputScreen(
    scriptText: String,
    onScriptChange: (String) -> Unit,
    onFilePickRequested: () -> Unit,
    onAnalyzeRequested: () -> Unit,
    templates: List<Pair<String, String>>,
    onLoadTemplate: (String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Draft Your Narrative Analytics",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Copy-paste, upload document files, or choose a pre-loaded template below to run high-tempo analytical reviews.",
            color = TextSecondary,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Templates quick list
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = ColorWarning,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Quick Start Suggestions:",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                templates.forEach { (name, content) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBg)
                            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                            .clickable { onLoadTemplate(name, content) }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name,
                                color = AccentIndigo,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Run",
                                tint = AccentPink,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Script content slot title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "YouTube Script Input *",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            
            // Upload button launcher
            TextButton(
                onClick = onFilePickRequested,
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.UploadFile,
                    contentDescription = null,
                    tint = AccentPink,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Upload File",
                    color = AccentPink,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Large Multiline Text Editor box
        OutlinedTextField(
            value = scriptText,
            onValueChange = onScriptChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .testTag("script_input_field"),
            placeholder = {
                Text(
                    text = "Pasted your YouTube video script blocks here...\nExample: \"In this video I'll reveal why saving money is completely useless...\"",
                    color = TextSecondary.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            },
            textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontSize = 14.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkCard,
                unfocusedContainerColor = DarkCard,
                focusedBorderColor = AccentIndigo,
                unfocusedBorderColor = DarkBorder,
                cursorColor = AccentIndigo
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Primary Analyze Script button
        Button(
            onClick = onAnalyzeRequested,
            enabled = scriptText.trim().isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("analyze_script_btn"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentIndigo,
                disabledContainerColor = DarkBorder
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Analyze Script Structure",
                    color = if (scriptText.trim().isNotEmpty()) Color.White else TextSecondary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun AnalyzingScreen(
    statusMessage: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AnalysisPulse")
    
    // Smooth angle rotation
    val angleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SpinnerAngle"
    )

    // Pulse size transitions
    val pulsingScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCirc),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulsingScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .drawBehind {
                    drawArc(
                        brush = Brush.sweepGradient(listOf(AccentIndigo, AccentPink, AccentIndigo)),
                        startAngle = angleRotation,
                        sweepAngle = 280f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 12f,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = AccentPink,
                modifier = Modifier
                    .size(40.dp)
                    .scale(pulsingScale)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Big Heading
        Text(
            text = "AI Structural Analysis Underway",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.SansSerif,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Rotating diagnostic messages
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(DarkCard)
                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "LOG > $statusMessage",
                color = AccentIndigo,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DashboardScreen(
    report: ScriptAnalysisReport,
    onRunAnotherClicked: () -> Unit
) {
    var activeTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Scores & Hook", "Retention Segments", "Style & Pacing", "AI Highlighter")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Report Title / Meta slot
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = report.scriptTitle,
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 26.sp
                )
                Text(
                    text = "Analysis Date: ${report.dateAnalyzed}",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Re-run Button
            IconButton(
                onClick = onRunAnotherClicked,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(DarkCard)
                    .border(1.dp, DarkBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Script",
                    tint = AccentPink
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // OVERALL SCORE LARGE DISPLAY CARD WITH AMBIENT GLOW BACKDROP
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        // Drawing professional radial ambient glow like the web design
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(AccentIndigo.copy(alpha = 0.12f), Color.Transparent),
                                center = Offset(size.width, 0f),
                                radius = size.width * 0.45f
                            ),
                            center = Offset(size.width, 0f),
                            radius = size.width * 0.45f
                        )
                    }
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular Canvas Arc Gauge
                    Box(
                        modifier = Modifier
                            .size(84.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Bottom Track ring
                            drawCircle(
                                color = DarkBorder,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6.dp.toPx())
                            )
                            // Progress sweep arc rotated to top
                            val sweepAngle = (report.overallScore.toFloat() / 100f) * 360f
                            drawArc(
                                color = AccentIndigo,
                                startAngle = -90f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 6.dp.toPx(),
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                            )
                        }
                        
                        // Calculate grade
                        val gradeText = when {
                            report.overallScore >= 92 -> "A+"
                            report.overallScore >= 86 -> "A"
                            report.overallScore >= 80 -> "A-"
                            report.overallScore >= 74 -> "B+"
                            else -> "B"
                        }
                        
                        Text(
                            text = gradeText,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Detail metadata
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "OVERALL SCRIPTSCORE",
                            color = TextSecondary.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(ColorGood)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Viral Potential: ${report.viralPotential}",
                                color = ColorGood,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = "This script has strong curiosity loops and is optimized against early drop-offs.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SCORE CARDS GRID (MOBILE: 2 COLUMN GRID WITH VISUAL PROGRESS ENTIRELY COMPLIANT WITH DESIGN GRID)
        Row(modifier = Modifier.fillMaxWidth()) {
            ScoreCardGridItem(
                title = "Hook Index",
                score = report.hookScore,
                badgeText = "Strong Hook",
                badgeColor = ColorGood,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            ScoreCardGridItem(
                title = "Retention",
                score = report.retentionScore,
                badgeText = "Medium Risk",
                badgeColor = ColorWarning,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            ScoreCardGridItem(
                title = "Clarity Rate",
                score = report.clarityScore,
                badgeText = "Excellent",
                badgeColor = AccentIndigo,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            ScoreCardGridItem(
                title = "Pacing Rate",
                score = report.storytelling.pacing,
                badgeText = "Optimized",
                badgeColor = AccentPink,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // POLISHED TAB NAVIGATION CARD WRAPPER
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = DarkCard,
            contentColor = AccentIndigo,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = AccentIndigo
                )
            },
            divider = { HorizontalDivider(color = DarkBorder) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
        ) {
            tabTitles.forEachIndexed { index, title ->
                val isSelected = activeTab == index
                Tab(
                    selected = isSelected,
                    onClick = { activeTab = index },
                    modifier = Modifier.background(
                        if (isSelected) AccentIndigo.copy(alpha = 0.05f) else Color.Transparent
                    ),
                    text = {
                        Text(
                            text = title.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) AccentIndigo else TextSecondary
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // TAB VIEW CONTROLLER
        when (activeTab) {
            0 -> TabHookAnalysis(report)
            1 -> TabRetentionSegments(report)
            2 -> TabStylePacing(report)
            3 -> TabScriptHighlighter(report)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // AI DYNAMIC ACTION SUGGESTIONS PANEL - EXACT DESIGN HTML COMPLIANT STYLING
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(AccentPink)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SMART RECOMMENDATIONS",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                report.suggestions.forEachIndexed { i, suggestion ->
                    val isOptimized = i % 2 == 0
                    val glyphColor = if (isOptimized) ColorWarning else ColorGood
                    val glyphText = if (isOptimized) "!" else "✓"
                    val glyphBg = glyphColor.copy(alpha = 0.12f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkBg)
                            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(glyphBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = glyphText,
                                    color = glyphColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Text(
                                text = suggestion,
                                color = TextPrimary.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun ScoreCardGridItem(
    title: String,
    score: Int,
    badgeText: String,
    badgeColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title.uppercase(),
                color = TextSecondary.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$score%",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = badgeText,
                    color = badgeColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { score.toFloat() / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp)),
                color = badgeColor,
                trackColor = DarkBorder
            )
        }
    }
}

@Composable
fun TabHookAnalysis(report: ScriptAnalysisReport) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Section 1: Hook Diagnostics",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "The first 15 seconds decide if viewer skips. Optimize these components.",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Triple horizontal progress bars
        HookProgressItem("Hook Strength Index", report.hookStrength, AccentIndigo)
        HookProgressItem("Curiosity Generation", report.hookCuriosity, AccentPink)
        HookProgressItem("Emotional Tension Trigger", report.hookEmotionalTrigger, ColorWarning)
    }
}

@Composable
fun HookProgressItem(label: String, score: Int, color: Color) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = TextSecondary, fontSize = 12.sp)
            Text("$score / 100", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { score.toFloat() / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = DarkBorder
        )
    }
}

@Composable
fun TabRetentionSegments(report: ScriptAnalysisReport) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Section 2: Narrative Retention Breakdown",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Analysis of audience retention risk across standard segment intervals.",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        report.retentionSegments.forEach { segment ->
            val color = when (segment.riskLevel) {
                "Low Risk" -> ColorGood
                "Medium Risk" -> ColorWarning
                else -> ColorDanger
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkCard)
                    .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(segment.sectionName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(segment.feedback, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                    
                    // Risk level indicator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = segment.riskLevel,
                            color = color,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabStylePacing(report: ScriptAnalysisReport) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Style & Readability Analysis",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(10.dp))

        // Readability 2x2 grid values
        Row(modifier = Modifier.fillMaxWidth()) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Reading Grade", color = TextSecondary, fontSize = 10.sp)
                    Text(report.readability.readingLevel, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Optimal is 7-8th grade", color = TextSecondary, fontSize = 9.sp)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Avg Sentence Length", color = TextSecondary, fontSize = 10.sp)
                    Text("${report.readability.sentenceLength} words", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Optimal is <15 words", color = TextSecondary, fontSize = 9.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Passive Voice %", color = TextSecondary, fontSize = 10.sp)
                    Text("${report.readability.passiveVoicePercentage}%", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Optimal is <10%", color = TextSecondary, fontSize = 9.sp)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Complexity Score", color = TextSecondary, fontSize = 10.sp)
                    Text(report.readability.complexityScore, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Optimal is Simple", color = TextSecondary, fontSize = 9.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Engagement & Energy Drivers",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Action elements that drive human comments, retention, and interaction.",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        HookProgressItem("Curiosity Drivers", report.engagement.curiosity, AccentIndigo)
        HookProgressItem("Emotional Resonance", report.engagement.emotionalImpact, AccentPink)
        HookProgressItem("Expert Authority", report.engagement.authority, ColorGood)
        HookProgressItem("Entertainment Value", report.engagement.entertainmentValue, ColorWarning)
    }
}

@Composable
fun TabScriptHighlighter(report: ScriptAnalysisReport) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Interactive Script Heatmap Highlighter",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Heatmap tracking retention risks. Hover or scan highlighted paragraphs.",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkCard)
                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            val annotatedText = buildAnnotatedString {
                report.highlights.forEach { passage ->
                    val colorConfig = when (passage.segmentType) {
                        "strong" -> Pair(ColorGood, Color(0xFF0D3220))   // Green backdrop
                        "average" -> Pair(ColorWarning, Color(0xFF2C240E)) // Yellow backdrop
                        else -> Pair(ColorDanger, Color(0xFF330F11))    // Red backdrop
                    }
                    
                    withStyle(
                        style = SpanStyle(
                            background = colorConfig.second,
                            color = colorConfig.first,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(" ${passage.text.trim()} ")
                    }
                }
            }
            
            Text(
                text = annotatedText,
                lineHeight = 24.sp,
                fontSize = 14.sp,
                fontFamily = FontFamily.SansSerif
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Legend explanation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            LegendItem("Strong Hook", ColorGood)
            LegendItem("Average Flow", ColorWarning)
            LegendItem("Drop-off Risk", ColorDanger)
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BottomMenuBarSection(
    activeTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Column {
        HorizontalDivider(color = DarkBorder, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(DarkBg)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val items = listOf(
                Triple("Home", Icons.Default.Home, 0),
                Triple("Scan", Icons.Default.Analytics, 1),
                Triple("Dashboard", Icons.Default.Dashboard, 2),
                Triple("About", Icons.Default.Info, 3)
            )
            
            items.forEach { (label, icon, index) ->
                val isSelected = activeTab == index
                val itemColor = if (isSelected) AccentIndigo else TextSecondary.copy(alpha = 0.5f)
                
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onTabSelected(index) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = itemColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = label,
                        color = itemColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
