package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.screens.ScriptScoreApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge layout for modern, full-bleed backgrounds and safe navigation spacing
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                ScriptScoreApp()
            }
        }
    }
}
