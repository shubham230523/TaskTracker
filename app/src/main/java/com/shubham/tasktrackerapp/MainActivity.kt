package com.shubham.tasktrackerapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.widget.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.shubham.tasktrackerapp.dashboard.DashBoard
import com.shubham.tasktrackerapp.newtask.NewTask
import com.shubham.tasktrackerapp.theme.TaskTrackerTheme
import com.shubham.tasktrackerapp.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "taskTag"
    }

    @OptIn(ExperimentalAnimationApi::class)
    @SuppressLint("ObjectAnimatorBinding")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rootView = findViewById<ComposeView>(R.id.activity_main_compose_view)
        rootView.apply {
            setContent {
                TaskTrackerTheme {

                    LaunchedEffect(key1 = Unit, block = {
                        createNotificationChannel()
                    })

                    AskForNotificationPermission()

                    val navController = rememberAnimatedNavController()
                    val items = listOf(
                        BottomNavItems.Home,
                        BottomNavItems.NewTask,
                        BottomNavItems.DashBoard
                    )

                    Scaffold(
                        content = { padding ->
                            Column(
                                modifier = Modifier.padding(padding)
                            ) {
                                AnimatedNavHost(
                                    navController = navController,
                                    startDestination = Screen.Home.route,
                                    enterTransition = {
                                        slideInHorizontally(initialOffsetX = { 500 }) + fadeIn()
                                    },
                                    exitTransition = {
                                        slideOutHorizontally(targetOffsetX = { -500 }) + fadeOut()
                                    }
                                ) {
                                    composable(
                                        route = Screen.Home.route,
                                        arguments = listOf()
                                    ) {
                                        HomeScreen(navController)
                                    }
                                    composable(
                                        route = Screen.NewTask.route,
                                        enterTransition = {
                                            slideInVertically(
                                                initialOffsetY = { 1000 },
                                                animationSpec = tween(durationMillis = 1000)
                                            ) + fadeIn()
                                        },
                                        exitTransition = {
                                            slideOutVertically(
                                                targetOffsetY = { 1000 },
                                                animationSpec = tween(durationMillis = 1000)
                                            ) + fadeOut()
                                        }
                                    ) {
                                        NewTask(navController)
                                    }
                                    composable(route = Screen.DashBoard.route) {
                                        DashBoard()
                                    }
                                    composable(route = Screen.EditTask.route.plus("/{id}")) { navBackStackEntry ->
                                        val taskId = navBackStackEntry.arguments?.getString("id")
                                        taskId?.let {
                                            EditTaskScreen(taskId, navController)
                                        }
                                    }
                                }
                            }
                        },
                        bottomBar = {
                            BottomNavigation(
                                backgroundColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .height(40.dp)
                                    .fillMaxWidth()
                            ) {
                                val navBackStackEntry by navController.currentBackStackEntryAsState()
                                val currentRoute = navBackStackEntry?.destination?.route
                                items.forEach { item ->
                                    BottomNavigationItem(
                                        icon = {
                                            Icon(
                                                painter = painterResource(id = item.icon),
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        },
                                        selectedContentColor = MaterialTheme.colorScheme.primary,
                                        unselectedContentColor = MaterialTheme.colorScheme.primaryContainer,
                                        selected = currentRoute == item.route,
                                        onClick = {
                                            navController.navigate(
                                                item.route,
                                            ) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                // Avoid multiple copies of the same destination when
                                                // reselecting the same item
                                                launchSingleTop = true
                                                // Restore state when reselecting a previously selected item
                                                restoreState = true
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val channelId = getString(R.string.CHANNEL_ID)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun AskForNotificationPermission() {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        if (!it) {
            Toast.makeText(
                context,
                "Notification permission is required for notifying user, you can enable this in settings",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // for android version 13 or above we have to ask for notification permission
    if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permission = Manifest.permission.POST_NOTIFICATIONS
        val permissionCheckResult = ContextCompat.checkSelfPermission(
            LocalContext.current,
            Manifest.permission.POST_NOTIFICATIONS
        )
        if (permissionCheckResult != PackageManager.PERMISSION_GRANTED) {
            LaunchedEffect(key1 = Unit, block = {
                permissionLauncher.launch(permission)
            })
        }
    }
}