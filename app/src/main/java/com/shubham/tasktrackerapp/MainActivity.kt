
package com.shubham.tasktrackerapp

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.navigation
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.shubham.tasktrackerapp.dashboard.DashboardFragment
import com.shubham.tasktrackerapp.data.local.Task
import com.shubham.tasktrackerapp.data.local.TaskDao
import com.shubham.tasktrackerapp.data.local.TaskDatabase
import com.shubham.tasktrackerapp.newtask.NewTask
import com.shubham.tasktrackerapp.newtask.NewTaskFragment
import com.shubham.tasktrackerapp.theme.TaskTrackerTheme
import com.shubham.tasktrackerapp.util.BottomNavItems
import com.shubham.tasktrackerapp.util.Screen
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
                    val navController = rememberAnimatedNavController()
                    val items = listOf(
                        BottomNavItems.Home,
                        BottomNavItems.NewTask,
                        BottomNavItems.DashBoard
                    )
                    val state = remember {
                        mutableStateOf(false)
                    }
                    Scaffold(
                        drawerContent = { Text(text = "drawerContent") },
                        content = {padding ->
                            Column(
                                modifier = Modifier.padding(padding)
                            ) {
                                AnimatedNavHost(
                                    navController = navController,
                                    startDestination = Screen.Home.route,
                                    enterTransition = {
                                        if(initialState.destination.hierarchy.any{
                                                Log.d("initialState" , "${it.route}")
                                                it.route == Screen.NewTask.route
                                        }){
                                            slideInVertically(
                                                initialOffsetY = {1000},
                                                animationSpec = tween(
                                                    durationMillis = 2000,
                                                    easing = LinearEasing
                                                )
                                            )
                                        }
                                        else {
                                            slideInHorizontally(
                                                initialOffsetX = {1000},
                                                animationSpec = tween(
                                                    durationMillis = 2000,
                                                    easing = LinearEasing
                                                )
                                            )
                                        }
                                    },
                                    exitTransition = {
                                        if(targetState.destination.hierarchy.any{
                                                Log.d("targetState" , "${it.route}")
                                                it.route == Screen.NewTask.route
                                        }){
                                            ExitTransition.None
                                        }
                                        else {
                                            slideOutHorizontally(
                                                targetOffsetX = {1000},
                                                animationSpec = tween(
                                                    durationMillis = 2000,
                                                    easing = LinearEasing
                                                )
                                            )
                                        }
                                    },
                                ){
                                    composable(
                                        route = Screen.Home.route,
                                    ){
                                        HomeScreen(navController)
                                    }
                                    composable(
                                        route = Screen.NewTask.route,
//                                        enterTransition ={
//                                            slideInVertically(
//                                                initialOffsetY = {3000},
//                                                animationSpec = tween(
//                                                    delayMillis = 2000,
//                                                    easing = LinearEasing
//                                                )
//                                            )
//                                        },
//                                        exitTransition = {
//                                            slideOutVertically(
//                                                targetOffsetY = {3000}
//                                            )
//                                        }
                                    ){
                                        NewTask(visible = state)
                                    }
                                    composable(route = Screen.DashBoard.route){

                                    }
                                }
                            }

                        },
                        bottomBar = {
                            BottomNavigation(
                                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .height(40.dp)
                                    .fillMaxWidth()
                            ) {
                                val navBackStackEntry by navController.currentBackStackEntryAsState()
                                val currentRoute = navBackStackEntry?.destination?.route
                                items.forEach {item ->
                                    BottomNavigationItem(
                                        icon = {
                                            Icon(
                                                painter = painterResource(id = item.icon) ,
                                                contentDescription = null
                                            )
                                        },
                                        selectedContentColor = MaterialTheme.colorScheme.primary,
                                        unselectedContentColor = MaterialTheme.colorScheme.inversePrimary,
                                        selected = currentRoute == item.route,
                                        onClick = {
                                            navController.navigate(
                                                item.route,
                                            ){
                                                navController.graph.startDestinationRoute?.let{
                                                    popUpTo(item.route){
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
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
//        bottomNavBar = findViewById(R.id.bottom_nav)
//        fragmentContainer = findViewById(R.id.fragment_container)
//
//        supportFragmentManager.beginTransaction()
//            .add(R.id.fragment_container, HomeFragment(), "Home")
//            .addToBackStack(null)
//            .commit()
//
//        bottomNavBar.setOnItemSelectedListener {
//            when (it.itemId) {
//                R.id.nav_home -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.fragment_container, HomeFragment())
//                        .commit()
//                    return@setOnItemSelectedListener true
//                }
//                R.id.nav_new_task -> {
//                    if (supportFragmentManager.backStackEntryCount > 1) {
//                        onBackPressed()
//                        return@setOnItemSelectedListener true
//                    }
//                    supportFragmentManager
//                        .beginTransaction()
//                        .setCustomAnimations(
//                            R.anim.slide_in,
//                            0,
//                            0,
//                            R.anim.slide_out
//                        )
//                        .add(R.id.fragment_container, NewTaskFragment(this))
//                        .addToBackStack(null)
//                        .commit()
//                    return@setOnItemSelectedListener true
//                }
//                R.id.nav_dashboard -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.fragment_container, DashboardFragment(), "Dashboard")
//                        .commit()
//                    return@setOnItemSelectedListener true
//                }
//            }
//            true
//        }
    }
//
//    override fun onBackPressed() {
//        if (supportFragmentManager.backStackEntryCount > 1) {
//            supportFragmentManager.popBackStack()
//            val fragment = supportFragmentManager.fragments.first()
//
//            // We are checking for home and dashboard because if we are in this block
//            // then it means that we are removing the new task frag
//            if (fragment.tag == "Home") {
//                // TODO change selected item of bottom navbar
//                bottomNavBar.selectedItemId = R.id.nav_home
//            } else {
//
//            }
//        } else {
//            finish()
//        }
//    }
}
//
fun NavGraphBuilder.composable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable (NavBackStackEntry) -> Unit
) {
    addDestination(
        ComposeNavigator.Destination(provider[ComposeNavigator::class], content).apply {
            this.route = route
            arguments.forEach { (argumentName, argument) ->
                addArgument(argumentName, argument)
            }
            deepLinks.forEach { deepLink ->
                addDeepLink(deepLink)
            }
        }
    )
}

@Composable
fun SlideUpAnimation(comp: @Composable (AnimatedVisibilityScope.() -> Unit)) {
    val state = remember {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }
    AnimatedVisibility(
        visibleState = state,
        enter = slideInVertically(
            initialOffsetY = {300},
            animationSpec =  tween(
                durationMillis = 3000,
                easing = LinearEasing
            )
        ),
        exit = slideOutVertically(
            animationSpec = tween(
                durationMillis = 3000,
                easing = LinearEasing
            )
        ),
        content = comp
    )
}