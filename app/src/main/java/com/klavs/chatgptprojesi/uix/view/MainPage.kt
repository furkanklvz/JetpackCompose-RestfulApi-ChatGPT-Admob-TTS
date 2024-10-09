package com.klavs.chatgptprojesi.uix.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.klavs.chatgptprojesi.R
import com.klavs.chatgptprojesi.data.entity.AudioFile
import com.klavs.chatgptprojesi.ui.theme.ChatGPTProjesiTheme
import com.klavs.chatgptprojesi.ui.theme.secondaryContainer
import com.klavs.chatgptprojesi.uix.view.ads.AdmobBanner
import com.klavs.chatgptprojesi.uix.viewmodel.BookmarksResult
import com.klavs.chatgptprojesi.uix.viewmodel.MainPageVM
import com.klavs.chatgptprojesi.uix.viewmodel.Result
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(navController: NavHostController) {

    val viewModel: MainPageVM = hiltViewModel()
    DisposableEffect(Unit) {
        onDispose {
            viewModel.ResetViewModel()
        }
    }
    when (val response = viewModel.chatResponse.value) {
        is Result.Error -> Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Log.e("error", response.message ?: "")
            AlertDialog(
                icon = { Icon(imageVector = Icons.Filled.Warning, contentDescription = "") },
                onDismissRequest = { viewModel.ResetViewModel() },
                confirmButton = {
                    Button(onClick = { viewModel.SendTopicList(topicList = response.topicList!!) }) {
                        Text(text = stringResource(id = R.string.error_confirm_try_again))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.ResetViewModel() }) {
                        Text(text = stringResource(id = R.string.error_cancel))
                    }
                },
                title = { Text(text = stringResource(id = R.string.error)) },
                text = {
                    Text(text = stringResource(id = R.string.internet_error_text))
                })
        }

        Result.Idle -> {
            val showBookmarks = remember { mutableStateOf(false) }
            val sheetState = rememberModalBottomSheetState()
            val showInfoDialog = remember { mutableStateOf(false) }
            Scaffold(topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            showBookmarks.value = true
                            viewModel.GetSavedStories()
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.bookmark),
                                contentDescription = "bookmark"
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { showInfoDialog.value = true },
                            modifier = Modifier
                                .zIndex(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info, contentDescription = ""
                            )
                        }
                    }
                )
            }) { paddingValues ->

                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding())
                ) {
                    if (showInfoDialog.value) {
                        InfoDialog {
                            showInfoDialog.value = false
                        }
                    }

                    if (showBookmarks.value) {
                        BookmarksBottomSheet(
                            state = sheetState,
                            navController = navController
                        ) { showBookmarks.value = false }
                    }

                    Content(navController = navController)
                }
            }


        }

        Result.Loading -> {
            Scaffold {
                LottieAnimationScreen(it, stringResource(id = R.string.writing_story))
            }

        }

        is Result.Success -> {
            LaunchedEffect(Unit) {
                navController.navigate("story/${response.data}")
            }

        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksBottomSheet(
    state: SheetState,
    navController: NavHostController,
    onDismiss: () -> Unit
) {
    val viewModel: MainPageVM = hiltViewModel()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
        modifier = Modifier
            .height(screenHeight * 0.8f)
    ) {
        when (val result = viewModel.bookmarksResponse.value) {
            is BookmarksResult.Error -> {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = stringResource(id = R.string.error_message) + result.message)
                }
            }

            BookmarksResult.Loading ->
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }

            BookmarksResult.Success -> {
                LazyColumn {
                    items(viewModel.savedAudioFiles.toList()) {
                        BookmarkListItem(item = it, navController = navController)
                    }
                }
            }
        }

    }
}

@Composable
fun BookmarkListItem(item: AudioFile, navController: NavHostController) {
    val title = item.title.split(".")[0].replace("_", " ").replaceFirstChar { it.uppercase() }
    val viewModel: MainPageVM = hiltViewModel()
    val activity = LocalContext.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 6.dp)
            .clickable {
                navController.navigate("story/${item.text}")
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.audio_icon),
                contentDescription = "audio",
                modifier = Modifier.padding(7.dp)
            )
            Column {
                Text(text = title, fontWeight = FontWeight.Bold)
                val dateFormatter = SimpleDateFormat("dd MMM y, HH.mm", Locale("tr", "TR"))
                Text(text = dateFormatter.format(item.lastModified))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = item.duration.toString())

            Icon(imageVector = Icons.Filled.KeyboardArrowRight, contentDescription = "")
        }

    }
}


@Composable
fun LottieAnimationScreen(pv: PaddingValues, text: String) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.sp
    val composition =
        rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.story_loading))
    Column(
        Modifier
            .fillMaxSize()
            .padding(pv),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            AdmobBanner(modifier = Modifier.fillMaxWidth())
        }
        Box(
            Modifier
                .fillMaxWidth()
                .weight(8f)
        ) {
            Column(verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)) {
                Box(modifier = Modifier.fillMaxHeight(0.5f)){
                    LottieAnimation(
                        composition = composition.value,
                        iterations = Int.MAX_VALUE
                    )
                }

                Text(
                    text = text,
                    fontSize = screenWidth / 19
                )
            }

        }
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            AdmobBanner(modifier = Modifier.fillMaxWidth())
        }
    }

}

@Composable
fun InfoDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                val intent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
                context.startActivity(intent)
            }) {
                Text(text = stringResource(id = R.string.chech_it_now))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.got_it))
            }
        },
        title = { Text(text = stringResource(id = R.string.warning)) },
        icon = { Icon(imageVector = Icons.Filled.Info, contentDescription = "info") },
        text = { Text(text = stringResource(id = R.string.tts_info_text)) })
}

@Composable
private fun Content(navController: NavHostController) {
    Column(
        Modifier
            .fillMaxSize()
            .zIndex(0f),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val topicList = arrayListOf(
            "friendship",
            "family",
            "animals",
            "cars",
            "love",
            "horror",
            "dragon",
            "princess",
            "prince",
            "witch",
            "zombie",
            "alien",
            "football",
            "basketball",
            "volleyball",
            "video_games"
        )
        val selectedTopics = remember {
            mutableStateListOf<String>()
        }
        Column(verticalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.weight(8f)) {
            TopicGrid(topicList = topicList,
                listSize = selectedTopics.size,
                addTopic = { selectedTopics.add(it) },
                removeTopic = { selectedTopics.remove(it) })


            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CreateButton(selectedTopics, navController = navController)
            }
        }
        /*AdmobBanner(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )*/
    }
}

@Composable
private fun CreateButton(selectedTopics: List<String>, navController: NavHostController) {
    val viewModel: MainPageVM = hiltViewModel()
    val context = LocalContext.current
    val toast_message = stringResource(id = R.string.at_least_one_topic)
    Button(
        onClick = {
            if (selectedTopics.size == 0) {
                Toast.makeText(context, toast_message, Toast.LENGTH_SHORT).show()
                return@Button
            } else {
                viewModel.SendTopicList(selectedTopics)
            }
        }
    ) {
        Text(
            text = stringResource(id = R.string.generate_story),
            fontSize = 26.sp,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold
        )
    }

    /*TextButton(onClick = {
        navController.navigate("story/${"Bu bir deneme konuşma metnidir.bir, iki, üç, dört, beş, altı, yedi, sekiz, dokuz, on"}")
    }) {
        Text(text = "test sound")
    }*/
}

@Composable
private fun TopicGrid(
    topicList: List<String>,
    listSize: Int,
    addTopic: (String) -> Unit,
    removeTopic: (String) -> Unit
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    Column(
        Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.selecting_topic_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.padding(12.dp))
        Box {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .height(screenHeight / 2)
            ) {
                items(topicList) { topic ->
                    TopicCard(
                        value = topic,
                        Add = { addTopic(it) },
                        listSize = listSize,
                        Remove = { removeTopic(it) })
                }

            }
            HorizontalDivider(
                thickness = 4.dp,
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .align(Alignment.BottomCenter)
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(10.dp))
            )
        }

    }

}

@SuppressLint("DiscouragedApi")
@Composable
private fun TopicCard(
    value: String,
    Add: (String) -> Unit,
    listSize: Int,
    Remove: (String) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val selected = rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val resourceId = context.resources.getIdentifier(value, "drawable", context.packageName)
    val dynamicString = context.getString(
        context.resources.getIdentifier(value, "string", context.packageName)
    )
    val toast_message = stringResource(id = R.string.at_most_three_topics)

    Card(colors = CardDefaults.cardColors(
        containerColor = if (selected.value) {
            secondaryContainer
        } else {
            CardDefaults.cardColors().containerColor
        }
    ), modifier = Modifier
        .height(screenHeight / 11)
        .padding(if (selected.value) 6.dp else 2.dp)
        .clickable {
            if (selected.value) {
                selected.value = false
                Remove(dynamicString)
            } else {
                if (listSize != 3) {
                    selected.value = true
                    Add(dynamicString)
                } else {
                    Toast
                        .makeText(context, toast_message, Toast.LENGTH_SHORT)
                        .show()
                }

            }
        }

    ) {
        Box {
            Row(
                Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Image(
                    painter = painterResource(id = resourceId),
                    contentDescription = value,
                    modifier = Modifier.size(60.dp)
                )
                Text(text = dynamicString)
            }
        }

    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ChatGPTProjesiTheme {
        LottieAnimationScreen(pv = PaddingValues(), text = "loading")
    }
}