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

    // --- All Flashcards (used for review-all mode) ---
    @Query("SELECT * FROM flashcards")
    fun getAllFlashcards(): Flow<List<Flashcard>>

    // --- Edital Topics ---
    @Query("SELECT * FROM edital_topics WHERE contestId = :contestId ORDER BY id ASC")
    fun getEditalTopicsForContest(contestId: Int): Flow<List<EditalTopic>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEditalTopic(topic: EditalTopic)

    @Query("UPDATE edital_topics SET status = :status WHERE id = :topicId")
    suspend fun updateEditalTopicStatus(topicId: Int, status: Int)

    @Query("DELETE FROM edital_topics WHERE contestId = :contestId")
    suspend fun deleteEditalTopicsForContest(contestId: Int)
}
