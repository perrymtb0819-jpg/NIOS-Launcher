package com.example.premiumlauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.premiumlauncher.home.HomeScreen
import com.example.premiumlauncher.ui.theme.PremiumLauncherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PremiumLauncherTheme {
                HomeScreen(
                    onAppClick = { appId ->
                        // TODO: Launch the app
                    },
                    onFolderClick = { folderId ->
                        // TODO: Open the folder
                    }
                )
            }
        }
    }
}