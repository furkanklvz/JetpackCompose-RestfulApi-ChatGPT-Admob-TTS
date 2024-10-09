package com.klavs.chatgptprojesi.data.entity

import android.net.Uri
import java.util.Date
import kotlin.time.Duration

data class AudioFile(
    val title: String,
    val fileUri: Uri,
    val size: Long,
    val duration: Duration,
    val lastModified: Date,
    val text:String
    )
