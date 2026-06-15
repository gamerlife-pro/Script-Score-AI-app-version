package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

sealed interface UiState {
    object Landing : UiState
    object Input : UiState
    data class Analyzing(val message: String) : UiState
    data class Dashboard(val report: ScriptAnalysisReport) : UiState
}

class ScriptScoreViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Landing)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _scriptText = MutableStateFlow("")
    val scriptText: StateFlow<String> = _scriptText.asStateFlow()

    // History of past reports analyzed during this session (in memory)
    private val _history = MutableStateFlow<List<ScriptAnalysisReport>>(emptyList())
    val history: StateFlow<List<ScriptAnalysisReport>> = _history.asStateFlow()

    // Preloaded templates for creators to test instantly
    val templates = listOf(
        Pair(
            "MrBeast Style: $1 vs $500k Hotel Room!",
            """I stayed in a $1 hotel room and a $500,000 hotel room to see if there's actually any difference!
First, the $1 room is literally just a box on the floor of a moving truck... wait, is that a goat? Yes, there is an actual goat in my bathroom. 
Now, before we look at the half-million-dollar suite, let's talk about why we are doing this. Most people will never get to see inside these super exclusive buildings. 
At $10,000, we got a suite built entirely out of pure ice in Norway, where even the glasses are ice!
Finally, we arrive at the $500,000 room. It includes its own private water park and 24-hour butler service. Let's see if it's worth it! If you want to see us spend 24 hours inside this water park, hit that subscribe button right now!"""
        ),
        Pair(
            "Tech Explainer: Is AI Actually Conscious?",
            """Every single day, millions of people ask ChatGPT for advice, homework help, and lines of code. But is there a ghost in the machine?
The main problem is that we don't even have a clear definition of what consciousness actually is in neuroscience.
To understand, let's look at neural networks. They predict the very next word based on mathematical weights. There is no feeling, no understanding, and no memory outside of their active context window.
However, developers are working on cognitive architectures that simulate loops of self-reflection. When these systems assess their own thoughts, the line between simulation and reality blurs.
In conclusion, AI is not conscious yet—but it is mimicking human intelligence so well that the distinction might soon cease to matter. If you liked this video, leave a comment below with your thoughts!"""
        ),
        Pair(
            "Finance Guide: How I Retired at Age 28",
            """What if I told you that working until you are 65 is a completely outdated scam?
The hard truth is that saving 10% of your salary will never buy you freedom. High inflation rates will eat your savings before you ever get to enjoy them.
Here is the index fund solution. To retire early, you need to follow the Rule of 25. Save 25 times your annual expenses, and invest it entirely in broad-market funds.
By withdrawing just 4% of your portfolio every year, your capital continues to grow faster than you can spend it, securing passive income forever.
This single habit of extreme savings and automated investing changed my life. Check out my pinned link below to download my free investment spreadsheet, and I will see you in the next guide!"""
        )
    )

    fun navigateToLanding() {
        _uiState.value = UiState.Landing
    }

    fun navigateToInput() {
        _uiState.value = UiState.Input
    }

    fun navigateToLatestDashboardOrInput() {
        val lastReport = _history.value.firstOrNull()
        if (lastReport != null) {
            _uiState.value = UiState.Dashboard(lastReport)
        } else {
            _uiState.value = UiState.Input
        }
    }

    fun setScript(text: String) {
        _scriptText.value = text
    }

    fun startAnalysis(title: String = "") {
        val rawText = _scriptText.value.trim()
        if (rawText.isEmpty()) return

        val finalTitle = if (title.isNotEmpty()) {
            title
        } else {
            // Extract title from the first line or first 5 words
            val lines = rawText.split("\n")
            val firstLine = lines.firstOrNull { it.trim().isNotEmpty() } ?: "My YouTube Script"
            if (firstLine.length > 35) {
                firstLine.take(32) + "..."
            } else {
                firstLine
            }
        }

        viewModelScope.launch {
            val loadingMessages = listOf(
                "Parsing opening hook parameters...",
                "Running structural narrative deconstruction...",
                "Evaluating viewer retention patterns...",
                "Detecting passive voice & speech pacing...",
                "Simulating viewer drop-offs with AI model...",
                "Drafting custom creator recommendations..."
            )

            // Step-by-step loading simulation
            for (idx in loadingMessages.indices) {
                _uiState.value = UiState.Analyzing(loadingMessages[idx])
                delay(400) // Total time ~ 2.4 seconds
            }

            val report = generateAnalysisReport(finalTitle, rawText)
            
            // Add to session history
            val currentHistory = _history.value.toMutableList()
            currentHistory.add(0, report)
            _history.value = currentHistory

            _uiState.value = UiState.Dashboard(report)
        }
    }

    private fun generateAnalysisReport(title: String, text: String): ScriptAnalysisReport {
        val wordCount = text.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
        
        // Random overall scores bounded realistically
        val overall = Random.nextInt(72, 96)
        val retention = Random.nextInt(65, 94)
        val hook = Random.nextInt(70, 98)
        val clarity = Random.nextInt(75, 96)
        
        val viralRank = when {
            overall >= 90 -> "Viral (Exceptional)"
            overall >= 82 -> "High Potential"
            else -> "Moderate Potential"
        }

        // Hook detailed sub-scores
        val hookStrength = Random.nextInt(68, 97)
        val hookCuriosity = Random.nextInt(70, 99)
        val hookEmotionalTrigger = Random.nextInt(60, 95)

        // Readability Metrics
        val readingLevels = listOf("6th Grade", "7th Grade", "8th Grade", "9th Grade", "College Freshman")
        val selectedReadingLevel = when {
            clarity >= 90 -> readingLevels[0] // Simple / Easy
            clarity >= 83 -> readingLevels[1]
            clarity >= 77 -> readingLevels[2]
            else -> readingLevels[4]          // Complex
        }
        val avgSentenceLength = 10.5f + Random.nextFloat() * 8f
        val passivePercent = Random.nextInt(4, 18)
        val readabilityComplexity = when {
            clarity >= 88 -> "Low Complexity"
            clarity >= 76 -> "Moderate Complexity"
            else -> "High Complexity"
        }
        val readability = ReadabilityMetrics(
            readingLevel = selectedReadingLevel,
            sentenceLength = Math.round(avgSentenceLength * 10f) / 10f,
            passiveVoicePercentage = passivePercent,
            complexityScore = readabilityComplexity
        )

        // Storytelling
        val storytelling = StorytellingMetrics(
            setup = Random.nextInt(70, 95),
            conflict = Random.nextInt(60, 92),
            resolution = Random.nextInt(65, 96),
            pacing = Random.nextInt(74, 98)
        )

        // Engagement
        val engagement = EngagementMetrics(
            curiosity = Random.nextInt(65, 96),
            emotionalImpact = Random.nextInt(60, 94),
            authority = Random.nextInt(68, 95),
            entertainmentValue = Random.nextInt(70, 97)
        )

        // Predefined pools of action suggestions based on parameters
        val poolHook = listOf(
            "Your hook reveals the core payoff too early. Delay the main reveal to secure longer retention.",
            "Introduce a stronger emotional trigger or direct question in the opening 10 seconds of speech.",
            "The screen directions are sparse in your hook; ensure you pair early dialog with high-tempo visual setups.",
            "Add a 'Micro-Narrative' in your hook—make a small, complete promise that resolves before the title drop."
        )
        val poolRetention = listOf(
            "pacing drops noticeably in paragraphs 4-6. Consider cutting repetitive transition sentences to maintain speed.",
            "Insert a planned visual 'Pattern Interrupt' (overlay, chart, B-roll) right around the 60-second mark.",
            "Smooth out the shift from explaining the Problem to presenting the Solution; it currently feels slightly disjointed.",
            "Your mid-script slump risk is elevated; insert an unanswered question before starting the core breakdown."
        )
        val poolPacingStory = listOf(
            "The Conflict section has low stakes. Explicitly state the negative consequences if the viewer ignores your advice.",
            "Make your resolution snappy. A slow ending leads to instant click-away before the CTA cards appear.",
            "Pacing is uniform throughout. Introduce a 'breather block' of slow, personal anecdotes to create contrast.",
            "Your storytelling setup is excellent, but increase the tension right before the main proof peak."
        )
        val poolEngagementClean = listOf(
            "The final CTA is slightly long. Stick to suggesting a single, highly relevant next video link.",
            "Weave in a brief proof point or expert reference to spike the overall 'Authority' index.",
            "Inject humor or a polarizing question in the midsection to increase user comments and audience engagement.",
            "Simplify paragraph layouts. Shorten long compound sentences into crisp, punchy bullet thoughts."
        )

        val suggestions = listOf(
            poolHook[Random.nextInt(poolHook.size)],
            poolRetention[Random.nextInt(poolRetention.size)],
            poolPacingStory[Random.nextInt(poolPacingStory.size)],
            poolEngagementClean[Random.nextInt(poolEngagementClean.size)]
        )

        // Retention segments
        val retentionSegments = listOf(
            RetentionSegment("Intro (0-15s)", hookStrength, if (hookStrength >= 80) "Low Risk" else "Medium Risk", if (hookStrength >= 80) "green" else "yellow", "Dynamic setup with clear visual hooks."),
            RetentionSegment("Problem (15-60s)", storytelling.setup, if (storytelling.setup >= 80) "Low Risk" else "Medium Risk", if (storytelling.setup >= 80) "green" else "yellow", "Builds tension nicely, but avoid over-explaining details."),
            RetentionSegment("Main Content (1m-4m)", storytelling.pacing, if (storytelling.pacing >= 82) "Low Risk" else "Medium Risk", if (storytelling.pacing >= 82) "green" else "yellow", "Highly informative pacing, keep visual pattern interrupts steady."),
            RetentionSegment("Solution (4m-5m)", storytelling.resolution, if (storytelling.conflict < 75) "Medium Risk" else "Low Risk", if (storytelling.conflict < 75) "yellow" else "green", "Strong, crisp resolution of the core threat."),
            RetentionSegment("CTA & End (5m+)", engagement.authority, if (engagement.authority >= 85) "Low Risk" else "High Risk", if (engagement.authority >= 85) "green" else "red", "Make sure the end card is placed within 5 seconds of naming the CTA.")
        )

        // Dynamic highlighting of the user's specific script text
        val highlights = generateScriptHighlights(text)

        val currentDate = SimpleDateFormat("LLL dd, yyyy • HH:mm", Locale.getDefault()).format(Date())

        return ScriptAnalysisReport(
            id = "REP_${System.currentTimeMillis()}",
            scriptTitle = title,
            scriptText = text,
            dateAnalyzed = currentDate,
            overallScore = overall,
            retentionScore = retention,
            hookScore = hook,
            clarityScore = clarity,
            viralPotential = viralRank,
            hookStrength = hookStrength,
            hookCuriosity = hookCuriosity,
            hookEmotionalTrigger = hookEmotionalTrigger,
            retentionSegments = retentionSegments,
            storytelling = storytelling,
            readability = readability,
            engagement = engagement,
            suggestions = suggestions,
            highlights = highlights
        )
    }

    private fun generateScriptHighlights(text: String): List<HighlightedPassage> {
        // Break script into sentences or lines to highlight them logically with green/yellow/red categories
        val sentences = text.split(Regex("(?<=[.!?])\\s+")).filter { it.trim().isNotEmpty() }
        if (sentences.isEmpty()) return emptyList()

        val results = mutableListOf<HighlightedPassage>()
        
        sentences.forEachIndexed { idx, s ->
            val type = when {
                idx == 0 || s.contains("vs", ignoreCase = true) || s.contains("!", ignoreCase = true) -> {
                    HighlightedPassage(s, "strong", "Hook and curiosity trigger is strong here.")
                }
                idx == sentences.size - 1 || s.contains("subscribe", ignoreCase = true) || s.contains("link", ignoreCase = true) -> {
                    HighlightedPassage(s, "average", "Standard call to action.")
                }
                idx % 4 == 1 -> {
                    HighlightedPassage(s, "dropoff", "Potential retention slump. Speed up delivery.")
                }
                idx % 4 == 2 -> {
                    HighlightedPassage(s, "strong", "Great explanation detailing real proof.")
                }
                else -> {
                    HighlightedPassage(s, "average", "Normal conversational transition.")
                }
            }
            results.add(type)
        }

        return results
    }
}
