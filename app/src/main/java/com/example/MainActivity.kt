package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val darkTheme = remember { mutableStateOf(true) } // Prioritizing Dark Theme for clinical environments
      MyApplicationTheme(darkTheme = darkTheme.value) {
        MainScreen(
          darkTheme = darkTheme.value,
          onToggleTheme = { darkTheme.value = !darkTheme.value }
        )
      }
    }
  }
}
