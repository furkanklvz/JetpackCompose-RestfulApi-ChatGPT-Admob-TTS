package com.klavs.chatgptprojesi.data.retrofit

import com.klavs.chatgptprojesi.data.entity.ChatRequest
import com.klavs.chatgptprojesi.data.entity.ChatResponse
import com.klavs.chatgptprojesi.utils.ApiConstants
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ChatGPTApi {

    @Headers("Content-Type: application/json",
        "Authorization: Bearer " + ApiConstants.OPENAI_API_KEY)
    @POST("v1/chat/completions")
    suspend fun getCompletion(@Body request: ChatRequest): ChatResponse
}