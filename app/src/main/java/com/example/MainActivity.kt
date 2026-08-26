package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.manager.DefaultConnectionManager
import com.example.data.transport.WebSocketTransport
import com.example.ui.MainViewModel
import com.example.ui.screens.DevicesScreen
import com.example.ui.screens.ScreenModeScreen
import com.example.ui.screens.TouchpadScreen
import com.example.ui.theme.MyApplicationTheme
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Manual DI for V1
        val okHttpClient = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS) // WebSockets need no timeout
            .build()
        val transport = WebSocketTransport(okHttpClient)
        val connectionManager = DefaultConnectionManager(transport)
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(connectionManager) as T
            }
        }

        setContent {
            MyApplicationTheme {
                val viewModel: MainViewModel = viewModel(factory = factory)
                val navController = rememberNavController()
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination
                            
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.TouchApp, contentDescription = "Touch") },
                                label = { Text("Touch") },
                                selected = currentDestination?.hierarchy?.any { it.route == "touch" } == true,
                                onClick = {
                                    navController.navigate("touch") {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Computer, contentDescription = "Screen") },
                                label = { Text("Screen") },
                                selected = currentDestination?.hierarchy?.any { it.route == "screen" } == true,
                                onClick = {
                                    navController.navigate("screen") {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Devices, contentDescription = "Devices") },
                                label = { Text("Devices") },
                                selected = currentDestination?.hierarchy?.any { it.route == "devices" } == true,
                                onClick = {
                                    navController.navigate("devices") {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "touch",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("touch") { TouchpadScreen(viewModel) }
                        composable("screen") { ScreenModeScreen(viewModel) }
                        composable("devices") { DevicesScreen(viewModel) }
                    }
                }
            }
        }
    }
}
