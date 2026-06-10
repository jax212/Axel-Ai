package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.CallRecord
import com.example.data.db.MemoryItem
import com.example.data.db.AutomationRoutine
import com.example.ui.model.Contact
import com.example.ui.model.MOCK_CONTACTS
import com.example.ui.theme.*
import com.example.ui.viewmodel.AssistantViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantDashboard(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ViewModel Flows
    val records by viewModel.callRecords.collectAsStateWithLifecycle()
    val memories by viewModel.allMemories.collectAsStateWithLifecycle()
    val routines by viewModel.allRoutines.collectAsStateWithLifecycle()

    val brightness by viewModel.brightness.collectAsStateWithLifecycle()
    val volume by viewModel.volume.collectAsStateWithLifecycle()
    val isWifiOn by viewModel.isWifiOn.collectAsStateWithLifecycle()
    val isBluetoothOn by viewModel.isBluetoothOn.collectAsStateWithLifecycle()
    val isFlashlightOn by viewModel.isFlashlightOn.collectAsStateWithLifecycle()
    val isBatterySaverOn by viewModel.isBatterySaverOn.collectAsStateWithLifecycle()
    val batteryPercent by viewModel.batteryPercent.collectAsStateWithLifecycle()
    val hardwareInfo by viewModel.hardwareInfo.collectAsStateWithLifecycle()

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isCallActive by viewModel.isCallActive.collectAsStateWithLifecycle()
    val isIncomingRinging by viewModel.isIncomingRinging.collectAsStateWithLifecycle()
    val activeContact by viewModel.activeContact.collectAsStateWithLifecycle()
    val liveTranscript by viewModel.liveTranscript.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val callType by viewModel.currentCallType.collectAsStateWithLifecycle()
    val personality by viewModel.selectedPersonality.collectAsStateWithLifecycle()
    val visionResult by viewModel.visionScanResult.collectAsStateWithLifecycle()
    val isCameraScanning by viewModel.isCameraScanning.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    // Navigation and state indices
    var activeTab by remember { mutableIntStateOf(0) }
    var selectedRecordDetail by remember { mutableStateOf<CallRecord?>(null) }
    var inputSpeechText by remember { mutableStateOf("") }

    // Memory form inputs
    var showAddMemoryDialog by remember { mutableStateOf(false) }
    var newMemoryKey by remember { mutableStateOf("") }
    var newMemoryContent by remember { mutableStateOf("") }
    var newMemoryCategory by remember { mutableStateOf("PREFERENCE") }

    // Automation builder dialog
    var showAddRoutineDialog by remember { mutableStateOf(false) }
    var newRoutineName by remember { mutableStateOf("") }
    var newRoutineTrigger by remember { mutableStateOf("") }
    var newRoutineActions by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF070B14),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C1322))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Main Header Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AXEL CORE SYSTEM ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.8.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                        Text(
                            text = "Axel AI Companion",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            )
                        )
                        Text(
                            text = "One AI for Everything",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SkyText,
                                fontStyle = FontStyle.Italic
                            )
                        )
                    }

                    // Direct quick activation of "Hey Axel" voiceline channel
                    Button(
                        onClick = {
                            viewModel.startDirectVoicelineCall(
                                Contact("Hey Axel", "*777", "AI Offline Engine", "🧠", "Turn on wifi.")
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("voiceline_trigger_button")
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "Hey Axel voice command", tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hey Axel", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                // Sub-header stats telemetry indicators
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TelemetryBadge(label = "BATT: $batteryPercent%", color = if (batteryPercent > 20) NeonGreen else NeonPink)
                    TelemetryBadge(label = "WIFI: ${if (isWifiOn) "UP" else "DOWN"}", color = if (isWifiOn) NeonCyan else SkyText)
                    TelemetryBadge(label = "BT: ${if (isBluetoothOn) "UP" else "DOWN"}", color = if (isBluetoothOn) NeonGreen else SkyText)
                    TelemetryBadge(label = "STYLE: $personality", color = NeonPink)
                }
            }
        },
        bottomBar = {
            if (!isCallActive && !isIncomingRinging) {
                ScrollableTabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color(0xFF0A101D),
                    contentColor = NeonCyan,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                            color = NeonCyan
                        )
                    }
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("Core Dial & System") }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("Vision Scanner") }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        text = { Text("Memory Bank") }
                    )
                    Tab(
                        selected = activeTab == 3,
                        onClick = { activeTab = 3 },
                        text = { Text("Automation Routines") }
                    )
                    Tab(
                        selected = activeTab == 4,
                        onClick = { activeTab = 4 },
                        text = { Text("Universal Search") }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Display main interactive modules appropriately based on tab selection
            Crossfade(targetState = activeTab, label = "TabSwitchAnim") { index ->
                when (index) {
                    0 -> ControllerHomeTab(
                        viewModel = viewModel,
                        brightness = brightness,
                        volume = volume,
                        isWifiOn = isWifiOn,
                        isBluetoothOn = isBluetoothOn,
                        isFlashlightOn = isFlashlightOn,
                        isBatterySaverActive = isBatterySaverOn,
                        hardwareInfo = hardwareInfo,
                        selectedStyle = personality,
                        onTriggerDelegate = { viewModel.startDirectVoicelineCall(it) }
                    )
                    1 -> VisionAiTab(
                        isScanning = isCameraScanning,
                        scanResult = visionResult,
                        onScanTriggered = { viewModel.performSimulatedCameraScan(it) },
                        onClearScan = { viewModel.clearCameraScan() }
                    )
                    2 -> MemoryEngineTab(
                        memories = memories,
                        onAddMemoryClick = { showAddMemoryDialog = true },
                        onDeleteMemory = { viewModel.deleteMemory(it) }
                    )
                    3 -> AutomationEngineTab(
                        routines = routines,
                        onAddRoutineClick = { showAddRoutineDialog = true },
                        onToggleRoutine = { viewModel.toggleRoutineState(it) },
                        onRunRoutine = { viewModel.runAutomationWorkflow(it) },
                        onDeleteRoutine = { viewModel.deleteRoutine(it) }
                    )
                    4 -> SearchAndLogsTab(
                        records = records,
                        contacts = MOCK_CONTACTS,
                        memories = memories,
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.updateSearchQuery(it) },
                        onLogClick = { selectedRecordDetail = it },
                        onLogDelete = { viewModel.deleteCallRecord(it) },
                        onClearAllLogs = { viewModel.clearAllCallLogs() }
                    )
                }
            }

            // Floatable snackbar / status banner notification
            statusMessage?.let { msg ->
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "System message logo", tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = msg,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Dialog overlay: Create Memory fact database
            if (showAddMemoryDialog) {
                AlertDialog(
                    onDismissRequest = { showAddMemoryDialog = false },
                    containerColor = DeepSpaceSurface,
                    title = { Text("Teach Axel New Memory fact", color = NeonCyan, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Axel will persist this custom data inside Room Database and reference it during live interactions.", style = MaterialTheme.typography.bodySmall, color = SkyText)
                            OutlinedTextField(
                                value = newMemoryKey,
                                onValueChange = { newMemoryKey = it },
                                label = { Text("Fact Key/Identifier") },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = newMemoryContent,
                                onValueChange = { newMemoryContent = it },
                                label = { Text("Detail content") },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val categories = listOf("PREFERENCE", "CONTACT", "ROUTE", "HABIT")
                                categories.forEach { cat ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (newMemoryCategory == cat) NeonPink else Color.Black.copy(alpha = 0.3f))
                                            .clickable { newMemoryCategory = cat }
                                            .padding(6.dp)
                                    ) {
                                        Text(cat, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newMemoryKey.isNotBlank() && newMemoryContent.isNotBlank()) {
                                    viewModel.saveNewMemoryItem(newMemoryKey, newMemoryContent, newMemoryCategory)
                                    showAddMemoryDialog = false
                                    newMemoryKey = ""
                                    newMemoryContent = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        ) {
                            Text("Insert fact", color = Color.Black)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddMemoryDialog = false }) {
                            Text("Cancel", color = SkyText)
                        }
                    }
                )
            }

            // Dialog overlay: Create custom automation workflow
            if (showAddRoutineDialog) {
                AlertDialog(
                    onDismissRequest = { showAddRoutineDialog = false },
                    containerColor = DeepSpaceSurface,
                    title = { Text("Create Automation Routine", color = NeonCyan, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Map an action workflow list execute sequentially triggered under specific triggers.", style = MaterialTheme.typography.bodySmall, color = SkyText)
                            OutlinedTextField(
                                value = newRoutineName,
                                onValueChange = { newRoutineName = it },
                                label = { Text("Workflow Name (e.g. Work mode)") },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = newRoutineTrigger,
                                onValueChange = { newRoutineTrigger = it },
                                label = { Text("Trigger Source (e.g. Location connected)") },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = newRoutineActions,
                                onValueChange = { newRoutineActions = it },
                                label = { Text("Actions (separated by commas: WIFI OFF, Brightness reduced)") },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newRoutineName.isNotBlank() && newRoutineActions.isNotBlank()) {
                                    viewModel.saveNewRoutine(newRoutineName, newRoutineTrigger, newRoutineActions)
                                    showAddRoutineDialog = false
                                    newRoutineName = ""
                                    newRoutineTrigger = ""
                                    newRoutineActions = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        ) {
                            Text("Establish Script", color = Color.Black)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddRoutineDialog = false }) {
                            Text("Cancel", color = SkyText)
                        }
                    }
                )
            }

            // Past Call list detailed transcript overlay
            selectedRecordDetail?.let { log ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DeepSpaceSurface),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.85f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(log.contactName, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                                    Text("Axel Simulated Logs • ${log.durationSeconds}s", fontSize = 12.sp, color = NeonCyan)
                                }
                                IconButton(onClick = { selectedRecordDetail = null }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close details", tint = SkyText)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.Black.copy(alpha = 0.3f))
                                            .padding(14.dp)
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Info, contentDescription = "Metrics summary icon", tint = NeonGreen, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("AI Outcome Summary", fontWeight = FontWeight.Bold, color = NeonGreen, fontSize = 14.sp)
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(log.summary, color = LightSlate, fontSize = 13.sp, lineHeight = 20.sp)
                                        }
                                    }
                                }

                                item {
                                    Text("Interaction Logs", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SkyText)
                                }

                                val dialogueLines = log.transcript.split("\n").filter { it.isNotBlank() }
                                items(dialogueLines) { row ->
                                    val isMe = row.startsWith("User:") || row.startsWith("Me:")
                                    val bubbleBg = if (isMe) NeonCyan.copy(alpha = 0.12f) else DeepSpaceBackground
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight()
                                    ) {
                                        Text(
                                            text = row,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = if (isMe) NeonCyan else LightSlate,
                                                lineHeight = 18.sp
                                            ),
                                            modifier = Modifier
                                                .align(if (isMe) Alignment.CenterEnd else Alignment.CenterStart)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(bubbleBg)
                                                .padding(10.dp)
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = { selectedRecordDetail = null },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                            ) {
                                Text("Close Log")
                            }
                        }
                    }
                }
            }

            // Realtime active system call / voice simulation core overlay
            if (isCallActive) {
                ActiveCallOverlay(
                    contact = activeContact ?: MOCK_CONTACTS.first(),
                    callType = callType,
                    liveTranscript = liveTranscript,
                    isLoading = isLoading,
                    onEndCallClick = { viewModel.endVoicelineCall() },
                    onSpeechSubmitted = { viewModel.submitDirectSpeechText(it) }
                )
            }

            // Incoming Rings alert
            if (isIncomingRinging) {
                IncomingAlertOverlay(
                    contact = activeContact ?: MOCK_CONTACTS.first(),
                    onAccept = { viewModel.acceptIncomingCall() },
                    onDecline = { viewModel.rejectIncomingCall() }
                )
            }
        }
    }
}

// Badge helper
@Composable
fun TelemetryBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            fontFamily = FontFamily.Monospace
        )
    }
}

// TAB 0: Unified Core controller layout
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControllerHomeTab(
    viewModel: AssistantViewModel,
    brightness: Float,
    volume: Float,
    isWifiOn: Boolean,
    isBluetoothOn: Boolean,
    isFlashlightOn: Boolean,
    isBatterySaverActive: Boolean,
    hardwareInfo: String,
    selectedStyle: String,
    onTriggerDelegate: (Contact) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Module 3: Device Control Center", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            Text("Simulate modifications of system flags directly from voice transcript commands or tactile dials.", color = SkyText, fontSize = 12.sp)
        }

        // Hardware hardware tactile card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepSpaceSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Interactive tactile control switches", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = NeonCyan)
                    
                    // Switches
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TactileToggle(title = "Wi-Fi Indicator", isOn = isWifiOn, onToggle = { viewModel.updateWifi(it) })
                        TactileToggle(title = "Bluetooth link", isOn = isBluetoothOn, onToggle = { viewModel.updateBluetooth(it) })
                        TactileToggle(title = "Flashlight", isOn = isFlashlightOn, onToggle = { viewModel.updateFlashlight(it) })
                    }

                    Divider(color = SkyText.copy(alpha = 0.1f))

                    // Brightness slider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = "Light brightness bar", tint = NeonCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Screen Brightness Level: ${(brightness * 100).toInt()}%", fontSize = 11.sp, color = SkyText)
                            Slider(
                                value = brightness,
                                onValueChange = { viewModel.updateBrightness(it) },
                                colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                            )
                        }
                    }

                    // Volume slider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Hardware volume speaker bar", tint = NeonGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("System Core Audio Volume: ${(volume * 100).toInt()}%", fontSize = 11.sp, color = SkyText)
                            Slider(
                                value = volume,
                                onValueChange = { viewModel.updateVolume(it) },
                                colors = SliderDefaults.colors(thumbColor = NeonGreen, activeTrackColor = NeonGreen)
                            )
                        }
                    }

                    Divider(color = SkyText.copy(alpha = 0.1f))

                    // Power battery optimization saver trigger
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Extreme Battery Power Saver", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text("Slows core clocks and dims system screen brightness to 20% to safeguard life.", fontSize = 10.sp, color = SkyText)
                        }
                        Switch(
                            checked = isBatterySaverActive,
                            onCheckedChange = { viewModel.updateBatterySaver(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonPink, checkedTrackColor = NeonPink.copy(alpha = 0.4f))
                        )
                    }
                }
            }
        }

        // Module Personality settings aligner
        item {
            Text("Module 19: Custom Personality Mode", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            Text("Re-arrange Axel's vocal tone parameters real-time.", color = SkyText, fontSize = 12.sp)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepSpaceSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val styles = listOf("Friendly", "Professional", "Teacher", "Technical Expert")
                    styles.forEach { s ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedStyle == s) NeonCyan else Color.Black.copy(alpha = 0.3f))
                                .clickable { viewModel.changePersonality(s) }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                s,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedStyle == s) Color.Black else Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Speed Dial delegates
        item {
            Text("Module 1+16: Quick Assistant Dial Contacts", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            Text("Select an integrated contact receiver below to spin up a simulated call.", color = SkyText, fontSize = 12.sp)
        }

        items(MOCK_CONTACTS) { contact ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepSpaceSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(contact.avatarEmoji, fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(contact.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        Text(contact.role, fontSize = 11.sp, color = NeonCyan)
                        Text(contact.suggestedPrompt, fontSize = 10.sp, color = SkyText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(
                        onClick = { onTriggerDelegate(contact) },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.15f))
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "Dial quick channel contact", tint = NeonCyan)
                    }
                }
            }
        }
    }
}

@Composable
fun TactileToggle(title: String, isOn: Boolean, onToggle: (Boolean) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(title, fontSize = 10.sp, color = SkyText, fontWeight = FontWeight.SemiBold)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(32.dp))
                .background(if (isOn) NeonGreen.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.3f))
                .clickable { onToggle(!isOn) }
                .border(
                    width = 1.dp,
                    color = if (isOn) NeonGreen else SkyText.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (isOn) "ON" else "OFF",
                fontSize = 10.sp,
                color = if (isOn) NeonGreen else SkyText,
                fontWeight = FontWeight.Black
            )
        }
    }
}

// TAB 1: Vision Intelligence simulator OCR scan module
@Composable
fun VisionAiTab(
    isScanning: Boolean,
    scanResult: String?,
    onScanTriggered: (String) -> Unit,
    onClearScan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Module 4: Vision AI Intelligence Scanner", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
        Text("Bring the camera lens online dynamically or execute direct simulated OCR scanning of environment objects using Gemini feedback.", color = SkyText, fontSize = 12.sp)

        // Cam Viewfinder container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black)
                .border(2.dp, if (isScanning) NeonPink else NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isScanning) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = NeonPink)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("ALIGNING LENS METADATA ARCHITECTURE...", style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = NeonPink), fontSize = 10.sp)
                }
            } else {
                Text("SCANNING VIEW PORT OFFLINE\nClick a core scan script button below to trigger analysis.", color = SkyText, textAlign = TextAlign.Center, fontSize = 12.sp)
            }
        }

        // Realtime logs analyzer outcome
        scanResult?.let { outcome ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepSpaceSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("IDENTIFIED RESULTS LOG", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NeonGreen)
                        IconButton(onClick = onClearScan) {
                            Icon(Icons.Default.Close, contentDescription = "Close scanner analysis log", tint = SkyText, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Text(outcome, color = LightSlate, fontSize = 13.sp, lineHeight = 20.sp)
                        }
                    }
                }
            }
        }

        // Control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val scannables = listOf("QR Code", "OCR Text", "Object Recognition")
            scannables.forEach { item ->
                Button(
                    onClick = { onScanTriggered(item) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepSpaceSurface),
                    border = ButtonDefaults.outlinedButtonBorder,
                    enabled = !isScanning
                ) {
                    Text(item, fontSize = 9.sp, color = NeonCyan, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

// TAB 2: Offline Memory Engine & Graph elements
@Composable
fun MemoryEngineTab(
    memories: List<MemoryItem>,
    onAddMemoryClick: () -> Unit,
    onDeleteMemory: (MemoryItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Module 5+6: Memory Engine Bank", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                Text("Axel references these persisted Room database parameters to answer smart direct dialogue.", color = SkyText, fontSize = 11.sp)
            }

            IconButton(
                onClick = onAddMemoryClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(NeonCyan)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Insert memory", tint = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (memories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Memory engine empty. Click the '+' button to feed information facts.", color = SkyText, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(memories) { mem ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DeepSpaceSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NeonPink.copy(alpha = 0.15f))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    mem.category.take(4),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonPink,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(mem.factKey, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text(mem.content, fontSize = 12.sp, color = LightSlate)
                            }
                            IconButton(onClick = { onDeleteMemory(mem) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Forget fact", tint = NeonPink, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// TAB 3: Automation Routine manager list
@Composable
fun AutomationEngineTab(
    routines: List<AutomationRoutine>,
    onAddRoutineClick: () -> Unit,
    onToggleRoutine: (AutomationRoutine) -> Unit,
    onRunRoutine: (AutomationRoutine) -> Unit,
    onDeleteRoutine: (AutomationRoutine) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Module 7: System Automation Workflows", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                Text("Run customizable workflow actions of sequential device commands offline.", color = SkyText, fontSize = 11.sp)
            }
            IconButton(
                onClick = onAddRoutineClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(NeonCyan)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New automation routine", tint = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(routines) { rot ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepSpaceSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(rot.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if (rot.isActive) NeonGreen else SkyText))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Trigger: ${rot.triggerSource}", fontSize = 11.sp, color = SkyText)
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = rot.isActive,
                                    onCheckedChange = { onToggleRoutine(rot) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen)
                                )
                                IconButton(onClick = { onDeleteRoutine(rot) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete routine", tint = NeonPink, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.2f))
                                .padding(8.dp)
                        ) {
                            Text(
                                "Sequence Actions: " + rot.actionSequence,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = NeonCyan
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onRunRoutine(rot) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = if (rot.isActive) NeonCyan else Color.Gray)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Execute workflow", tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Run Workflow Sequence", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// TAB 4: Search & Transcripts
@Composable
fun SearchAndLogsTab(
    records: List<CallRecord>,
    contacts: List<Contact>,
    memories: List<MemoryItem>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onLogClick: (CallRecord) -> Unit,
    onLogDelete: (CallRecord) -> Unit,
    onClearAllLogs: () -> Unit
) {
    val dateHelper = remember { SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault()) }

    // Multi-source Search logic (Universal Search Module 9)
    val filteredRecords = remember(records, searchQuery) {
        if (searchQuery.isBlank()) records else records.filter {
            it.contactName.contains(searchQuery, ignoreCase = true) ||
            it.summary.contains(searchQuery, ignoreCase = true) ||
            it.transcript.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredContacts = remember(contacts, searchQuery) {
        if (searchQuery.isBlank()) emptyList() else contacts.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.role.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredMemories = remember(memories, searchQuery) {
        if (searchQuery.isBlank()) emptyList() else memories.filter {
            it.factKey.contains(searchQuery, ignoreCase = true) ||
            it.content.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Module 9: Universal Axel Search & Logs", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
        Text("Perform systemic searching across messages, memories, contacts, and speech transcriptions.", color = SkyText, fontSize = 11.sp)

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search files, memories, logs...", color = SkyText) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search bar magnifying glass icon", tint = NeonCyan) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = NeonCyan
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Searched Contacts results section
            if (filteredContacts.isNotEmpty()) {
                item { Text("Matched contacts Results", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                items(filteredContacts) { item ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(item.avatarEmoji, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(item.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(item.role, color = SkyText, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Searched Memories results section
            if (filteredMemories.isNotEmpty()) {
                item { Text("Matched memories Results", color = NeonPink, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                items(filteredMemories) { item ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF3B0728)), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            Column {
                                Text(item.factKey, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(item.content, color = SkyText, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Main system transcript logs header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (searchQuery.isBlank()) "All Past Speech Logs & Transmissions" else "Matched Logs Results",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    if (records.isNotEmpty() && searchQuery.isBlank()) {
                        Text(
                            "Delete All Logs",
                            color = NeonPink,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onClearAllLogs() }
                        )
                    }
                }
            }

            if (filteredRecords.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                        Text("No logs found match the search query.", color = SkyText, fontSize = 12.sp)
                    }
                }
            } else {
                items(filteredRecords) { log ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DeepSpaceSurface),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLogClick(log) }
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NeonGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Simulated refresh indicator", tint = NeonGreen, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(log.contactName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                Text(log.summary.take(74) + "...", style = MaterialTheme.typography.bodySmall, color = LightSlate, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(dateHelper.format(Date(log.timestamp)), fontSize = 8.sp, color = SkyText)
                                IconButton(onClick = { onLogDelete(log) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Forget log element", tint = NeonPink, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Active Call layout voice interface simulation overlay (MODERN & GLOWING DESIGN)
@Composable
fun ActiveCallOverlay(
    contact: Contact,
    callType: String,
    liveTranscript: List<String>,
    isLoading: Boolean,
    onEndCallClick: () -> Unit,
    onSpeechSubmitted: (String) -> Unit
) {
    val transcriptState = rememberLazyListState()
    var userSpokenDraftText by remember { mutableStateOf("") }

    LaunchedEffect(liveTranscript.size) {
        if (liveTranscript.isNotEmpty()) {
            transcriptState.animateScrollToItem(liveTranscript.size - 1)
        }
    }

    val infinitePulse = rememberInfiniteTransition(label = "pulse")
    val waveScale by infinitePulse.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Reverse),
        label = "scaling"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060912))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // System Line Badge status tracker
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.04f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if (isLoading) NeonPink else NeonCyan))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isLoading) "AXEL MODEL IS DECIPHERING..." else "SECURE AUDIOLINE FEED ENCRYPTED",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isLoading) NeonPink else NeonCyan
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Audio visualizer Core sphere
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(waveScale)
                ) {
                    drawCircle(
                        color = if (isLoading) NeonPink.copy(alpha = 0.12f) else NeonCyan.copy(alpha = 0.12f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F1626)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(contact.avatarEmoji, fontSize = 42.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (callType == "DIRECT") "Hey Axel Assistant Channel" else contact.name,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = Color.White
            )
            Text(text = "Operational Line Link: *889-AXEL-COGNITIVE", fontSize = 11.sp, color = SkyText)

            Spacer(modifier = Modifier.height(18.dp))

            // Scrollable Realtime visual scroll transcript logs
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepSpaceSurface.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("LIVE SPEECH CAPTURED TRANSMISSION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SkyText)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        state = transcriptState,
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(liveTranscript) { turn ->
                            val isMe = turn.startsWith("User:") || turn.startsWith("Me:") || turn.startsWith("Calling")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isMe) NeonCyan.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = turn,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        color = if (isMe) NeonCyan else LightSlate
                                    )
                                }
                            }
                        }
                    }

                    if (isLoading) {
                        Text(
                            text = "Axel is synthesizing natural speech responses...",
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic,
                            color = NeonCyan,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Speech typing inputs for simulated real audio discussions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = userSpokenDraftText,
                    onValueChange = { userSpokenDraftText = it },
                    placeholder = { Text("Say something to Axel AI...", color = SkyText, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonCyan
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        if (userSpokenDraftText.isNotBlank()) {
                            onSpeechSubmitted(userSpokenDraftText)
                            userSpokenDraftText = ""
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(NeonCyan)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send mic command speech text", tint = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Switch layout hang-up
            IconButton(
                onClick = onEndCallClick,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(NeonPink)
                    .testTag("hangup_button")
            ) {
                Icon(Icons.Default.Close, contentDescription = "Disconnect operational voice path", tint = Color.White)
            }
        }
    }
}

// Inbound connection alert
@Composable
fun IncomingAlertOverlay(
    contact: Contact,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF03050C))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Text(
                    "INCOMING SECURE CALL TUNNEL",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = NeonGreen,
                    letterSpacing = 2.sp,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(NeonGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(contact.avatarEmoji, fontSize = 48.sp)
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White)
                Text(contact.role, fontSize = 13.sp, color = SkyText)
            }

            // Options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Reject
                IconButton(
                    onClick = onDecline,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(NeonPink)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Reject call link", tint = Color.White)
                }

                // Accept
                IconButton(
                    onClick = onAccept,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(NeonGreen)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = "Accept call link", tint = Color.Black)
                }
            }
        }
    }
}
