package com.example.model

import androidx.compose.ui.graphics.Color

data class RetentionSegment(
    val sectionName: String,     // Intro, Problem, Main Content, Solution, CTA
    val score: Int,              // 0-100
    val riskLevel: String,        // "Low Risk", "Medium Risk", "High Risk"
    val riskColor: String,        // Hex or semantic indicator
    val feedback: String         // Brief detail
)

data class StorytellingMetrics(
    val setup: Int,
    val conflict: Int,
    val resolution: Int,
    val pacing: Int
)

data class ReadabilityMetrics(
    val readingLevel: String,     // e.g. "8th Grade"
    val sentenceLength: Float,    // avg words
    val passiveVoicePercentage: Int,
    val complexityScore: String   // "Simple", "Moderate", "Complex"
)

data class EngagementMetrics(
    val curiosity: Int,
    val emotionalImpact: Int,
    val authority: Int,
    val entertainmentValue: Int
)

data class HighlightedPassage(
    val text: String,
    val segmentType: String,      // "strong" (Green), "average" (Yellow), "dropoff" (Red)
    val annotation: String        // "Excellent curiosity trigger"
)

data class ScriptAnalysisReport(
    val id: String,
    val scriptTitle: String,
    val scriptText: String,
    val dateAnalyzed: String,
    val overallScore: Int,
    val retentionScore: Int,
    val hookScore: Int,
    val clarityScore: Int,
    val viralPotential: String,
    
    // Hook details
    val hookStrength: Int,
    val hookCuriosity: Int,
    val hookEmotionalTrigger: Int,
    
    // Detailed sections
    val retentionSegments: List<RetentionSegment>,
    val storytelling: StorytellingMetrics,
    val readability: ReadabilityMetrics,
    val engagement: EngagementMetrics,
    val suggestions: List<String>,
    val highlights: List<HighlightedPassage>
)
