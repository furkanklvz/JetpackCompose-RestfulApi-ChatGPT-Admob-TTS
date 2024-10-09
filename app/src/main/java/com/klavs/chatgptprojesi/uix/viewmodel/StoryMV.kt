package com.klavs.chatgptprojesi.uix.viewmodel


import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.getMaxSpeechInputLength
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.klavs.chatgptprojesi.R
import com.klavs.chatgptprojesi.data.datastore.AppPref
import com.klavs.chatgptprojesi.utils.ApiConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import javax.inject.Inject

sealed class AudioFileResult(val fileUri: Uri? = null, val message: String? = null) {
    class Success(data: Uri) : AudioFileResult(fileUri = data)
    class Error(message: String) : AudioFileResult(message = message)
    object Loading : AudioFileResult()
    object LanguageError : AudioFileResult()
}

@HiltViewModel
class StoryVM @Inject constructor(
    private val application: Application,
    private val ap: AppPref
) : AndroidViewModel(application) {

    val audioFileResult = mutableStateOf<AudioFileResult>(AudioFileResult.Loading)

    private lateinit var tts: TextToSpeech

    private var interstitialAd : InterstitialAd?=null
    private var adLoaded = mutableStateOf(false)

    fun LoadIntersitialAd(context: Context){
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, ApiConstants.INTERSTITIAL_AD_ID,adRequest,
            object : InterstitialAdLoadCallback(){
                override fun onAdLoaded(ad: InterstitialAd) {
                    super.onAdLoaded(ad)
                    interstitialAd = ad
                    adLoaded.value = true
                    ShowInterstitialAd(context as Activity)
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


    fun ConvertTextToMp3(text: String) {
        audioFileResult.value = AudioFileResult.Loading
        tts = TextToSpeech(application.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                lateinit var locale: Locale
                if (application.getString(R.string.language) =="tr"){
                    locale = Locale("tr", "TR")
                }else{
                    locale = Locale("en","US")
                }

                val result = tts.setLanguage(locale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    audioFileResult.value = AudioFileResult.LanguageError
                } else {
                    viewModelScope.launch(Dispatchers.IO) {
                        val outputFile = File(application.cacheDir, "output.mp3")

                        try {
                            Log.e("result", "metin uzunluğu: ${text.length}")
                            if (getMaxSpeechInputLength() < text.length) {
                                audioFileResult.value =
                                    AudioFileResult.Error("max char length error: " + getMaxSpeechInputLength())
                                return@launch
                            }
                            tts.setOnUtteranceProgressListener(object :
                                UtteranceProgressListener() {
                                override fun onStart(p0: String?) {

                                }

                                override fun onDone(p0: String?) {
                                    audioFileResult.value =
                                        AudioFileResult.Success(Uri.fromFile(outputFile))
                                    Log.e(
                                        "outputFile",
                                        "Ses dosyası oluşturuldu: ${outputFile.length()}"
                                    )

                                }

                                @Deprecated("Deprecated in Java")
                                override fun onError(p0: String?) {
                                    audioFileResult.value =
                                        AudioFileResult.Error("UtteranceProgressListener $p0" ?: "unknown error")
                                }

                            })

                            tts.synthesizeToFile(
                                text, null, outputFile, "tts_output"
                            )

                        } catch (e: Exception) {
                            audioFileResult.value = AudioFileResult.Error(e.localizedMessage?:"unknown error")
                        }

                    }
                }
            } else {
                audioFileResult.value = AudioFileResult.Error(status.toString())
            }
        }
    }

    fun DownloadStoryAuido( title: String, text: String) {
        val fileName = title.trim().lowercase().replace(" ", "_")

        viewModelScope.launch(Dispatchers.Main) {
            try {
                ap.SaveStory(storyName = fileName, storyText = text)
                Log.e("story", "story saved")
            }catch (e:Exception){
                Toast.makeText(application, "DataStore error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                Log.e("datastore error",e.localizedMessage?:"")
            }

        }

        val cacheFile = File(application.cacheDir, "output.mp3")
        val audioDir = File(application.filesDir, "audioFiles")
        if (!audioDir.exists()){
            audioDir.mkdir()
        }
        val targetFile = File(audioDir, "$fileName.mp3")
        try {
            if (cacheFile.exists() && cacheFile.length() > 0) {
                cacheFile.copyTo(targetFile, overwrite = true)
                Toast.makeText(application, application.getString(R.string.downloaded_successfully) +fileName, Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(application, "File not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                application,
                e.localizedMessage,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts.shutdown()
    }
}
