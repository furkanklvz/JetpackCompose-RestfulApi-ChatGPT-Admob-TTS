package com.klavs.chatgptprojesi.uix.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.klavs.chatgptprojesi.R
import com.klavs.chatgptprojesi.data.datastore.AppPref
import com.klavs.chatgptprojesi.data.entity.AudioFile
import com.klavs.chatgptprojesi.data.entity.Choice
import com.klavs.chatgptprojesi.data.repos.gptRepostory
import com.klavs.chatgptprojesi.utils.ApiConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date
import javax.inject.Inject
import kotlin.time.DurationUnit
import kotlin.time.toDuration

sealed class Result(val data: String? = null, val message: String? = null, val topicList: List<String>? = null) {
    class Success(data: String) : Result(data)
    class Error(message: String,topicList:List<String>) : Result(message = message, topicList = topicList)
    object Loading : Result()
    object Idle : Result()
}
sealed class BookmarksResult(val message: String? = null) {
    object Success : BookmarksResult()
    class Error(message: String) : BookmarksResult(message)
    object Loading : BookmarksResult()
}


@HiltViewModel
class MainPageVM @Inject constructor(
    var gptRepostory: gptRepostory,
    private val application: Application,
    private val ap: AppPref
) : AndroidViewModel(application) {

    private var interstitialAd : InterstitialAd?=null
    private var adLoaded = mutableStateOf(false)

    val chatResponse: MutableState<Result> = mutableStateOf(Result.Idle)
    val bookmarksResponse: MutableState<BookmarksResult> = mutableStateOf(BookmarksResult.Loading)


    val savedAudioFiles = mutableStateListOf<AudioFile>()

    fun LoadIntersitialAd(context: Context){
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context,ApiConstants.INTERSTITIAL_AD_ID,adRequest,
            object : InterstitialAdLoadCallback(){
                override fun onAdLoaded(ad: InterstitialAd) {
                    super.onAdLoaded(ad)
                    interstitialAd = ad
                    adLoaded.value = true
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    interstitialAd = null
                    adLoaded.value = false
                }
            })
    }
    fun ShowInterstitialAd(activity: Activity){
        interstitialAd?.show(activity)
    }




    fun ResetViewModel(){
        chatResponse.value = Result.Idle
    }

    fun SendTopicList(topicList: List<String>) {
        chatResponse.value = Result.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = gptRepostory.SendRequest(topicList)
                response.choices.firstOrNull()?.let { choice: Choice ->

                    withContext(Dispatchers.Main) {
                        chatResponse.value = Result.Success(data = choice.message.content)
                        Log.e("Masal", choice.message.content)
                    }
                }
            } catch (e: Exception) {

                withContext(Dispatchers.Main) {
                    chatResponse.value = Result.Error(
                        message = e.localizedMessage ?: application.getString(R.string.error_message),
                        topicList = topicList
                    )
                }
            }
        }
    }

    fun GetSavedStories() {
        bookmarksResponse.value = BookmarksResult.Loading
        savedAudioFiles.clear()
        val audioDir = File(application.filesDir, "audioFiles")
        if (audioDir.exists() && audioDir.isDirectory) {
            val files = audioDir.listFiles()
            try {
                files?.forEach { file ->
                    val fileName = file.name
                    val fileSize = file.length()
                    val fileUri = Uri.fromFile(file)
                    val lastModified = Date(file.lastModified())
                    val duration = GetAudioFileDuration(file).toDuration(DurationUnit.MILLISECONDS)
                    GetStory(fileName.split(".")[0]){storyText->
                        val text = storyText
                        val audioFile = AudioFile(
                            title = fileName,
                            fileUri = fileUri,
                            size = fileSize,
                            duration = duration,
                            lastModified = lastModified,
                            text = text
                        )
                        savedAudioFiles.add(audioFile)
                    }


                }
                bookmarksResponse.value = BookmarksResult.Success
            }catch (e:Exception){
                bookmarksResponse.value = BookmarksResult.Error(message = e.localizedMessage ?: application.getString(R.string.error_message))
            }

        }else{
            bookmarksResponse.value = BookmarksResult.Success
        }
    }

    fun GetAudioFileDuration(file: File): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            val durationStr =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = durationStr?.toLong() ?: 0L
            duration
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        } finally {
            retriever.release()
        }
    }

    fun GetStory(storyName: String, SendText:(String)->Unit) {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                SendText(ap.GetStory(storyName)?:"")
            }catch (e:Exception){
                Toast.makeText(application,e.localizedMessage,Toast.LENGTH_SHORT).show()
                Log.e("datastore error",e.localizedMessage?:"")
            }
        }
    }

}