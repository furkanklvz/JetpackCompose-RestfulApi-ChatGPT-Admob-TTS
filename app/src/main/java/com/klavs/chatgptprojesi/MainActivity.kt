package com.klavs.chatgptprojesi

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.klavs.chatgptprojesi.ui.theme.ChatGPTProjesiTheme
import com.klavs.chatgptprojesi.uix.view.MainPage
import com.klavs.chatgptprojesi.uix.view.Story
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current

            (context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            ChatGPTProjesiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Navigation(pv = innerPadding)
                }
            }
        }
    }
}

@Composable
private fun Navigation(pv: PaddingValues) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "main_page",
        modifier = Modifier.padding(pv)
    ) {
        composable("main_page") { MainPage(navController = navController) }
        composable(
            "story/{text}",
            arguments = listOf(navArgument("text") { type = NavType.StringType })
        ) {
            Story(story = it.arguments?.getString("text") ?: "Error",navController=navController)
        }
    }
}

