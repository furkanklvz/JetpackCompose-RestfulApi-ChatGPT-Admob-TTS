package com.klavs.chatgptprojesi.data.repos

import android.content.Context
import com.klavs.chatgptprojesi.data.datasource.DataSource
import com.klavs.chatgptprojesi.data.entity.ChatRequest

class gptRepostory(val ds: DataSource) {

    suspend fun SendRequest(topicList: List<String>) = ds.getCompletion(topicList)


}