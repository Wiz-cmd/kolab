package com.kolab.data.chat

import android.util.Log
import com.kolab.data.TokenManager
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject
import java.net.URI

class ChatSocketManager(private val tokenManager: TokenManager) {

    private var socket: Socket? = null
    private val _messages = MutableSharedFlow<ChatMessage>(extraBufferCapacity = 64)
    val messages: SharedFlow<ChatMessage> = _messages.asSharedFlow()

    private val _readReceipts = MutableSharedFlow<ReadReceipt>(extraBufferCapacity = 16)
    val readReceipts: SharedFlow<ReadReceipt> = _readReceipts.asSharedFlow()

    private val _connectionState = MutableSharedFlow<ConnectionState>(extraBufferCapacity = 4)
    val connectionState: SharedFlow<ConnectionState> = _connectionState.asSharedFlow()

    private var currentDealId: String? = null

    fun connect() {
        if (socket?.connected() == true) return

        val baseUrl = com.example.BuildConfig.BASE_URL.trimEnd('/')
            .replace("/api", "")
            .replace("http://", "http://")
            .replace("https://", "https://")

        val uri = URI.create(baseUrl)
        val options = IO.Options.builder()
            .setAuth(mapOf("token" to (tokenManager.getAccessToken() ?: "")))
            .setExtraHeaders(mapOf("Authorization" to listOf("Bearer ${tokenManager.getAccessToken() ?: ""}")))
            .build()

        socket = IO.socket(uri, options)

        socket?.on(Socket.EVENT_CONNECT) {
            Log.d(TAG, "Socket connected")
            _connectionState.tryEmit(ConnectionState.CONNECTED)
        }

        socket?.on(Socket.EVENT_DISCONNECT) { args ->
            Log.d(TAG, "Socket disconnected: ${args.firstOrNull()}")
            _connectionState.tryEmit(ConnectionState.DISCONNECTED)
        }

        socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
            Log.e(TAG, "Socket connect error: ${args.firstOrNull()}")
            _connectionState.tryEmit(ConnectionState.ERROR)
        }

        socket?.on("newMessage") { args ->
            val data = args.firstOrNull() as? JSONObject ?: return@on
            val message = ChatMessage(
                id = data.optString("id"),
                content = data.optString("content"),
                senderId = data.optString("senderId"),
                dealId = data.optString("dealId"),
                createdAt = data.optString("createdAt")
            )
            _messages.tryEmit(message)
        }

        socket?.on("readReceipt") { args ->
            val data = args.firstOrNull() as? JSONObject ?: return@on
            val receipt = ReadReceipt(
                dealId = data.optString("dealId"),
                readerId = data.optString("readerId")
            )
            _readReceipts.tryEmit(receipt)
        }

        socket?.on("joined") { args ->
            Log.d(TAG, "Joined deal room: ${args.firstOrNull()}")
        }

        socket?.connect()
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
        currentDealId = null
    }

    fun joinDeal(dealId: String) {
        currentDealId = dealId
        val data = JSONObject().put("dealId", dealId)
        socket?.emit("joinDeal", data)
    }

    fun sendMessage(dealId: String, content: String) {
        val data = JSONObject().apply {
            put("dealId", dealId)
            put("content", content)
        }
        socket?.emit("sendMessage", data)
    }

    fun markRead(dealId: String) {
        val data = JSONObject().put("dealId", dealId)
        socket?.emit("markRead", data)
    }

    fun isConnected(): Boolean = socket?.connected() == true

    companion object {
        private const val TAG = "ChatSocket"
    }
}

data class ChatMessage(
    val id: String,
    val content: String,
    val senderId: String,
    val dealId: String,
    val createdAt: String
)

data class ReadReceipt(
    val dealId: String,
    val readerId: String
)

enum class ConnectionState {
    CONNECTED, DISCONNECTED, ERROR
}
