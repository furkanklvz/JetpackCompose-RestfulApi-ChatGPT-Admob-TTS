package com.klavs.chatgptprojesi.data.datasource

import android.content.Context
import androidx.compose.ui.res.stringResource
import com.klavs.chatgptprojesi.R
import com.klavs.chatgptprojesi.data.entity.ChatRequest
import com.klavs.chatgptprojesi.data.entity.ChatResponse
import com.klavs.chatgptprojesi.data.entity.Message
import com.klavs.chatgptprojesi.data.retrofit.ChatGPTApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DataSource @Inject constructor(
    val gptApi: ChatGPTApi,
    val context: Context
) {

    suspend fun getCompletion(topicList: List<String>): ChatResponse = withContext(Dispatchers.IO) {
        val prompt = context.getString(R.string.gpt_prompt) + topicList.joinToString(", ")

        val request = ChatRequest(
            messages = listOf(Message("user", prompt))
        )
        return@withContext gptApi.getCompletion(request)
    }
}