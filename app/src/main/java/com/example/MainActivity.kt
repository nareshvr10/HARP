package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.vrntechnology.harpedge.data.repository.AuthRepository
import com.vrntechnology.harpedge.data.repository.HarpRepository
import com.vrntechnology.harpedge.navigation.HarpNavGraph
import com.vrntechnology.harpedge.ui.theme.HarpEdgeTheme
import com.vrntechnology.harpedge.ui.theme.HarpNavyDark

class MainActivity : ComponentActivity() {

    private val authRepository by lazy { AuthRepository() }
    private val harpRepository by lazy { HarpRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HarpEdgeTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = HarpNavyDark
                ) {
                    HarpNavGraph(
                        authRepository = authRepository,
                        harpRepository = harpRepository
                    )
                }
            }
        }
    }
}

