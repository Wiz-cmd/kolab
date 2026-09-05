package com.kolab.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.kolab.data.UiMessage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    dealId: String,
    chatViewModel: ChatViewModel,
    onNavigateBack: () -> Unit,
    currentUserId: String
) {
    val state by chatViewModel.state.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(dealId) {
        chatViewModel.loadChat(dealId)
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Chat")
                        if (!state.isConnected) {
                            Text(
                                text = "Connecting...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else if (state.securityNote != null) {
                            Text(
                                text = state.securityNote,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (state.isE2EE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(state.messages) { message ->
                        MessageBubble(
                            message = message,
                            isOwn = message.senderId == currentUserId
                        )
                    }
                }

                if (!state.isConnected) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.errorContainer,
                        tonalElevation = 2.dp
                    ) {
                        Text(
                            text = "Reconnecting...",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Type a message...") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (inputText.isNotBlank()) {
                                        chatViewModel.sendMessage(inputText)
                                        inputText = ""
                                    }
                                }
                            ),
                            maxLines = 3,
                            shape = RoundedCornerShape(24.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    chatViewModel.sendMessage(inputText)
                                    inputText = ""
                                }
                            },
                            enabled = inputText.isNotBlank() && state.isConnected
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: UiMessage, isOwn: Boolean) {
    val alignment = if (isOwn) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isOwn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isOwn) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        contentAlignment = alignment
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isOwn) 16.dp else 4.dp,
                bottomEnd = if (isOwn) 4.dp else 16.dp
            ),
            color = bubbleColor
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = message.content, color = textColor, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTime(message.createdAt),
                    color = textColor.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

private fun formatTime(isoString: String): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        val output = SimpleDateFormat("HH:mm", Locale.US)
        val date = input.parse(isoString)
        if (date != null) output.format(date) else ""
    } catch (e: Exception) { "" }
}
