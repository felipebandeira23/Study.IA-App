package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_notes")
data class StudyNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val summary: String = "",
    val topic: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "decks")
data class Deck(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val topic: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deckId: Int,
    val front: String,
    val back: String,
    // SM-2 spaced repetition fields
    val repetitions: Int = 0,
    val easeFactor: Float = 2.5f,
    val intervalDays: Int = 1,
    val nextReviewAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "card_reviews")
data class CardReview(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val flashcardId: Int,
    val deckId: Int,
    val quality: Int,
    val reviewedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_plans")
data class StudyPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val topic: String,
    val durationDays: Int,
    val level: String, // "iniciante" | "intermediário" | "avançado"
    val planContent: String, // day-by-day description (string or JSON)
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tracked_contests")
data class TrackedContest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val organizer: String = "",
    val examDate: String = "",
    val editalText: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deckId: Int,
    val cardsReviewed: Int,
    val correctAnswers: Int,
    val date: Long = System.currentTimeMillis()
)
