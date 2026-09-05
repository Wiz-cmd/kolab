package com.kolab.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.KolabRepository
import com.kolab.data.chat.ChatSocketManager

class ChatViewModelFactory(
    private val repository: KolabRepository,
    private val socketManager: ChatSocketManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(repository, socketManager) as T
    }
}