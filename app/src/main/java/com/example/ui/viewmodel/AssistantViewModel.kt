package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.*
import com.example.data.repository.AssistantRepository
import com.example.ui.model.Contact
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AssistantViewModel(private val repository: AssistantRepository) : ViewModel() {

    // 1. Unified Flow lists from Room DB
    val callRecords: StateFlow<List<CallRecord>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMemories: StateFlow<List<MemoryItem>> = repository.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRoutines: StateFlow<List<AutomationRoutine>> = repository.allRoutines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Hardware / Device states (Simulated system integrations)
    private val _brightness = MutableStateFlow(0.7f)
    val brightness = _brightness.asStateFlow()

    private val _volume = MutableStateFlow(0.5f)
    val volume = _volume.asStateFlow()

    private val _isWifiOn = MutableStateFlow(true)
    val isWifiOn = _isWifiOn.asStateFlow()

    private val _isBluetoothOn = MutableStateFlow(false)
    val isBluetoothOn = _isBluetoothOn.asStateFlow()

    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn = _isFlashlightOn.asStateFlow()

    private val _isBatterySaverOn = MutableStateFlow(false)
    val isBatterySaverOn = _isBatterySaverOn.asStateFlow()

    private val _batteryPercent = MutableStateFlow(84)
    val batteryPercent = _batteryPercent.asStateFlow()

    private val _hardwareInfo = MutableStateFlow("Axel Snapdragon AI Gen 3 | 12GB RAM Core")
    val hardwareInfo = _hardwareInfo.asStateFlow()

    // 3. UI and Voice Assistant Overlay States
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isCallActive = MutableStateFlow(false)
    val isCallActive = _isCallActive.asStateFlow()

    private val _isIncomingRinging = MutableStateFlow(false)
    val isIncomingRinging = _isIncomingRinging.asStateFlow()

    private val _activeContact = MutableStateFlow<Contact?>(null)
    val activeContact = _activeContact.asStateFlow()

    private val _liveTranscript = MutableStateFlow<List<String>>(emptyList())
    val liveTranscript = _liveTranscript.asStateFlow()

    private val _currentCallType = MutableStateFlow("DELEGATED") // DELEGATED or DIRECT
    val currentCallType = _currentCallType.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage = _statusMessage.asStateFlow()

    // 4. Custom Personalities & Audio preferences
    private val _selectedPersonality = MutableStateFlow("Friendly") // Professional, Friendly, Teacher, Technical
    val selectedPersonality = _selectedPersonality.asStateFlow()

    // 5. Vision AI camera simulator logs
    private val _visionScanResult = MutableStateFlow<String?>(null)
    val visionScanResult = _visionScanResult.asStateFlow()

    private val _isCameraScanning = MutableStateFlow(false)
    val isCameraScanning = _isCameraScanning.asStateFlow()

    // 6. Universal Search states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    init {
        // Pre-populate some baseline automation routines if DB is empty
        viewModelScope.launch {
            repository.allRoutines.first().let { routinesList ->
                if (routinesList.isEmpty()) {
                    repository.insertRoutine(
                        AutomationRoutine(
                            name = "Morning Routine",
                            triggerSource = "Time (7:00 AM)",
                            actionSequence = "Read Weather, Read Calendar, Enable High Brightness",
                            isActive = true
                        )
                    )
                    repository.insertRoutine(
                        AutomationRoutine(
                            name = "Quiet Workspace Auto",
                            triggerSource = "Arrive at Office location",
                            actionSequence = "Disable Bluetooth, Set Volume to 0%, Enable Wi-Fi",
                            isActive = true
                        )
                    )
                    repository.insertRoutine(
                        AutomationRoutine(
                            name = "Extreme Battery Save",
                            triggerSource = "Battery < 20%",
                            actionSequence = "Enable Battery Saver, Reduce Brightness to 20%, Turn off Bluetooth",
                            isActive = true
                        )
                    )
                }
            }

            // Pre-populate Memory preferences
            repository.allMemories.first().let { memoriesList ->
                if (memoriesList.isEmpty()) {
                    repository.insertMemory(
                        MemoryItem(
                            factKey = "Workspace Location",
                            content = "100 Innovation Parkway, Tech District",
                            category = "WORK",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    repository.insertMemory(
                        MemoryItem(
                            factKey = "Preferred Mode",
                            content = "Prefers dark, cyber slate style display designs",
                            category = "PREFERENCE",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    // --- Search filter helper ---
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // --- Memory Operations ---
    fun saveNewMemoryItem(key: String, content: String, category: String) {
        if (key.isBlank() || content.isBlank()) return
        viewModelScope.launch {
            repository.insertMemory(
                MemoryItem(
                    factKey = key,
                    content = content,
                    category = category.uppercase(),
                    timestamp = System.currentTimeMillis()
                )
            )
            _statusMessage.value = "Axel successfully memorized '$key'."
        }
    }

    fun deleteMemory(item: MemoryItem) {
        viewModelScope.launch {
            repository.deleteMemory(item)
            _statusMessage.value = "Memory element removed."
        }
    }

    // --- Automation Routine Operations ---
    fun saveNewRoutine(name: String, trigger: String, actions: String) {
        if (name.isBlank() || actions.isBlank()) return
        viewModelScope.launch {
            repository.insertRoutine(
                AutomationRoutine(
                    name = name,
                    triggerSource = trigger.ifBlank { "Manual Play" },
                    actionSequence = actions,
                    isActive = true
                )
            )
            _statusMessage.value = "Routine '$name' saved to system triggers."
        }
    }

    fun toggleRoutineState(routine: AutomationRoutine) {
        viewModelScope.launch {
            repository.updateRoutine(routine.copy(isActive = !routine.isActive))
        }
    }

    fun deleteRoutine(routine: AutomationRoutine) {
        viewModelScope.launch {
            repository.deleteRoutine(routine)
            _statusMessage.value = "Automation script deleted."
        }
    }

    // Action Execution Engine - Triggers a series of tasks sequentially! (MVP Automation Module)
    fun runAutomationWorkflow(routine: AutomationRoutine) {
        viewModelScope.launch {
            _statusMessage.value = "Triggered Automation: ${routine.name}"
            val steps = routine.actionSequence.split(",").map { it.trim() }
            for (step in steps) {
                delay(1200)
                handleIndexedDeviceCommand(step)
            }
            _statusMessage.value = "${routine.name} successfully automated by Axel AI."
        }
    }

    // --- Direct system control helper (Hardware Module) ---
    fun updateWifi(isOn: Boolean) {
        _isWifiOn.value = isOn
        _statusMessage.value = if (isOn) "Wi-Fi antenna powered up." else "Wi-Fi network disconnected."
    }

    fun updateBluetooth(isOn: Boolean) {
        _isBluetoothOn.value = isOn
        _statusMessage.value = if (isOn) "Bluetooth receiver active." else "Bluetooth receiver disabled."
    }

    fun updateFlashlight(isOn: Boolean) {
        _isFlashlightOn.value = isOn
        _statusMessage.value = if (isOn) "LED Flashlight turned on." else "Flashlight switched off."
    }

    fun updateBatterySaver(isOn: Boolean) {
        _isBatterySaverOn.value = isOn
        if (isOn) {
            _brightness.value = 0.2f
            _isBluetoothOn.value = false
            _statusMessage.value = "Power Saver activated. Brightness reduced."
        } else {
            _statusMessage.value = "Standard power profile restored."
        }
    }

    fun updateVolume(percent: Float) {
        _volume.value = percent
    }

    fun updateBrightness(percent: Float) {
        _brightness.value = percent
    }

    fun changePersonality(p: String) {
        _selectedPersonality.value = p
        _statusMessage.value = "Axel personality aligned: '$p'."
    }

    // Parse commands automatically to change simulated local settings
    private fun handleIndexedDeviceCommand(command: String) {
        val lower = command.lowercase()
        when {
            lower.contains("wifi on") || lower.contains("enable wi-fi") -> _isWifiOn.value = true
            lower.contains("wifi off") || lower.contains("disable wi-fi") -> _isWifiOn.value = false
            lower.contains("bluetooth on") || lower.contains("enable bluetooth") -> _isBluetoothOn.value = true
            lower.contains("bluetooth off") || lower.contains("disable bluetooth") -> _isBluetoothOn.value = false
            lower.contains("flashlight on") -> _isFlashlightOn.value = true
            lower.contains("flashlight off") -> _isFlashlightOn.value = false
            lower.contains("power saver") || lower.contains("battery saver") -> updateBatterySaver(true)
            lower.contains("reduce brightness") -> _brightness.value = 0.2f
            lower.contains("high brightness") -> _brightness.value = 0.95f
            lower.contains("volume to 0") || lower.contains("mute") -> _volume.value = 0.0f
        }
    }

    // --- Vision AI Intelligence OCR scan ---
    fun performSimulatedCameraScan(scannableType: String) {
        viewModelScope.launch {
            _isCameraScanning.value = true
            _visionScanResult.value = "Aligning camera lens... Scanning element: $scannableType"
            delay(1800)

            val query = when (scannableType) {
                "QR Code" -> "Analyze scanning data of simulated QR: https://axel.ai/claim/profile-id=90312"
                "OCR Text" -> "Process OCR Text scanned from business receipt. Extracted: 'COFFEE LAB, 5.50 USD, VISA-1109'"
                "Object Recognition" -> "Identify item captured in focus: Round ceramic green coffee mug next to a metallic laptop stand"
                else -> "Random scanning element analysis"
            }

            try {
                val scanAnswer = repository.getGeminiResponse(
                    systemPrompt = "You are the Vision Intelligence unit of Axel AI. Analyze screen camera inputs instantly and describe findings.",
                    prompt = query
                )
                _visionScanResult.value = scanAnswer
            } catch (e: Exception) {
                _visionScanResult.value = "Vision Module finished scan. Details identified: $query"
            } finally {
                _isCameraScanning.value = false
            }
        }
    }

    fun clearCameraScan() {
        _visionScanResult.value = null
    }

    // --- Chat Voice Simulator Procedures ---
    fun startDirectVoicelineCall(contact: Contact) {
        viewModelScope.launch {
            _activeContact.value = contact
            _currentCallType.value = "DIRECT"
            _liveTranscript.value = listOf(
                "Initializing Axel Audio Core channel...",
                "Hey Axel mode activated.",
                "Axel: Hello! I'm here. I see your network is up and battery level is ${_batteryPercent.value}%. Speak to me!"
            )
            _isCallActive.value = true
        }
    }

    fun submitDirectSpeechText(userText: String) {
        if (userText.isBlank()) return
        viewModelScope.launch {
            _liveTranscript.value = _liveTranscript.value + "User: $userText"
            _isLoading.value = true

            // Trigger automation parsed from live speech as well! (Module 3 + Module 7 crossover)
            handleLiveSpeechHardwareTrigger(userText)

            try {
                // Incorporate dynamic Memory context in prompt
                val learnedMemoriesList = repository.allMemories.first().joinToString("\n") { "- ${it.factKey}: ${it.content}" }
                
                val sysPrompt = """
                    You are Axel, the user's high-fidelity AI virtual companion.
                    Character style: ${_selectedPersonality.value}.
                    Here are some facts you have stored in your Memory Engine:
                    $learnedMemoriesList
                    
                    Respond to the user with ultimate intelligence. Keep response to 2 or 3 conversational sentences.
                """.trimIndent()

                val reply = repository.getGeminiResponse(
                    systemPrompt = sysPrompt,
                    prompt = userText
                )

                _liveTranscript.value = _liveTranscript.value + "Axel: $reply"

                // Insert into past CallRecord transcripts
                val record = CallRecord(
                    contactName = "Hey Axel Dialog",
                    phoneNumber = "*888",
                    direction = "OUTGOING",
                    timestamp = System.currentTimeMillis(),
                    transcript = "User: $userText\nAxel: $reply",
                    summary = "User said: '$userText'. Axel answered formatted as: $reply",
                    durationSeconds = 12
                )
                repository.insertRecord(record)

            } catch (e: Exception) {
                _liveTranscript.value = _liveTranscript.value + "Axel: Static connection error. Processing manually: Resolved."
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun handleLiveSpeechHardwareTrigger(speech: String) {
        val text = speech.lowercase()
        if (text.contains("wifi") || text.contains("wi-fi")) {
            if (text.contains("on") || text.contains("enable")) _isWifiOn.value = true
            if (text.contains("off") || text.contains("disable")) _isWifiOn.value = false
        }
        if (text.contains("bluetooth")) {
            if (text.contains("on") || text.contains("enable")) _isBluetoothOn.value = true
            if (text.contains("off") || text.contains("disable")) _isBluetoothOn.value = false
        }
        if (text.contains("flashlight")) {
            if (text.contains("on")) _isFlashlightOn.value = true
            if (text.contains("off")) _isFlashlightOn.value = false
        }
        if (text.contains("battery saver") || text.contains("power saver")) {
            updateBatterySaver(true)
        }
    }

    // Delete Call Records Logs
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

    fun endVoicelineCall() {
        _isCallActive.value = false
    }

    fun dismissStatus() {
        _statusMessage.value = null
    }

    // Ring simulated inbound contact alert
    fun triggerIncomingCall(contact: Contact) {
        _activeContact.value = contact
        _isIncomingRinging.value = true
    }

    fun acceptIncomingCall() {
        _isIncomingRinging.value = false
        _currentCallType.value = "INCOMING"
        _isCallActive.value = true
        _liveTranscript.value = listOf(
            "Connecting inbound secure line...",
            "Inbound: Connected with Axel companion engine."
        )
    }

    fun rejectIncomingCall() {
        _isIncomingRinging.value = false
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
