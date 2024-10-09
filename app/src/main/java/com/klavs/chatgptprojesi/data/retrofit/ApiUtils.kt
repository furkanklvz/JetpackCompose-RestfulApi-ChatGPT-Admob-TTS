package com.klavs.chatgptprojesi.data.retrofit

class ApiUtils {
    companion object{
        val BASE_URL = "https://api.openai.com/"

        fun GetChatGPTApi():ChatGPTApi{
            return RetrofitClient.GetClient(BASE_URL).create(ChatGPTApi::class.java)
        }
    }
}