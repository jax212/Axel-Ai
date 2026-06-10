package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.data.db.AppDatabase
import com.example.data.repository.AssistantRepository
import com.example.ui.screens.AssistantDashboard
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AssistantViewModel
import com.example.ui.viewmodel.AssistantViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Core MVVM wiring
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = AssistantRepository(
            database.callRecordDao(),
            database.memoryItemDao(),
            database.automationRoutineDao()
        )
        val viewModel: AssistantViewModel by viewModels {
            AssistantViewModelFactory(repository)
        }

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    AssistantDashboard(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

