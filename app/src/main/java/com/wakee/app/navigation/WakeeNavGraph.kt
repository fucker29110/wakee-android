package com.wakee.app.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.wakee.app.feature.alarm.AlarmSetScreen
import com.wakee.app.feature.alarm.RingingScreen
import com.wakee.app.feature.auth.LoginScreen
import com.wakee.app.feature.auth.OnboardingScreen
import com.wakee.app.feature.auth.AuthViewModel
import com.wakee.app.feature.chat.ChatListScreen
import com.wakee.app.feature.chat.ChatRoomScreen
import com.wakee.app.feature.friends.FriendFriendsListScreen
import com.wakee.app.feature.friends.FriendProfileScreen
import com.wakee.app.feature.friends.FriendsListScreen
import com.wakee.app.feature.friends.MutualFriendsListScreen
import com.wakee.app.feature.home.HomeScreen
import com.wakee.app.feature.home.PostDetailScreen
import com.wakee.app.feature.home.StoryCreateModal
import com.wakee.app.feature.home.StoryViewModal
import com.wakee.app.feature.notification.NotificationScreen
import com.wakee.app.feature.profile.ProfileScreen
import com.wakee.app.feature.profile.ProfileEditScreen
import com.wakee.app.feature.profile.SettingsScreen
import com.wakee.app.ui.theme.*

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Main : Screen("main")
    data object Home : Screen("home")
    data object Friends : Screen("friends")
    data object SendAlarm : Screen("send_alarm")
    data object Chat : Screen("chat")
    data object Profile : Screen("profile")
    data object PostDetail : Screen("post_detail/{activityId}") {
        fun createRoute(activityId: String) = "post_detail/$activityId"
    }
    data object FriendProfile : Screen("friend_profile/{userId}") {
        fun createRoute(userId: String) = "friend_profile/$userId"
    }
    data object ChatRoom : Screen("chat_room/{chatId}/{otherUserId}") {
        fun createRoute(chatId: String, otherUserId: String) = "chat_room/$chatId/$otherUserId"
    }
    data object Notification : Screen("notification")
    data object ProfileEdit : Screen("profile_edit")
    data object Settings : Screen("settings")
    data object Ringing : Screen("ringing/{eventId}") {
        fun createRoute(eventId: String) = "ringing/$eventId"
    }
    data object StoryCreate : Screen("story_create")
    data object StoryView : Screen("story_view/{storyId}") {
        fun createRoute(storyId: String) = "story_view/$storyId"
    }
    data object FriendFriends : Screen("friend_friends/{userId}") {
        fun createRoute(userId: String) = "friend_friends/$userId"
    }
    data object MutualFriends : Screen("mutual_friends/{userId}") {
        fun createRoute(userId: String) = "mutual_friends/$userId"
    }
    data object Onboarding : Screen("onboarding")
}

data class BottomNavItem(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String,
    val isCenter: Boolean = false
)

val bottomNavItems = listOf(
    BottomNavItem("home", Icons.Filled.Home, Icons.Outlined.Home, "ホーム"),
    BottomNavItem("friends", Icons.Filled.People, Icons.Outlined.People, "フレンド"),
    BottomNavItem("send_alarm", Icons.Filled.Alarm, Icons.Outlined.Alarm, "アラーム", isCenter = true),
    BottomNavItem("chat", Icons.Filled.Chat, Icons.Outlined.Chat, "チャット"),
    BottomNavItem("profile", Icons.Filled.Person, Icons.Outlined.Person, "プロフィール"),
)

@Composable
fun WakeeNavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize().background(Background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Accent)
        }
        return
    }

    val startDestination = when {
        !isLoggedIn -> Screen.Login.route
        currentUser?.onboardingCompleted == false -> Screen.Onboarding.route
        else -> Screen.Main.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    val user = authViewModel.currentUser.value
                    val dest = if (user?.onboardingCompleted == false) {
                        Screen.Onboarding.route
                    } else {
                        Screen.Main.route
                    }
                    navController.navigate(dest) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToRinging = { eventId ->
                    navController.navigate(Screen.Ringing.createRoute(eventId))
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            Screen.Ringing.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            RingingScreen(
                eventId = eventId,
                onDismissed = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToRinging: (String) -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = Background,
        bottomBar = {
            WakeeBottomBar(navController = navController, currentRoute = currentRoute)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onPostClick = { activityId ->
                        navController.navigate(Screen.PostDetail.createRoute(activityId))
                    },
                    onUserClick = { userId ->
                        navController.navigate(Screen.FriendProfile.createRoute(userId))
                    },
                    onNotificationClick = {
                        navController.navigate(Screen.Notification.route)
                    },
                    onStoryCreate = {
                        navController.navigate(Screen.StoryCreate.route)
                    },
                    onStoryClick = { storyId ->
                        navController.navigate(Screen.StoryView.createRoute(storyId))
                    }
                )
            }

            composable(Screen.Friends.route) {
                FriendsListScreen(
                    onUserClick = { userId ->
                        navController.navigate(Screen.FriendProfile.createRoute(userId))
                    }
                )
            }

            composable(Screen.SendAlarm.route) {
                AlarmSetScreen(
                    onAlarmSent = { navController.popBackStack() }
                )
            }

            composable(Screen.Chat.route) {
                ChatListScreen(
                    onChatClick = { chatId, otherUserId ->
                        navController.navigate(Screen.ChatRoom.createRoute(chatId, otherUserId))
                    },
                    onUserClick = { userId ->
                        navController.navigate(Screen.FriendProfile.createRoute(userId))
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onEditProfile = {
                        navController.navigate(Screen.ProfileEdit.route)
                    },
                    onSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onUserClick = { userId ->
                        navController.navigate(Screen.FriendProfile.createRoute(userId))
                    }
                )
            }

            composable(
                Screen.PostDetail.route,
                arguments = listOf(navArgument("activityId") { type = NavType.StringType })
            ) { backStackEntry ->
                val activityId = backStackEntry.arguments?.getString("activityId") ?: ""
                PostDetailScreen(
                    activityId = activityId,
                    onBack = { navController.popBackStack() },
                    onUserClick = { userId ->
                        navController.navigate(Screen.FriendProfile.createRoute(userId))
                    }
                )
            }

            composable(
                Screen.FriendProfile.route,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                FriendProfileScreen(
                    userId = userId,
                    onBack = { navController.popBackStack() },
                    onChatClick = { chatId, otherUserId ->
                        navController.navigate(Screen.ChatRoom.createRoute(chatId, otherUserId))
                    },
                    onFriendsListClick = { uid ->
                        navController.navigate(Screen.FriendFriends.createRoute(uid))
                    },
                    onMutualFriendsClick = { uid ->
                        navController.navigate(Screen.MutualFriends.createRoute(uid))
                    }
                )
            }

            composable(
                Screen.FriendFriends.route,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                FriendFriendsListScreen(
                    userId = userId,
                    onBack = { navController.popBackStack() },
                    onUserClick = { uid ->
                        navController.navigate(Screen.FriendProfile.createRoute(uid))
                    }
                )
            }

            composable(
                Screen.MutualFriends.route,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                MutualFriendsListScreen(
                    userId = userId,
                    onBack = { navController.popBackStack() },
                    onUserClick = { uid ->
                        navController.navigate(Screen.FriendProfile.createRoute(uid))
                    }
                )
            }

            composable(
                Screen.ChatRoom.route,
                arguments = listOf(
                    navArgument("chatId") { type = NavType.StringType },
                    navArgument("otherUserId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                val otherUserId = backStackEntry.arguments?.getString("otherUserId") ?: ""
                ChatRoomScreen(
                    chatId = chatId,
                    otherUserId = otherUserId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Notification.route) {
                NotificationScreen(
                    onBack = { navController.popBackStack() },
                    onUserClick = { userId ->
                        navController.navigate(Screen.FriendProfile.createRoute(userId))
                    }
                )
            }

            composable(Screen.ProfileEdit.route) {
                ProfileEditScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = onLogout
                )
            }

            composable(Screen.StoryCreate.route) {
                StoryCreateModal(
                    onDismiss = { navController.popBackStack() }
                )
            }

            composable(
                Screen.StoryView.route,
                arguments = listOf(navArgument("storyId") { type = NavType.StringType })
            ) { backStackEntry ->
                val storyId = backStackEntry.arguments?.getString("storyId") ?: ""
                StoryViewModal(
                    storyId = storyId,
                    onDismiss = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun WakeeBottomBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar(
        containerColor = Surface,
        contentColor = PrimaryText,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            if (item.isCenter) {
                NavigationBarItem(
                    selected = currentRoute == item.route,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(Accent, AccentEnd))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Alarm,
                                contentDescription = item.label,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    label = null,
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent
                    )
                )
            } else {
                val selected = currentRoute == item.route
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            tint = if (selected) TabActive else TabInactive
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            fontSize = 10.sp,
                            color = if (selected) TabActive else TabInactive
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}
