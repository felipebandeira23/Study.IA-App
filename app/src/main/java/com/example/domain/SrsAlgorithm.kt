package com.example.domain

import com.example.data.model.Flashcard
import java.util.concurrent.TimeUnit

object SrsAlgorithm {

    data class ReviewResult(
        val repetitions: Int,
        val easeFactor: Float,
        val intervalDays: Int,
        val nextReviewAt: Long
    )

    /**
     * SM-2 algorithm.
     * @param quality 0=blackout, 1=wrong, 2=wrong but familiar, 3=correct with difficulty, 4=correct, 5=perfect
     */
    fun calculate(
        quality: Int,
        repetitions: Int,
        easeFactor: Float,
        intervalDays: Int
    ): ReviewResult {
        val newRepetitions = if (quality >= 3) repetitions + 1 else 0
        val newEaseFactor = (easeFactor + 0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
            .toFloat().coerceAtLeast(1.3f)
        val newInterval = when {
            newRepetitions == 0 -> 1
            newRepetitions == 1 -> 1
            newRepetitions == 2 -> 6
            else -> (intervalDays * newEaseFactor).toInt().coerceAtLeast(1)
        }
        val nextReviewAt = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(newInterval.toLong())
        return ReviewResult(newRepetitions, newEaseFactor, newInterval, nextReviewAt)
    }

    fun isDue(nextReviewAt: Long): Boolean = System.currentTimeMillis() >= nextReviewAt

    fun getDueCards(cards: List<Flashcard>): List<Flashcard> =
        cards.filter { isDue(it.nextReviewAt) }.sortedBy { it.nextReviewAt }
}
