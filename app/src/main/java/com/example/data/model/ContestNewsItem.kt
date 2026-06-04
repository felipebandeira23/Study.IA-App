package com.example.data.model

/** In-memory model for a contest news card fetched via Gemini Search Grounding. */
data class ContestNewsItem(
    val title: String,
    val area: String,
    val summary: String,
    val examDate: String = "A definir",
    val salary: String = "",
    val vacancies: String = "",
    val inscriptionDeadline: String = "A definir",
    val isNational: Boolean = false
)

/** In-memory result of auto-filling a contest form via Gemini Search Grounding. */
data class ContestAutoFill(
    val organizer: String = "",
    val examDate: String = "",
    val salary: String = "",
    val vacancies: String = "",
    val editalContent: String = ""
)
