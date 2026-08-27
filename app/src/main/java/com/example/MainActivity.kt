package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.AppDatabase
import com.example.data.remote.RetrofitClient
import com.example.repository.ProjectRepository
import com.example.ui.DashboardScreen
import com.example.ui.DashboardViewModel
import com.example.ui.DashboardViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val database = AppDatabase.getDatabase(this)
        val repository = ProjectRepository(
            projectDao = database.projectDao(),
            gitHubService = RetrofitClient.instance,
            statusTransitionDao = database.statusTransitionDao()
        )
        val viewModelFactory = DashboardViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[DashboardViewModel::class.java]
        
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DashboardScreen(viewModel)
                }
            }
        }
    }
}
