package com.klavs.chatgptprojesi.uix.view


import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.klavs.chatgptprojesi.R
import com.klavs.chatgptprojesi.uix.view.ads.AdmobBanner
import com.klavs.chatgptprojesi.uix.viewmodel.AudioFileResult
import com.klavs.chatgptprojesi.uix.viewmodel.StoryVM
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Story(story: String = "Bu bir deneme konuşma metnidir", navController: NavHostController) {
    val viewModel: StoryVM = hiltViewModel()
    val context = LocalContext.current



    LaunchedEffect(Unit) {
        viewModel.ConvertTextToMp3(story)
    }
    when (val result = viewModel.audioFileResult.value) {
        is AudioFileResult.Error -> {
            Column(Modifier.fillMaxSize(0.9f), verticalArrangement = Arrangement.Center) {
                AlertDialog(
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "error"
                        )
                    }, onDismissRequest = { navController.popBackStack() },
                    confirmButton = {
                        Button(onClick = { viewModel.ConvertTextToMp3(story) }) {
                            Text(text = stringResource(id = R.string.error_confirm_try_again))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { navController.popBackStack() }) {
                            Text(text = stringResource(id = R.string.error_cancel))
                        }
                    },
                    title = { Text(text = stringResource(id = R.string.error)) },
                    text = { Text(text = stringResource(id = R.string.error_text)) })
            }
        }

        AudioFileResult.Loading -> {
            Scaffold {
                LottieAnimationScreen(pv = it, text = stringResource(id = R.string.reading_story))
            }
        }

        is AudioFileResult.Success -> {
            LaunchedEffect(Unit) {
                viewModel.LoadIntersitialAd(context)
            }
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(text = stringResource(id = R.string.listen_to_story)) })
                }) { innerPadding ->
                AudioPlayer(fileUri = result.fileUri!!, pv = innerPadding, text = story)
            }
        }

        AudioFileResult.LanguageError -> RedirectionToPlayStoreAlertDialog {
            navController.navigate("main_page")
        }
    }

}

@Composable
fun RedirectionToPlayStoreAlertDialog(OnDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        title = { Text(text = stringResource(id = R.string.tts_not_found)) },
        text = {
            Text(
                text = stringResource(id = R.string.tts_not_found_text)
            )
        },
        onDismissRequest = OnDismiss,
        confirmButton = {
            Button(onClick = {
                val playStoreIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.tts")
                )
                playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(playStoreIntent)
            }) {
                Text(text = stringResource(id = R.string.download_tts))
            }
        },
        dismissButton = {
            TextButton(onClick = OnDismiss) {
                Text(text = stringResource(id = R.string.back_to_main_page))
            }
        },
        icon = {
            Icon(imageVector = Icons.Filled.Warning, contentDescription = "warning")
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayer(fileUri: Uri, pv: PaddingValues, text: String) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val context = LocalContext.current
    val exoPlayer by remember {
        mutableStateOf(ExoPlayer.Builder(context).build())
    }
    LaunchedEffect(Unit) {
        val mediaItem = MediaItem.fromUri(fileUri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    val isPlaying = remember { mutableStateOf(false) }
    val currentPosition = remember { mutableStateOf(0L) }
    val duration = remember { mutableStateOf(exoPlayer.duration) }
    val composition =
        rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.reading_story_anim))
    LaunchedEffect(Unit) {
        while (true) {
            isPlaying.value = exoPlayer.isPlaying
            currentPosition.value = exoPlayer.currentPosition
            duration.value = exoPlayer.duration
            delay(200)
        }
    }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showDownloadDialog by remember { mutableStateOf(false) }
    Box(
        Modifier
            .fillMaxSize()
            .padding(top = pv.calculateTopPadding())
    ) {
        if (showDownloadDialog) {
            Box(modifier = Modifier.align(Alignment.Center)) {
                DownloadStoryAuido(text) {
                    showDownloadDialog = false
                }
            }

        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                },
                sheetState = sheetState
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(text = text, modifier = Modifier.padding(20.dp))
                }

            }
        }
        Column(
            Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                Modifier
                    .fillMaxWidth(0.95f)
                    .weight(8f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    IconButton(
                        onClick = { showBottomSheet = true },
                        modifier = Modifier.size(screenWidth / 10)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.text_icon),
                            contentDescription = "text",
                            modifier = Modifier.size(screenWidth / 10)
                        )
                    }
                    IconButton(
                        onClick = { showDownloadDialog = true },
                        modifier = Modifier.size(screenWidth / 13)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.download_icon),
                            contentDescription = "text",
                            modifier = Modifier.size(screenWidth / 13)
                        )
                    }
                }
                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LottieAnimation(
                        modifier = Modifier.size(screenHeight / 2.5f),
                        composition = composition.value,
                        iterations = Int.MAX_VALUE
                    )
                    IconButton(onClick = {
                        if (isPlaying.value) {
                            exoPlayer.pause()
                            Log.e("playerstate", "pause")
                        } else {
                            exoPlayer.play()
                            Log.e("playerstate", "play")
                        }
                    }, modifier = Modifier.size(screenHeight / 8)) {
                        if (isPlaying.value) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_pause_24),
                                contentDescription = "pause",
                                modifier = Modifier.size(screenHeight / 10)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "play",
                                modifier = Modifier.size(screenHeight / 8)
                            )
                        }

                    }
                    Box(modifier = Modifier.fillMaxWidth(0.85f)) {
                        Slider(
                            value = currentPosition.value.toFloat(),
                            valueRange = 0f..(if (duration.value < 0L) 1f else duration.value.toFloat()),
                            onValueChange = {
                                exoPlayer.seekTo(it.toLong())
                            })
                    }

                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                //AdmobBanner(modifier = Modifier.fillMaxWidth())
            }
        }


    }


    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
}


@Composable
fun DownloadStoryAuido(storyText: String, DismissRequest: () -> Unit) {
    val viewModel: StoryVM = hiltViewModel()
    var titleValue by remember {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest = DismissRequest,
        confirmButton = {
            Button(onClick = {
                if (titleValue.isNotEmpty()) {
                    viewModel.DownloadStoryAuido(titleValue, text = storyText)
                    DismissRequest()
                }
            }) {
                Text(text = stringResource(id = R.string.download))
            }
        },
        dismissButton = {
            TextButton(onClick = DismissRequest) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.download_icon),
                contentDescription = "download"
            )
        },
        title = { Text(text = stringResource(id = R.string.save_the_story)) },
        text = {
            Column {
                Text(text = stringResource(id = R.string.save_the_story_text))
                TextField(
                    value = titleValue,
                    onValueChange = { titleValue = it },
                    shape = RoundedCornerShape(10.dp),
                    colors =
                    TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    placeholder = { Text(text = stringResource(id = R.string.title)) },
                    modifier = Modifier.padding(5.dp)
                )
            }

        }
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FakeAudioPlayer(fileUri: Uri, pv: PaddingValues, text: String) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp


    val isPlaying = remember { mutableStateOf(false) }
    val currentPosition = remember { mutableStateOf(0L) }
    val composition =
        rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.reading_story_anim))

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showDownloadDialog by remember { mutableStateOf(false) }
    Box(
        Modifier
            .fillMaxSize()
            .padding(top = pv.calculateTopPadding())
    ) {
        if (showDownloadDialog) {
            Box(modifier = Modifier.align(Alignment.Center)) {
                DownloadStoryAuido(text) {
                    showDownloadDialog = false
                }
            }

        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                },
                sheetState = sheetState
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(text = text, modifier = Modifier.padding(20.dp))
                }

            }
        }
        Column(
            Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                Modifier
                    .fillMaxWidth(0.95f)
                    .weight(8f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    IconButton(
                        onClick = { showBottomSheet = true },
                        modifier = Modifier.size(screenHeight / 18)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.text_icon),
                            contentDescription = "text",
                            modifier = Modifier.size(screenHeight / 18)
                        )
                    }
                    IconButton(
                        onClick = { showDownloadDialog = true },
                        modifier = Modifier.size(screenHeight / 21)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.download_icon),
                            contentDescription = "text",
                            modifier = Modifier.size(screenHeight / 21)
                        )
                    }
                }
                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    LottieAnimation(
                        modifier = Modifier.size(screenHeight / 2.5f),
                        composition = composition.value,
                        iterations = Int.MAX_VALUE
                    )
                    IconButton(onClick = {
                        if (isPlaying.value) {
                            Log.e("playerstate", "pause")
                        } else {
                            Log.e("playerstate", "play")
                        }
                    }, modifier = Modifier.size(screenHeight / 8)) {
                        if (isPlaying.value) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_pause_24),
                                contentDescription = "pause",
                                modifier = Modifier.size(screenHeight / 10)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "play",
                                modifier = Modifier.size(screenHeight / 8)
                            )
                        }

                    }
                    Box(modifier = Modifier.fillMaxWidth(0.85f)) {
                        Slider(
                            value = currentPosition.value.toFloat(),
                            valueRange = 0f..3f,
                            onValueChange = {
                            })
                    }

                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Card(modifier = Modifier.align(Alignment.Center)) {
                    Text(text = "adjkkkkkkkkkkkkkkkkkkk")
                }
            }
        }


    }


}

@Preview(showBackground = true)
@Composable
fun GreetingStoryPreview() {
    Column(Modifier.fillMaxSize()) {
        FakeAudioPlayer(fileUri = "".toUri(), pv = PaddingValues(), text = "deneme")
    }

}