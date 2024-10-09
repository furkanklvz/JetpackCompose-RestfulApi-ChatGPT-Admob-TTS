package com.klavs.chatgptprojesi.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.klavs.chatgptprojesi.data.datasource.DataSource
import com.klavs.chatgptprojesi.data.repos.gptRepostory
import com.klavs.chatgptprojesi.data.retrofit.ApiUtils
import com.klavs.chatgptprojesi.data.retrofit.ChatGPTApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideGptRepository(ds: DataSource): gptRepostory {
        return gptRepostory(ds)
    }

    @Provides
    @Singleton
    fun provideDataSource(gptApi: ChatGPTApi, context: Context): DataSource {
        return DataSource(gptApi, context = context)
    }

    @Provides
    @Singleton
    fun provideChatGPTApi(): ChatGPTApi {
        return ApiUtils.GetChatGPTApi()
    }
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    val Context.dataStoreFile: DataStore<Preferences> by preferencesDataStore(name = "stories")
    @Provides
    @Singleton
    fun provideDataStore(context: Context): DataStore<Preferences> {
        return context.dataStoreFile
    }
}

