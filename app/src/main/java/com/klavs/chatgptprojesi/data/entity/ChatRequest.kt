package com.klavs.chatgptprojesi.data.entity
//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
data class ChatRequest(
    val model: String = "gpt-3.5-turbo-1106",
    val messages: List<Message>
)
