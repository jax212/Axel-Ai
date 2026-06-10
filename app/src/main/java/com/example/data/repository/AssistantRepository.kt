package com.example.data.repository

import com.example.data.api.*
import com.example.data.db.CallRecord
import com.example.data.db.CallRecordDao
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AssistantRepository(private val callRecordDao: CallRecordDao) {

    val allRecords: Flow<List<CallRecord>> = callRecordDao.getAllRecords()

    suspend fun insertRecord(record: CallRecord): Long = withContext(Dispatchers.IO) {
        callRecordDao.insertRecord(record)
    }

    suspend fun updateRecord(record: CallRecord) = withContext(Dispatchers.IO) {
        callRecordDao.updateRecord(record)
    }

    suspend fun deleteRecord(record: CallRecord) = withContext(Dispatchers.IO) {
        callRecordDao.deleteRecord(record)
    }

    suspend fun clearAllRecords() = withContext(Dispatchers.IO) {
        callRecordDao.clearAll()
    }

    // Direct chat helper with Gemini
    suspend fun getGeminiResponse(
        systemPrompt: String,
        prompt: String,
        history: List<Content> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Local high-fidelity mockup reply in case API key is not configured in the secrets panel
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
             "I encountered a small signal issue but am still running. (Error: ${e.localizedMessage ?: "Connection Timeout"}). Let's simulate a reply: ${getMockResponse(prompt, systemPrompt)}"
        }
    }

    // Generate a cohesive, realistic automated phone call transcripts (AI Delegate vs Responder)
    suspend fun generateAutomatedCallTranscript(
        contactName: String,
        objective: String,
        contactRole: String
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            You are a phone simulator. Generate a highly realistic phone conversation transcription log between our AI Voice Assistant and $contactName ($contactRole).
            Our AI Assistant's objective: "$objective"
            Make it look like a real-time transcript layout. Keep it around 4 to 6 alternating turns.
            Include timestamp-style markers or short indicators.
            At the end, append a short block:
            "SUMMARY: [brief summary of outcome in 2 sentences]"
            "FOLLOW_UPS: [bullet points of actions]"
            
            Do not use markdown formatting like asterisks or code blocks. Just plain text.
        """.trimIndent()

        val systemPrompt = "You are a professional administrative virtual assistant simulator."
        getGeminiResponse(systemPrompt, prompt)
    }

    // Generate smart response drafts (SMS / Email)
    suspend fun generateDraft(
        objective: String,
        sender: String = "AI Assistant",
        recipient: String
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            Write an elite, highly professional and friendly draft response (either email or SMS, whichever is more appropriate) to $recipient.
            Our goal/objective: "$objective"
            Make it polished and concise. Avoid heavy placeholders; make it ready to send.
            Mention it was drafted by $sender.
        """.trimIndent()

        val systemPrompt = "You are an elite productivity drafting companion."
        getGeminiResponse(systemPrompt, prompt)
    }

    // Mock responses in case of missing internet or missing API key
    private fun getMockResponse(prompt: String, systemPrompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hi") -> {
                "Hello! This is Seraphina, your AI Phone Assistant. I'm connected and ready to secure your calls, draft messages, or manage schedules today. What task would you like to assign me?"
            }
            lower.contains("appointment") || lower.contains("dentist") || lower.contains("emily") -> {
                "I've analyzed Dr. Emily's office timings. Reaching out to schedule your dental cleanup on Friday morning at 9:30 AM is completely feasible. Shall I delegate an automated call to confirm this right now?"
            }
            lower.contains("landlord") || lower.contains("john") || lower.contains("rent") || lower.contains("faucet") -> {
                "Got it. I can reach out to John (your landlord) to request a plumbing check for that leaky faucet and ask for an extension on Sunday's checkout time. I will draft the message or dial his virtual office."
            }
            lower.contains("late") || lower.contains("sarah") -> {
                "I will draft a quick, professional SMS to Sarah immediately: 'Hi Sarah, Seraphina here on behalf of your co-author. Just letting you know they are running about 10 minutes behind schedule but are excited to review the draft chapter soon!'"
            }
            lower.contains("summary") || lower.contains("summarize") -> {
                "SUMMARY: The call concluded successfully. Dr. Emily's desk confirmed the dental hygiene session is booked for this Friday morning at 10:00 AM. \n\nFOLLOW-UPS:\n- Add appointment to Google Calendar\n- Prepare insurance document details"
            }
            else -> {
                "Understood. I will process your request: '$prompt'. As your AI assistant, I can place automated delegate calls, summarize historic voice transmissions, or compile smart text drafts. What would you like to execute?"
            }
        }
    }
}
