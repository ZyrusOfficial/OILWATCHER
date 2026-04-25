package com.oilwatcher.monitor.data.ml


import android.net.Uri

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognizer
import com.oilwatcher.monitor.domain.model.FuelType
import com.oilwatcher.monitor.domain.model.PriceCandidate
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ML Kit OCR wrapper for gas price extraction.
 *
 * Pipeline:
 * 1. Accept captured image (URI or Bitmap)
 * 2. Run ML Kit Text Recognition (on-device, ~100ms)
 * 3. Filter text blocks for price patterns ($X.XX regex)
 * 4. Map prices to fuel grades using vertical position
 * 5. Return PriceCandidate list with confidence scores
 */
@Singleton
class PriceExtractor @Inject constructor(
    private val textRecognizer: TextRecognizer,
) {
    // Matches prices like: 3.45, $3.45, 3.459 (9/10ths format)
    private val priceRegex = Regex("""\$?\d{1,2}\.\d{2,3}""")

    // Label keywords to identify fuel grades from surrounding text
    private val gradeKeywords = mapOf(
        FuelType.REGULAR to listOf("regular", "reg", "unleaded", "87"),
        FuelType.MIDGRADE to listOf("mid", "plus", "midgrade", "89"),
        FuelType.PREMIUM to listOf("premium", "prem", "super", "91", "93"),
        FuelType.DIESEL to listOf("diesel", "dsl"),
    )

    /**
     * Extract prices from an image URI.
     * Returns a list of price candidates sorted by position (top to bottom).
     */
    suspend fun extractPrices(
        context: android.content.Context,
        imageUri: Uri,
    ): List<PriceCandidate> {
        val inputImage = InputImage.fromFilePath(context, imageUri)
        val text = textRecognizer.process(inputImage).await()
        return parsePrices(text)
    }

    /**
     * Extract prices from an ML Kit Text result.
     */
    fun parsePrices(text: Text): List<PriceCandidate> {
        val candidates = mutableListOf<PriceCandidate>()

        for (block in text.textBlocks) {
            for (line in block.lines) {
                val lineText = line.text
                val match = priceRegex.find(lineText)

                if (match != null) {
                    val rawPrice = match.value
                        .removePrefix("$")
                        .let {
                            // Handle 9/10ths format (e.g., 3.459 → 3.45)
                            if (it.length > 4 && it.contains(".")) {
                                it.substring(0, it.indexOf(".") + 3)
                            } else it
                        }

                    val price = rawPrice.toDoubleOrNull() ?: continue

                    // Validate price is in reasonable range ($0.50 – $20.00)
                    if (price < 0.50 || price > 20.00) continue

                    // Try to identify fuel grade from surrounding text
                    val grade = identifyGrade(block.text, lineText)

                    candidates.add(
                        PriceCandidate(
                            value = price,
                            topPosition = line.boundingBox?.top ?: 0,
                            confidence = line.confidence ?: 0f,
                            rawText = lineText,
                            fuelGrade = grade,
                        )
                    )
                }
            }
        }

        // Sort by vertical position (top to bottom on the price board)
        candidates.sortBy { it.topPosition }

        // If no grades were detected by keywords, assign by position
        if (candidates.none { it.fuelGrade != null }) {
            return assignGradesByPosition(candidates)
        }

        return candidates
    }

    /**
     * Try to identify fuel grade from text near the price.
     */
    private fun identifyGrade(blockText: String, lineText: String): FuelType? {
        val searchText = (blockText + " " + lineText).lowercase()
        for ((grade, keywords) in gradeKeywords) {
            if (keywords.any { searchText.contains(it) }) {
                return grade
            }
        }
        return null
    }

    /**
     * Assign fuel grades based on vertical position on the price board.
     * Standard order (top to bottom): Regular → Midgrade → Premium → Diesel
     */
    private fun assignGradesByPosition(candidates: List<PriceCandidate>): List<PriceCandidate> {
        val grades = listOf(FuelType.REGULAR, FuelType.MIDGRADE, FuelType.PREMIUM, FuelType.DIESEL)
        return candidates.mapIndexed { index, candidate ->
            candidate.copy(fuelGrade = grades.getOrNull(index))
        }
    }
}
