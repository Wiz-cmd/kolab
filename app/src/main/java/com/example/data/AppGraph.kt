package com.example.data

import android.content.Context
import com.kolab.data.TokenManager
import com.kolab.data.chat.ChatSocketManager

object AppGraph {
    lateinit var applicationContext: Context
    lateinit var tokenManager: TokenManager
    lateinit var socketManager: ChatSocketManager
    lateinit var repository: KolabRepository
}