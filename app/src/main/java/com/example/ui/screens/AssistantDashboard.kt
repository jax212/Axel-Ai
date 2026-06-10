package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.CallRecord
import com.example.ui.model.Contact
import com.example.ui.model.MOCK_CONTACTS
import com.example.ui.theme.*
import com.example.ui.viewmodel.AssistantViewModel
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

    val records by viewModel.callRecords.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isCallActive by viewModel.isCallActive.collectAsStateWithLifecycle()
    val isIncomingRinging by viewModel.isIncomingRinging.collectAsStateWithLifecycle()
    val activeContact by viewModel.activeContact.collectAsStateWithLifecycle()
    val liveTranscript by viewModel.liveTranscript.collectAsStateWithLifecycle()
    val draftText by viewModel.draftText.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val callType by viewModel.currentCallType.collectAsStateWithLifecycle()

    var activeTab by remember { mutableIntStateOf(0) }
    var showDelegateDialog by remember { mutableStateOf<Contact?>(null) }
    var delegateInstruction by remember { mutableStateOf("") }
    var quickDraftInstruction by remember { mutableStateOf("") }
    var selectedRecordForDetail by remember { mutableStateOf<CallRecord?>(null) }
    var directChatSpeech by remember { mutableStateOf("") }

    // Floating snackbar messages handler
    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            delay(3000)
            viewModel.dismissStatus()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0F172A),
                                Color(0xFF020617)
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
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
                                    .background(NeonGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SERAPHINA CORE v1.2",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeonGreen,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                        Text(
                            text = "AI Phone Assistant",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            )
                        )
                    }

                    // Simulated Trigger Incoming call button
                    IconButton(
                        onClick = { viewModel.triggerIncomingCall(MOCK_CONTACTS.first()) },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DeepSpaceSurface)
                            .testTag("simulate_incoming_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Simulate incoming system check",
                            tint = NeonCyan
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (!isCallActive && !isIncomingRinging) {
                NavigationBar(
                    containerColor = Color(0xFF0E1626),
                    tonalElevation = 8.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    NavigationBarItem(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Contacts") },
                        label = { Text("Contacts") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            unselectedIconColor = SkyText,
                            unselectedTextColor = SkyText,
                            indicatorColor = DeepSpaceSurface
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        icon = { Icon(Icons.Default.List, contentDescription = "History Logs") },
                        label = { Text("Call Logs") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            unselectedIconColor = SkyText,
                            unselectedTextColor = SkyText,
                            indicatorColor = DeepSpaceSurface
                        )
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
            // Main Dashboard Content Tabs
            Crossfade(targetState = activeTab, label = "TabTransition") { tabIndex ->
                when (tabIndex) {
                    0 -> ContactsTab(
                        contacts = MOCK_CONTACTS,
                        onDelegateClick = { showDelegateDialog = it },
                        onDraftClick = {
                            viewModel.createSmartDraft(it, "")
                            quickDraftInstruction = it.suggestedPrompt
                        }
                    )
                    1 -> CallLogsTab(
                        records = records,
                        onRecordClick = { selectedRecordForDetail = it },
                        onDeleteClick = { viewModel.deleteCallRecord(it) },
                        onClearAll = { viewModel.clearAllCallLogs() }
                    )
                }
            }

            // Quick draft generation popup overlay
            draftText?.let { text ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DeepSpaceSurface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Smart Response Draft",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = NeonCyan
                                )
                                IconButton(onClick = { viewModel.clearDraft() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SkyText)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.3f))
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = LightSlate,
                                        lineHeight = 22.sp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("AI Smart Response Draft", text)
                                        clipboard.setPrimaryClip(clip)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepSpaceSurface),
                                    border = ButtonDefaults.outlinedButtonBorder
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Copy")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copy Draft")
                                }

                                Button(
                                    onClick = { viewModel.clearDraft() },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Finish")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Apply & Close")
                                }
                            }
                        }
                    }
                }
            }

            // Custom delegate instructions dialog
            showDelegateDialog?.let { contact ->
                AlertDialog(
                    onDismissRequest = { showDelegateDialog = null },
                    containerColor = DeepSpaceSurface,
                    title = {
                        Text(
                            text = "Delegate Call: ${contact.name}",
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "Instruct Seraphina exactly what goal she should negotiate or discuss on this simulated call.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SkyText
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = delegateInstruction,
                                onValueChange = { delegateInstruction = it },
                                placeholder = { Text(contact.suggestedPrompt, style = TextStyle(color = SkyText.copy(alpha = 0.6f))) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = SkyText,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.startDelegatedCall(contact, delegateInstruction)
                                showDelegateDialog = null
                                delegateInstruction = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        ) {
                            Text("Launch Call")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDelegateDialog = null }) {
                            Text("Cancel", color = SkyText)
                        }
                    }
                )
            }

            // Past Call Record detailed report overlay
            selectedRecordForDetail?.let { record ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
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
                                .padding(20.dp)
                        ) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = record.contactName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${if (record.direction == "DELEGATED") "Delegated AI Call" else "Direct Assistant Dialogue"} • ${record.durationSeconds}s",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonCyan
                                    )
                                }
                                IconButton(onClick = { selectedRecordForDetail = null }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close Detail", tint = SkyText)
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = SkyText.copy(alpha = 0.2f))

                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Info, contentDescription = "Summary Logo", tint = NeonGreen, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("AI Summary & Outcomes", fontWeight = FontWeight.Bold, color = NeonGreen)
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = record.summary,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = LightSlate,
                                                lineHeight = 22.sp
                                            )
                                        }
                                    }
                                }

                                item {
                                    Text(
                                        text = "Conversation Transcript Log",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SkyText,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }

                                val scriptTurns = record.transcript.split("\n").filter { it.isNotBlank() }
                                if (scriptTurns.isEmpty()) {
                                    item {
                                        Text("No logged transcripts available for this record.", color = SkyText, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                    }
                                } else {
                                    items(scriptTurns) { turn ->
                                        val isMe = turn.startsWith("Me:") || turn.lowercase().startsWith("assistant:")
                                        val bubbleColor = if (isMe) DeepSpaceSurface else Color(0xFF1E293B)
                                        val align = if (isMe) Alignment.End else Alignment.Start

                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = align
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(bubbleColor)
                                                    .padding(10.dp)
                                            ) {
                                                Text(
                                                    text = turn,
                                                    style = MaterialTheme.typography.bodySmall.copy(color = LightSlate)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { selectedRecordForDetail = null },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                            ) {
                                Text("Done")
                            }
                        }
                    }
                }
            }

            // Realtime calling and voice simulator overlays (STUNNING UI DESIGN)
            if (isCallActive) {
                ActiveCallOverlay(
                    contact = activeContact ?: MOCK_CONTACTS.first(),
                    callType = callType,
                    liveTranscript = liveTranscript,
                    isLoading = isLoading,
                    onEndCallClick = { viewModel.endCall() },
                    onSpeechSubmitted = {
                        viewModel.sendMessageToActiveCall(it)
                        directChatSpeech = ""
                    }
                )
            }

            // Incoming notification alert rings
            if (isIncomingRinging) {
                IncomingAlertOverlay(
                    contact = activeContact ?: MOCK_CONTACTS.first(),
                    onAccept = { viewModel.acceptIncomingCall() },
                    onDecline = { viewModel.endCall() }
                )
            }
        }
    }
}

// Contacts Grid list UI representation
@Composable
fun ContactsTab(
    contacts: List<Contact>,
    onDelegateClick: (Contact) -> Unit,
    onDraftClick: (Contact) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Available Delegates",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Select a contact to have Seraphina call autonomously on your behalf or write response drafts instantly.",
                style = MaterialTheme.typography.bodySmall,
                color = SkyText,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        items(contacts) { contact ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepSpaceSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(contact.avatarEmoji, fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = contact.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = contact.role,
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonCyan
                        )
                        Text(
                            text = contact.phone,
                            style = MaterialTheme.typography.labelSmall,
                            color = SkyText
                        )
                    }
                }

                Divider(color = SkyText.copy(alpha = 0.1f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.15f))
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onDraftClick(contact) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = "Draft Layout")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Draft SMS", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onDelegateClick(contact) },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "Initiate call", tint = DeepSpaceBackground)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delegate Call", fontSize = 12.sp, color = DeepSpaceBackground)
                    }
                }
            }
        }
    }
}

// Local history logs List representation
@Composable
fun CallLogsTab(
    records: List<CallRecord>,
    onRecordClick: (CallRecord) -> Unit,
    onDeleteClick: (CallRecord) -> Unit,
    onClearAll: () -> Unit
) {
    val dateHelper = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

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
                Text(
                    text = "Transmission Logs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "A complete repository of summarized AI interactions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SkyText
                )
            }

            if (records.isNotEmpty()) {
                Text(
                    text = "Clear All",
                    color = NeonPink,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onClearAll() }
                        .padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(DeepSpaceSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "Empty list symbol", modifier = Modifier.size(36.dp), tint = SkyText)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Static Line - No Transmissions",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Launch a delegated call above to view modern logs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SkyText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 40.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(records) { record ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DeepSpaceSurface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRecordClick(record) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (record.direction == "DELEGATED") NeonCyan.copy(alpha = 0.15f)
                                        else NeonGreen.copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (record.direction == "DELEGATED") Icons.Default.Refresh else Icons.Default.Phone,
                                    contentDescription = "Direction logo",
                                    tint = if (record.direction == "DELEGATED") NeonCyan else NeonGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = record.contactName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "${if (record.direction == "DELEGATED") "Delegate Call" else "Direct dialogue"} • ${record.durationSeconds}s",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SkyText
                                )
                                Text(
                                    text = record.summary.take(80) + "...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LightSlate,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = dateHelper.format(Date(record.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SkyText
                                )

                                IconButton(
                                    onClick = { onDeleteClick(record) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete item",
                                        tint = NeonPink,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Active Call overlay with soundwave pulse visualization!
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
    var spokenBubbleText by remember { mutableStateOf("") }

    // Scroll to the bottom of transcript as it logs
    LaunchedEffect(liveTranscript.size) {
        if (liveTranscript.isNotEmpty()) {
            transcriptState.animateScrollToItem(liveTranscript.size - 1)
        }
    }

    // Beautiful glowing sound wave pulse animation logic
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Signal Core Status
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isLoading) NeonPink else NeonCyan)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isLoading) "PROCESSING AI TRANSACTION..." else "SECURE AUDIO LINE UP",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isLoading) NeonPink else NeonCyan,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pulse audio sphere core
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp)
            ) {
                // Pulse halo wave drawings
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(scalePulse)
                ) {
                    drawCircle(
                        color = if (isLoading) NeonPink.copy(alpha = 0.15f) else NeonCyan.copy(alpha = 0.15f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    DeepSpaceSurface,
                                    Color(0xFF0F172A)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.avatarEmoji,
                        fontSize = 50.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (callType == "DELEGATED") "Delegated Assistant Call" else contact.name,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = Color.White
            )

            Text(
                text = if (callType == "DELEGATED") "Target: Discussing with ${contact.name}" else contact.phone,
                style = MaterialTheme.typography.bodySmall,
                color = SkyText
            )

            // Live Speech-to-Text dynamic card container
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = DeepSpaceSurface.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "LIVE SPEECH TRANSCRIPT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SkyText,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        state = transcriptState,
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(liveTranscript) { turn ->
                            val isMe = turn.startsWith("Me:") || turn.startsWith("Connecting") || turn.startsWith("Call finished")
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + expandVertically()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isMe) NeonCyan.copy(alpha = 0.12f)
                                                else Color.White.copy(alpha = 0.05f)
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = turn,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = if (isMe) NeonCyan else LightSlate,
                                                lineHeight = 18.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "Assistant is thinking...",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = NeonCyan,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            )
                        }
                    }
                }
            }

            // Interactive typing row ONLY for direct dialogue calling with Seraphina
            if (callType == "INCOMING") {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = spokenBubbleText,
                        onValueChange = { spokenBubbleText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Speak / Type to Seraphina...", color = SkyText) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = SkyText,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp)
                    )

                    IconButton(
                        onClick = {
                            if (spokenBubbleText.isNotBlank()) {
                                onSpeechSubmitted(spokenBubbleText)
                                spokenBubbleText = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(NeonCyan)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Submit Speech", tint = DeepSpaceBackground)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Hang up Action
            IconButton(
                onClick = onEndCallClick,
                modifier = Modifier
                    .size(66.dp)
                    .clip(CircleShape)
                    .background(NeonPink)
                    .testTag("hangup_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Hang Up",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

// Full Incoming Screen Alert Rings
@Composable
fun IncomingAlertOverlay(
    contact: Contact,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    // Glowing ring pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "ring_trans")
    val alphaRing by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030712))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Text(
                    text = "SERAPHINA SYS INBOUND CALL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NeonGreen,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    modifier = Modifier.alpha(alphaRing)
                )

                Spacer(modifier = Modifier.height(30.dp))

                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(NeonGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(contact.avatarEmoji, fontSize = 54.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = contact.name,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    color = Color.White
                )

                Text(
                    text = "Direct Assistance Dialogue Ready",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SkyText
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 60.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decline Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onDecline,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(NeonPink)
                            .testTag("decline_call_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Decline Call", tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Decline", color = SkyText, fontSize = 12.sp)
                }

                // Accept Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onAccept,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(NeonGreen)
                            .testTag("accept_call_button")
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "Accept Call", tint = DeepSpaceBackground, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Accept", color = SkyText, fontSize = 12.sp)
                }
            }
        }
    }
}
