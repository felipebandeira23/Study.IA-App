package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {

    // --- Study Notes ---
    @Query("SELECT * FROM study_notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<StudyNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: StudyNote)

    @Delete
    suspend fun deleteNote(note: StudyNote)

    // --- Decks ---
    @Query("SELECT * FROM decks ORDER BY createdAt DESC")
    fun getAllDecks(): Flow<List<Deck>>

    @Query("SELECT * FROM decks WHERE id = :id LIMIT 1")
    suspend fun getDeckById(id: Int): Deck?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: Deck): Long

    @Query("DELETE FROM decks WHERE id = :deckId")
    suspend fun deleteDeckById(deckId: Int)

    // --- Flashcards ---
    @Query("SELECT * FROM flashcards WHERE deckId = :deckId")
    fun getFlashcardsForDeck(deckId: Int): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId")
    suspend fun getFlashcardsForDeckSync(deckId: Int): List<Flashcard>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: Flashcard)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(flashcards: List<Flashcard>)

    @Query("UPDATE flashcards SET repetitions=:reps, easeFactor=:ef, intervalDays=:interval, nextReviewAt=:nextAt WHERE id=:id")
    suspend fun updateFlashcardSrs(id: Int, reps: Int, ef: Float, interval: Int, nextAt: Long)

    @Query("SELECT * FROM flashcards WHERE deckId=:deckId AND nextReviewAt <= :now ORDER BY nextReviewAt ASC")
    suspend fun getDueFlashcards(deckId: Int, now: Long = System.currentTimeMillis()): List<Flashcard>

    @Query("SELECT COUNT(*) FROM flashcards WHERE deckId=:deckId AND nextReviewAt <= :now")
    fun getDueCount(deckId: Int, now: Long = System.currentTimeMillis()): Flow<Int>

    // --- Card Reviews ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCardReview(review: CardReview)

    @Query("SELECT * FROM card_reviews WHERE deckId=:deckId ORDER BY reviewedAt DESC")
    fun getReviewsForDeck(deckId: Int): Flow<List<CardReview>>

    // --- Study Plans ---
    @Query("SELECT * FROM study_plans ORDER BY createdAt DESC")
    fun getAllPlans(): Flow<List<StudyPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: StudyPlan)

    @Query("DELETE FROM study_plans WHERE id = :id")
    suspend fun deletePlanById(id: Int)

    // --- Tracked Contests ---
    @Query("SELECT * FROM tracked_contests ORDER BY createdAt DESC")
    fun getAllContests(): Flow<List<TrackedContest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContest(contest: TrackedContest)

    @Query("DELETE FROM tracked_contests WHERE id = :contestId")
    suspend fun deleteContestById(contestId: Int)

    // --- Study Sessions ---
    @Query("SELECT * FROM study_sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<StudySession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySession)
}
