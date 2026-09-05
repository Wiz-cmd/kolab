package com.kolab.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.KolabRepository
import com.example.data.security.ChatCrypto
import com.kolab.data.UiMessage
import com.kolab.data.chat.ChatSocketManager
import com.kolab.data.chat.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatState(
    val messages: List<UiMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
    val isConnected: Boolean = false,
    val dealId: String = "",
    val otherUser: com.kolab.data.UiUser? = null,
    val isE2EE: Boolean = false,
    val securityNote: String? = null
)

class ChatViewModel(
    private val repository: KolabRepository,
    private val socketManager: ChatSocketManager
) : ViewModel() {

    private val crypto: ChatCrypto = repository.chatCrypto()

    private var currentPeerPublicKey: String? = null

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    init {
        observeSocketMessages()
        observeConnectionState()
    }

    private fun observeSocketMessages() {
        viewModelScope.launch {
            socketManager.messages.collect { msg ->
                val currentUserId = repository.getCurrentUserId()
                val content = decryptIncoming(msg.content)
                val uiMsg = UiMessage(
                    id = msg.id,
                    content = content,
                    senderId = msg.senderId,
                    createdAt = msg.createdAt
                )
                _state.update { state ->
                    state.copy(messages = state.messages + uiMsg)
                }
            }
        }
    }

    private fun observeConnectionState() {
        viewModelScope.launch {
            socketManager.connectionState.collect { connectionState ->
                _state.update { it.copy(isConnected = connectionState == ConnectionState.CONNECTED) }
            }
        }
    }

    fun loadChat(dealId: String) {
        _state.update { it.copy(dealId = dealId, messages = emptyList(), isLoading = true) }

        socketManager.connect()
        socketManager.joinDeal(dealId)

        viewModelScope.launch {
            val myPublicKey = repository.ensurePublicKeyPublished()
            val peerId = repository.getDealPeerId(dealId)
            val peerPublicKey = peerId?.let { repository.getUserPublicKey(it) }
            currentPeerPublicKey = peerPublicKey

            _state.update {
                it.copy(
                    isE2EE = myPublicKey.isNotBlank() && !peerPublicKey.isNullOrBlank(),
                    securityNote = if (peerPublicKey.isNullOrBlank()) {
                        "End-to-end encryption will start once the other device shares its key"
                    } else {
                        "End-to-end encrypted"
                    }
                )
            }

            repository.getMessages(dealId)
                .onSuccess { messages ->
                    val decrypted = messages.map { msg ->
                        val plain = decryptIncoming(msg.content)
                        if (plain != msg.content) msg.copy(content = plain) else msg
                    }
                    _state.update { it.copy(messages = decrypted, isLoading = false) }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        val dealId = _state.value.dealId
        _state.update { it.copy(isSending = true) }

        val payload = crypto.encrypt(currentPeerPublicKey, content) ?: content
        socketManager.sendMessage(dealId, payload)

        _state.update { it.copy(isSending = false) }
    }

    private fun decryptIncoming(content: String): String =
        if (crypto.isEnvelope(content)) {
            crypto.decrypt(currentPeerPublicKey, content) ?: LOCKED_MESSAGE
        } else {
            content
        }

    fun markRead() {
        val dealId = _state.value.dealId
        if (dealId.isNotBlank()) {
            socketManager.markRead(dealId)
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        socketManager.disconnect()
    }

    companion object {
        const val LOCKED_MESSAGE = "Encrypted message — key unavailable on this device"
    }
}
