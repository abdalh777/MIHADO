package com.example.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.MihadViewModel
import com.example.screens.*
import com.example.theme.*

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Today : Screen("today", "اليوم", Icons.Default.Home)
    object Planner : Screen("planner", "خطتي", Icons.Default.Schedule)
    object Roadmap : Screen("roadmap", "المهمة", Icons.Default.Event)
    object Study : Screen("study", "المذاكرة", Icons.AutoMirrored.Filled.MenuBook)
    object Heatmap : Screen("heatmap", "الخريطة", Icons.Default.GridOn)
    object Habits : Screen("habits", "العادات", Icons.Default.CheckCircle)
    object Analytics : Screen("analytics", "تحليلي", Icons.Default.BarChart)
    object Profile : Screen("profile", "الملف الشخصي", Icons.Default.Person)
}

@Composable
fun MihadNavigation(vm: MihadViewModel) {
    val user by vm.currentUser.collectAsState()
    val navController = rememberNavController()

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Forest,
            secondary = Mint,
            background = Soft,
            surface = CardBg,
            onBackground = Ink,
            onPrimary = Color.White,
            onSurface = Ink
        )
    ) {
        CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PremiumBackgroundBrush)
                ) {
                    if (user == null) {
                        AuthScreen(vm)
                    } else {
                        val activeUser = user!!
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route

                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            containerColor = Color.Transparent,
                        bottomBar = {
                            // Only show bottom navigation on main tabs, hide on Profile screen
                            if (currentRoute != Screen.Profile.route) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
                                        .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.5f), spotColor = Color.Black.copy(alpha = 0.7f))
                                        .background(CardBg.copy(alpha = 0.95f), RoundedCornerShape(20.dp))
                                        .border(1.dp, Ink.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                                        .padding(vertical = 4.dp, horizontal = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val tabs = listOf(
                                            Screen.Today,
                                            Screen.Planner,
                                            Screen.Roadmap,
                                            Screen.Study,
                                            Screen.Heatmap,
                                            Screen.Habits,
                                            Screen.Analytics
                                        )
                                        tabs.forEach { screen ->
                                            val isSelected = currentRoute == screen.route
                                            
                                            val scale by animateFloatAsState(if (isSelected) 1.05f else 1.0f, label = "tab_scale")
                                            
                                            Column(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        navController.navigate(screen.route) {
                                                            popUpTo(navController.graph.findStartDestination().id) {
                                                                saveState = true
                                                            }
                                                            launchSingleTop = true
                                                            restoreState = true
                                                        }
                                                    }
                                                    .padding(horizontal = 4.dp, vertical = 4.dp)
                                                    .graphicsLayer {
                                                        scaleX = scale
                                                        scaleY = scale
                                                    },
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(34.dp)
                                                        .background(
                                                            if (isSelected) Forest.copy(alpha = 0.12f) else Color.Transparent,
                                                            RoundedCornerShape(10.dp)
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = screen.icon,
                                                        contentDescription = screen.title,
                                                        tint = if (isSelected) Forest else Ink.copy(alpha = 0.4f),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(1.dp))
                                                Text(
                                                    text = screen.title,
                                                    fontSize = 8.sp,
                                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                                    color = if (isSelected) Forest else Ink.copy(alpha = 0.4f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    ) { padding ->
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Today.route,
                            modifier = Modifier.padding(padding),
                            enterTransition = { fadeIn(animationSpec = tween(220)) },
                            exitTransition = { fadeOut(animationSpec = tween(220)) }
                        ) {
                            composable(Screen.Today.route) {
                                TodayScreen(
                                    vm = vm,
                                    user = activeUser,
                                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                                    onNavigateToPlanner = { navController.navigate(Screen.Planner.route) },
                                    onNavigateToRoadmap = { navController.navigate(Screen.Roadmap.route) }
                                )
                            }
                            composable(Screen.Planner.route) {
                                PlannerScreen(vm)
                            }
                            composable(Screen.Roadmap.route) {
                                RoadmapScreen(vm)
                            }
                            composable(Screen.Study.route) {
                                StudyScreen(vm)
                            }
                            composable(Screen.Heatmap.route) {
                                HeatmapScreen(vm)
                            }
                            composable(Screen.Habits.route) {
                                HabitsScreen(vm)
                            }
                            composable(Screen.Analytics.route) {
                                AnalyticsScreen(vm, activeUser)
                            }
                            composable(Screen.Profile.route) {
                                // Add a back button option inside Profile screen header or simply back gesture
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Column {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = { navController.popBackStack() },
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Mint)
                                            ) {
                                                Icon(Icons.Default.ArrowForward, contentDescription = "رجوع", tint = Forest)
                                            }
                                        }
                                        ProfileScreen(vm, activeUser)
                                    }
                                }
                            }
                        }
                    }
                }
                }
            }
        }
    }
}
