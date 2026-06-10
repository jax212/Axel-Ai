package com.example.data.repository

import com.example.data.api.*
import com.example.data.db.*
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AssistantRepository(
    private val callRecordDao: CallRecordDao,
    private val memoryItemDao: MemoryItemDao,
    private val automationRoutineDao: AutomationRoutineDao
) {

    // Call Records Flows
    val allRecords: Flow<List<CallRecord>> = callRecordDao.getAllRecords()

    suspend fun insertRecord(record: CallRecord): Long = withContext(Dispatchers.IO) {
        callRecordDao.insertRecord(record)
    }

    suspend fun deleteRecord(record: CallRecord) = withContext(Dispatchers.IO) {
        callRecordDao.deleteRecord(record)
    }

    suspend fun clearAllRecords() = withContext(Dispatchers.IO) {
        callRecordDao.clearAll()
    }

    // Memories Flows
    val allMemories: Flow<List<MemoryItem>> = memoryItemDao.getAllMemories()

    suspend fun insertMemory(item: MemoryItem): Long = withContext(Dispatchers.IO) {
        memoryItemDao.insertMemory(item)
    }

    suspend fun deleteMemory(item: MemoryItem) = withContext(Dispatchers.IO) {
        memoryItemDao.deleteMemory(item)
    }

    suspend fun deleteMemoryById(id: Long) = withContext(Dispatchers.IO) {
        memoryItemDao.deleteMemoryById(id)
    }

    suspend fun clearAllMemories() = withContext(Dispatchers.IO) {
        memoryItemDao.clearAll()
    }

    // Automation Workflows Flows
    val allRoutines: Flow<List<AutomationRoutine>> = automationRoutineDao.getAllRoutines()

    suspend fun insertRoutine(routine: AutomationRoutine): Long = withContext(Dispatchers.IO) {
        automationRoutineDao.insertRoutine(routine)
    }

    suspend fun updateRoutine(routine: AutomationRoutine) = withContext(Dispatchers.IO) {
        automationRoutineDao.updateRoutine(routine)
    }

    suspend fun deleteRoutine(routine: AutomationRoutine) = withContext(Dispatchers.IO) {
        automationRoutineDao.deleteRoutine(routine)
    }

    suspend fun clearAllRoutines() = withContext(Dispatchers.IO) {
        automationRoutineDao.clearAll()
    }

    // Direct chat helper with Gemini
    suspend fun getGeminiResponse(
        systemPrompt: String,
        prompt: String,
        history: List<Content> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getMockResponse(prompt, systemPrompt)
        }

        try {
            val userContent = Content(parts = listOf(Part(text = prompt)))
            val fullContents = history + listOf(userContent)

            val request = GenerateContentRequest(
                contents = fullContents,
                systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
            )

            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "I apologize, but I could not process that request currently."
        } catch (e: Exception) {
            e.printStackTrace()
            "I encountered a small signal issue but am still running offline. Here is my simulated response: ${getMockResponse(prompt, systemPrompt)}"
        }
    }

    // Create realistic transcript logs
    suspend fun generateAutomatedCallTranscript(
        contactName: String,
        objective: String,
        contactRole: String
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            You are Axel AI assistant. Generate a realistic logs of standard dialogue simulating an automated delegate conversation on behalf of the user with $contactName ($contactRole) discussing: "$objective".
            Make sure Axel introduces itself as the user's personal companion, resolves the objective efficiently, and structures a clear conversation.
            Structure:
            Axel: [Dialogue]
            Participant: [Dialogue]
            
            At the end of the script output:
            "SUMMARY: [Saves outline]"
            "FOLLOW_UPS: - Task list item 1\n- Task list item 2"
        """.trimIndent()

        val systemPrompt = "You are Axel, the elite multi-agent administrative voice companion."
        getGeminiResponse(systemPrompt, prompt)
    }

    // Mock replies targeting Axel AI identity
    private fun getMockResponse(prompt: String, systemPrompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("axel") -> {
                "Hello! This is Axel AI. I am your supreme virtual voice agent who is listening, ready to remember facts, scan inputs, or automate tasks. What key instruction shall we run?"
            }
            lower.contains("appointment") || lower.contains("dentist") || lower.contains("emily") -> {
                "Setting the Friday dentist booking with Dr. Emily. In addition, I will create a reminder for Friday morning at 9:30 AM to prepare."
            }
            lower.contains("wifi") || lower.contains("bluetooth") || lower.contains("brightness") || lower.contains("flashlight") -> {
                "System action decoded: Simulating direct Android device modification. Completed successfully."
            }
            lower.contains("scan") || lower.contains("qr") || lower.contains("barcode") || lower.contains("ocr") || lower.contains("camera") -> {
                "Vision Intelligence processing complete: Identified QR metadata/OCR documentation text successfully."
            }
            else -> {
                "Understood. Axel AI is processing: '$prompt'. I can synchronize calendars, manage device controls (Wi-Fi, brightness), index notes, triggers, or analyze scanned elements."
            }
        }
    }
}
