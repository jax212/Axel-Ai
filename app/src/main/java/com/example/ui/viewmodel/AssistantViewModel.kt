package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.CallRecord
import com.example.data.repository.AssistantRepository
import com.example.ui.model.Contact
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AssistantViewModel(private val repository: AssistantRepository) : ViewModel() {

    // Read reactive local call records
    val callRecords: StateFlow<List<CallRecord>> = repository.allRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Call state variables
    private val _isCallActive = MutableStateFlow(false)
    val isCallActive: StateFlow<Boolean> = _isCallActive.asStateFlow()

    private val _isIncomingRinging = MutableStateFlow(false)
    val isIncomingRinging: StateFlow<Boolean> = _isIncomingRinging.asStateFlow()

    private val _activeContact = MutableStateFlow<Contact?>(null)
    val activeContact: StateFlow<Contact?> = _activeContact.asStateFlow()

    private val _liveTranscript = MutableStateFlow<List<String>>(emptyList())
    val liveTranscript: StateFlow<List<String>> = _liveTranscript.asStateFlow()

    // Current drafted text for copy/send
    private val _draftText = MutableStateFlow<String?>(null)
    val draftText: StateFlow<String?> = _draftText.asStateFlow()

    // Current error/status messages
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _currentCallType = MutableStateFlow("DELEGATED") // DELEGATED or INTERACTIVE
    val currentCallType: StateFlow<String> = _currentCallType.asStateFlow()

    // Start a simulated automated delegated call (AI represents user to the contact)
    fun startDelegatedCall(contact: Contact, customObjective: String) {
        viewModelScope.launch {
            _activeContact.value = contact
            _currentCallType.value = "DELEGATED"
            _liveTranscript.value = listOf("Connecting secure neural link...", "Vibe-check line alignment okay.", "Calling ${contact.name}...")
            _isCallActive.value = true
            _isLoading.value = true

            try {
                // Fetch transcript from Gemini
                val targetObjective = customObjective.ifBlank { contact.suggestedPrompt }
                val completeScript = repository.generateAutomatedCallTranscript(
                    contactName = contact.name,
                    objective = targetObjective,
                    contactRole = contact.role
                )

                // Render lines step by step to simulate live audio
                _isLoading.value = false
                val rawLines = completeScript.split("\n")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                // Filter out final summary / followups from the scrolling log
                val convoLines = rawLines.filter { 
                    !it.startsWith("SUMMARY") && !it.startsWith("FOLLOW_UPS") && !it.startsWith("FOLLOW-UP") 
                }

                // Extract summary and follow_ups
                val summaryLine = rawLines.firstOrNull { it.contains("SUMMARY:", ignoreCase = true) }
                    ?.replace("SUMMARY:", "", ignoreCase = true)?.trim()
                    ?: "Negotiation was simulated successfully and saved."
                
                val followups = rawLines.filter { it.startsWith("-") || it.contains("FOLLOW_UP") || it.contains("FOLLOW-UP") }
                    .joinToString("\n")
                    .ifBlank { "- Review automated outcome\n- Sync contact calendar" }

                // Scroll the conversation turns
                for (line in convoLines) {
                    delay(1800)
                    _liveTranscript.value = _liveTranscript.value + line
                }

                delay(1200)
                _liveTranscript.value = _liveTranscript.value + "Call finished. Synchronizing records..."
                delay(1000)

                // Save CallRecord into Room Database
                val record = CallRecord(
                    contactName = contact.name,
                    phoneNumber = contact.phone,
                    direction = "DELEGATED",
                    timestamp = System.currentTimeMillis(),
                    transcript = convoLines.joinToString("\n"),
                    summary = "$summaryLine\n\nFollow-ups:\n$followups",
                    durationSeconds = convoLines.size * 3 + 10
                )
                repository.insertRecord(record)

            } catch (e: Exception) {
                _liveTranscript.value = _liveTranscript.value + "Connection error: ${e.localizedMessage}"
                _statusMessage.value = "Failed to run automated assistant call."
            } finally {
                _isLoading.value = false
                _isCallActive.value = false
            }
        }
    }

    // Toggle simulated Incoming Call Ringing
    fun triggerIncomingCall(contact: Contact) {
        _activeContact.value = contact
        _isIncomingRinging.value = true
    }

    // Accept Incoming Call (Go into interactive mode)
    fun acceptIncomingCall() {
        val contact = _activeContact.value ?: return
        _isIncomingRinging.value = false
        _currentCallType.value = "INCOMING"
        _isCallActive.value = true
        _liveTranscript.value = listOf("Connected with Seraphina", "Assistant: Hello! I'm here. How can I assist your schedules today?")
        
        // Simulating the user accepting the call
    }

    // Decline/End Call
    fun endCall() {
        _isCallActive.value = false
        _isIncomingRinging.value = false
    }

    // Submit user typing during active manual conversation with Seraphina
    fun sendMessageToActiveCall(userSpeech: String) {
        if (userSpeech.isBlank()) return
        val contact = _activeContact.value ?: return
        
        viewModelScope.launch {
            _liveTranscript.value = _liveTranscript.value + "Me: $userSpeech"
            _isLoading.value = true
            
            try {
                // Ask Gemini for immediate feedback during interactive call
                val response = repository.getGeminiResponse(
                    systemPrompt = "You are Seraphina, the user's ultimate personal administrative phone voice assistant. Respond concisely to the user in a friendly, conversational audio-style format.",
                    prompt = userSpeech
                )
                _liveTranscript.value = _liveTranscript.value + "Seraphina: $response"

                // Create and insert call log record
                val record = CallRecord(
                    contactName = contact.name,
                    phoneNumber = contact.phone,
                    direction = "INCOMING",
                    timestamp = System.currentTimeMillis(),
                    transcript = "Me: $userSpeech\nSeraphina: $response",
                    summary = "User discussed: '$userSpeech'\nAssistant response: '$response'",
                    durationSeconds = 15
                )
                repository.insertRecord(record)

            } catch (e: Exception) {
                _liveTranscript.value = _liveTranscript.value + "Signal static: Could not reach assistant."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Generate Smart Draft SMS/Letter using Gemini
    fun createSmartDraft(contact: Contact, customInstruction: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _draftText.value = "Generating draft..."
            try {
                val objective = customInstruction.ifBlank { contact.suggestedPrompt }
                val draft = repository.generateDraft(
                    objective = objective,
                    recipient = contact.name
                )
                _draftText.value = draft
            } catch (e: Exception) {
                _draftText.value = "Error generating draft: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Clear history logs
    fun deleteCallRecord(record: CallRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record)
        }
    }

    fun clearAllCallLogs() {
        viewModelScope.launch {
            repository.clearAllRecords()
        }
    }

    fun dismissStatus() {
        _statusMessage.value = null
    }

    fun clearDraft() {
        _draftText.value = null
    }
}

class AssistantViewModelFactory(private val repository: AssistantRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AssistantViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AssistantViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
