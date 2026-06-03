package com.example.data.repository

import com.example.data.local.StudyDao
import com.example.data.model.*
import com.example.data.remote.GeminiClient
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class StudyRepository(private val studyDao: StudyDao) {

    // Databases Flows
    val allNotes: Flow<List<StudyNote>> = studyDao.getAllNotes()
    val allDecks: Flow<List<Deck>> = studyDao.getAllDecks()
    val allPlans: Flow<List<StudyPlan>> = studyDao.getAllPlans()
    val allContests: Flow<List<TrackedContest>> = studyDao.getAllContests()
    val allSessions: Flow<List<StudySession>> = studyDao.getAllSessions()

    fun getFlashcardsForDeck(deckId: Int): Flow<List<Flashcard>> = studyDao.getFlashcardsForDeck(deckId)
    suspend fun getFlashcardsForDeckSync(deckId: Int): List<Flashcard> = studyDao.getFlashcardsForDeckSync(deckId)
    suspend fun getDeckById(deckId: Int): Deck? = studyDao.getDeckById(deckId)

    // Notes Transactions
    suspend fun saveNote(title: String, content: String, summary: String, topic: String) {
        val note = StudyNote(
            title = title,
            content = content,
            summary = summary,
            topic = topic
        )
        studyDao.insertNote(note)
    }

    suspend fun deleteNote(note: StudyNote) {
        studyDao.deleteNote(note)
    }

    // Decks & Flashcards Transactions
    suspend fun saveDeckWithCards(title: String, topic: String, description: String, cards: List<Pair<String, String>>): Int {
        val deckId = studyDao.insertDeck(Deck(title = title, topic = topic, description = description))
        val flashcardsList = cards.map { (front, back) ->
            Flashcard(deckId = deckId.toInt(), front = front, back = back)
        }
        studyDao.insertFlashcards(flashcardsList)
        return deckId.toInt()
    }

    suspend fun deleteDeck(deckId: Int) {
        studyDao.deleteDeckById(deckId)
    }

    // Plans Transactions
    suspend fun savePlan(topic: String, durationDays: Int, level: String, content: String) {
        val plan = StudyPlan(
            topic = topic,
            durationDays = durationDays,
            level = level,
            planContent = content
        )
        studyDao.insertPlan(plan)
    }

    suspend fun deletePlan(planId: Int) {
        studyDao.deletePlanById(planId)
    }

    // Contests Transactions
    suspend fun saveContest(name: String, organizer: String, examDate: String, editalText: String, notes: String) {
        val contest = TrackedContest(
            name = name,
            organizer = organizer,
            examDate = examDate,
            editalText = editalText,
            notes = notes
        )
        studyDao.insertContest(contest)
    }

    suspend fun deleteContest(contestId: Int) {
        studyDao.deleteContestById(contestId)
    }

    // Sessions Transactions
    suspend fun recordSession(deckId: Int, cardsReviewed: Int, correctAnswers: Int) {
        val session = StudySession(
            deckId = deckId,
            cardsReviewed = cardsReviewed,
            correctAnswers = correctAnswers
        )
        studyDao.insertSession(session)
    }

    // --- Gemini Generation Functions ---

    /**
     * Generates Study Note Summary from text/topic.
     */
    suspend fun generateSummary(title: String, content: String): String {
        return if (GeminiClient.isApiKeyAvailable()) {
            val systemInstruction = "Você é um professor virtual de alta didática encarregado de fazer resumos de estudo excelentes."
            val prompt = """
                Crie um resumo acadêmico elegante e estruturado sobre o assunto: "$title".
                Se houver conteúdo adicional fornecido abaixo, use-o para fundamentar o resumo; caso contrário, use seu próprio conhecimento profundo sobre o assunto.
                
                Conteúdo adicional fornecido:
                ---
                $content
                ---
                
                Seu resumo deve ser ricamente decorado com bullet points, definições cruciais e dividido em tópicos claros (ex: Introdução, Conceitos Fundamentais, Aplicação Prática, Conclusão). Use tags de negrito para destacar palavras-chave essenciais. Responda em Português do Brasil.
            """.trimIndent()
            GeminiClient.fetchContent(prompt, systemInstruction)
        } else {
            getMockSummary(title, content)
        }
    }

    /**
     * Generates interactive flashcards.
     */
    suspend fun generateFlashcards(contentOrTopic: String, count: Int): List<Pair<String, String>> {
        return if (GeminiClient.isApiKeyAvailable()) {
            val systemInstruction = "Você é uma IA programada para gerar flashcards estruturados estritamente em formato JSON."
            val prompt = """
                Gere exatamente $count flashcards de revisão no formato JSON sobre o tema ou conteúdo: "$contentOrTopic".
                A resposta deve ser estritamente uma lista JSON de objetos, onde cada objeto correspondente a um flashcard deve conter exatamente os campos "front" (pergunta direta e curta) e "back" (resposta precisa e sucinta).
                
                Exemplo de formato esperado:
                [
                  {
                    "front": "Qual é a capital do Brasil?",
                    "back": "Brasília."
                  }
                ]
                
                Não retorne nenhum texto antes ou depois do JSON. Não envolva o código em tags de formatação como ```json. Comece com '[' e termine com ']'. Estude o conteúdo profundamente e faça perguntas que ajudem na memorização real do assunto.
            """.trimIndent()
            
            val responseString = GeminiClient.fetchContent(prompt, systemInstruction, jsonOutput = true)
            // Parse JSON manually for complete robustness
            parseFlashcardsJson(responseString)
        } else {
            getMockFlashcards(contentOrTopic, count)
        }
    }

    /**
     * Generates a structural day-by-day Study Plan.
     */
    suspend fun generateStudyPlan(
        topic: String,
        durationDays: Int,
        level: String,
        contestContext: TrackedContest? = null
    ): String {
        return if (GeminiClient.isApiKeyAvailable()) {
            val systemInstruction = "Você é um mentor acadêmico altamente experiente em planos de estudo personalizados."
            val contestSection = if (contestContext != null) {
                buildString {
                    appendLine("O plano de estudos deve levar em consideração o seguinte edital ou concurso:")
                    appendLine("- Nome do concurso: ${contestContext.name}")
                    if (contestContext.organizer.isNotBlank()) appendLine("- Banca Organizadora: ${contestContext.organizer}")
                    if (contestContext.examDate.isNotBlank()) appendLine("- Data prevista da prova: ${contestContext.examDate}")
                    if (contestContext.editalText.isNotBlank()) {
                        appendLine("- Conteúdo programático do edital:")
                        appendLine(contestContext.editalText)
                    }
                    if (contestContext.notes.isNotBlank()) appendLine("- Anotações adicionais: ${contestContext.notes}")
                }.trimEnd()
            } else ""

            val prompt = """
                Crie um roteiro diário estruturado para um plano de estudos com duração de exatamente $durationDays dias para o tema de estudo: "$topic".
                O nível de dificuldade deve ser: "$level" (iniciante, intermediário ou avançado).
                
                $contestSection
                
                A resposta deve ser obrigatoriamente um objeto JSON com o seguinte formato:
                {
                  "title": "Plano de Estudos - $topic",
                  "days": [
                    {
                      "day": 1,
                      "theme": "Título do Dia 1",
                      "tasks": ["Tarefa 1 de estudo", "Tarefa 2 de exercício"],
                      "resources": "Livros recomendados ou tópicos recomendados"
                    }
                  ]
                }
                
                Por favor, retorne APENAS o JSON válido. Não inclua marcas de formatação ```json ou textos explicativos adicionais. Comece e termine estritamente com chaves.
            """.trimIndent()

            val responseString = GeminiClient.fetchContent(prompt, systemInstruction, jsonOutput = true)
            responseString
        } else {
            getMockStudyPlanJson(topic, durationDays, level)
        }
    }

    // --- JSON Parsers ---

    private fun parseFlashcardsJson(jsonStr: String): List<Pair<String, String>> {
        val list = mutableListOf<Pair<String, String>>()
        try {
            // Clean markdown blocks if Gemini added them anyway
            val cleaned = jsonStr.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            val array = JSONArray(cleaned)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val front = obj.optString("front", "Assunto Front")
                val back = obj.optString("back", "Assunto Back")
                list.add(Pair(front, back))
            }
        } catch (e: Exception) {
            // Fallback parsing just in case
            list.add(Pair("Erro ao ler JSON da IA", "Verifique se a chave de API é válida e tente novamente."))
            list.add(Pair("Pergunta Técnica", "Como funciona a memória flash no Android?"))
            list.add(Pair("Resposta Técnica", "Trata-se de um tipo de armazenamento não volátil baseado em células de silício."))
        }
        return list
    }

    // --- Mocks for Offline Testing ---

    private fun getMockSummary(title: String, content: String): String {
        return """
            📚 *Resumo de Estudo Integrado [Modo Simulado]*
            
            Este resumo foi gerado em modo de teste porque nenhuma chave de API do Gemini foi configurada no app.
            
            ### Tema do Estudo
            **$title**
            
            ---
            
            ### 🔍 Visão Geral e Conceitos Fundamentais
            O tema **$title** representa um tópico central no aprendizado de alto nível. Quando nos aprofundamos nesse assunto, devemos focar nos seguintes pilares fundamentais:
            
            1. **Consistência:** O estudo prático e contínuo é mais eficiente que longas sessões espaçadas.
            2. **Retenção Ativa:** Fazer perguntas e utilizar flashcards de recordação ativa acelera a memorização de longo prazo.
            3. **Prática Distribuída:** Spaced Repetition (repetição espaçada) ajuda a mover informações da memória de curto prazo para a de longo prazo.
            
            ---
            
            ### 📝 Tópicos Importantes
            * **Fundamentação:** Conteúdo teórico base. Composto por definições, leis, axiomas ou regras fundamentais do tema.
            * **Dificuldades Comuns:** Erros frequentes cometidos por quem estuda este assunto pela primeira vez (inclui confusão de termos e falta de base).
            * **Casos Práticos:** Como este conhecimento é cobrado em provas nacionais, exames da ordem, concursos ou no desenvolvimento do dia a dia.
            
            ---
            
            ### 💡 Conclusão do Tutor
            A chave para dominar o tópico de **$title** é a prática deliberada combinada com ciclos curtos de revisão. Ao salvar este resumo, você poderá revisá-lo a qualquer momento em sua área de resumos salvos.
        """.trimIndent()
    }

    private fun getMockFlashcards(contentOrTopic: String, count: Int): List<Pair<String, String>> {
        val list = mutableListOf<Pair<String, String>>()
        val mockData = listOf(
            Pair("Qual é o núcleo do estudo ativo?", "A recordação ativa (Active Recall), onde você força seu cérebro a recuperar a informação em vez de apenas reler."),
            Pair("O que prega a curva do esquecimento de Ebbinghaus?", "Mostra que esquecemos as informações de forma exponencial se não as revisarmos estrategicamente."),
            Pair("Como funciona o Método Pomodoro?", "Foco total por 25 minutos seguido por um descanso curto de 5 minutos, aumentando a produtividade e o foco."),
            Pair("Qual a definição de Revisão Espaçada (Spaced Repetition)?", "Uma técnica que consiste em revisar conteúdos em intervalos crescentes para maximizar a retenção."),
            Pair("O que é o Princípio de Pareto (Regra 80/20)?", "Indica que 80% dos resultados que obtemos vêm de 20% dos esforços que empenhamos."),
            Pair("Qual a importância de fazer simulados práticos?", "Treinar sob pressão de tempo, fixar conteúdo e identificar os pontos fracos que exigem mais foco.")
        )

        for (i in 0 until count) {
            val data = mockData[i % mockData.size]
            list.add(Pair("[Modo Demo] #${i + 1}: ${data.first}", data.second))
        }
        return list
    }

    private fun getMockStudyPlanJson(topic: String, durationDays: Int, level: String): String {
        val daysArray = JSONArray()
        for (day in 1..durationDays) {
            val dayObj = JSONObject()
            dayObj.put("day", day)
            dayObj.put("theme", "Dia $day: Introdução e Fundamentos de $topic")
            
            val tasks = JSONArray()
            tasks.put("Ler capítulos base sobre $topic no nível $level")
            tasks.put("Fazer 10 flashcards de recordação ativa")
            tasks.put("Resolver pelo menos 5 exercícios práticos do assunto")
            dayObj.put("tasks", tasks)
            
            dayObj.put("resources", "Mapas mentais, artigos técnicos ou PDF recomendado de $topic")
            daysArray.put(dayObj)
        }

        val root = JSONObject()
        root.put("title", "Plano de Estudos - $topic [Modo Demo]")
        root.put("days", daysArray)
        return root.toString()
    }
}
