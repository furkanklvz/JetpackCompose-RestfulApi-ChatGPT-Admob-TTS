package com.klavs.chatgptprojesi.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AppPref @Inject constructor(
    private val context:Context,
    private val dataStore: DataStore<Preferences>
    ) {

    suspend fun SaveStory(storyName:String, storyText:String){
        dataStore.edit {
            it[stringPreferencesKey(storyName)] = storyText
        }
    }
    suspend fun GetStory(storyName:String):String?{
        val preferences = dataStore.data.first()
        return preferences[stringPreferencesKey(storyName)]
    }

}