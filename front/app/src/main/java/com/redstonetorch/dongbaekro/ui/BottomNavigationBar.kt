package com.redstonetorch.dongbaekro.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.redstonetorch.dongbaekro.ui.screens.HomeScreen
import com.redstonetorch.dongbaekro.ui.screens.RouteTypeSelectionScreen
import com.redstonetorch.dongbaekro.ui.screens.SOSScreen
import com.redstonetorch.dongbaekro.ui.screens.SettingsScreen
import com.redstonetorch.dongbaekro.ui.dto.KakaoPlace

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "홈", Icons.Default.Home)
    object SOS : Screen("sos", "SOS", Icons.Default.Warning)
    object Settings : Screen("settings", "설정", Icons.Default.Settings)
}

val items = listOf(
    Screen.Home,
    Screen.SOS,
    Screen.Settings
)

@Composable
fun AppNavigation(viewModel: AuthViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Home.route, Modifier.padding(innerPadding)) {
            composable(Screen.Home.route) { 
                HomeScreen(
                    onNavigateToRouteSelection = { currentLat, currentLng, destination ->
                        navController.navigate(
                            "route_selection/$currentLat/$currentLng/${destination.place_name}/${destination.x}/${destination.y}/${destination.address_name}/${destination.road_address_name}"
                        )
                    }
                )
            }
            
            // 경로 선택 화면 추가
            composable(
                "route_selection/{currentLat}/{currentLng}/{placeName}/{x}/{y}/{address}/{roadAddress}",
                arguments = listOf(
                    navArgument("currentLat") { type = NavType.StringType },
                    navArgument("currentLng") { type = NavType.StringType },
                    navArgument("placeName") { type = NavType.StringType },
                    navArgument("x") { type = NavType.StringType },
                    navArgument("y") { type = NavType.StringType },
                    navArgument("address") { type = NavType.StringType },
                    navArgument("roadAddress") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val currentLat = backStackEntry.arguments?.getString("currentLat")?.toDoubleOrNull() ?: 0.0
                val currentLng = backStackEntry.arguments?.getString("currentLng")?.toDoubleOrNull() ?: 0.0
                val placeName = backStackEntry.arguments?.getString("placeName") ?: ""
                val x = backStackEntry.arguments?.getString("x") ?: ""
                val y = backStackEntry.arguments?.getString("y") ?: ""
                val address = backStackEntry.arguments?.getString("address") ?: ""
                val roadAddress = backStackEntry.arguments?.getString("roadAddress") ?: ""
                
                val destination = KakaoPlace(
                    id = "",
                    place_name = placeName,
                    address_name = address,
                    road_address_name = roadAddress,
                    x = x,
                    y = y,
                    phone = "",
                    category_name = "",
                    category_group_code = "",
                    category_group_name = "",
                    distance = "",
                    place_url = ""
                )
                
                RouteTypeSelectionScreen(
                    currentLat = currentLat,
                    currentLng = currentLng,
                    destination = destination,
                    onRouteTypeSelected = { routeType ->
                        // 경로 유형 선택 후 실제 길찾기 화면으로 이동
                        val facilityTypes = when (routeType) {
                            "CCTV" -> listOf("CCTV")
                            "EMERGENCY_BELL" -> listOf("EMERGENCY_BELL")
                            "STREETLAMP" -> listOf("STREETLAMP")
                            "COMPREHENSIVE" -> listOf("CCTV", "EMERGENCY_BELL", "STREETLAMP")
                            else -> listOf("CCTV", "EMERGENCY_BELL", "STREETLAMP")
                        }
                        
                        navController.navigate(
                            "route_result/$currentLat/$currentLng/${destination.x}/${destination.y}/${facilityTypes.joinToString(",")}"
                        )
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            // 길찾기 결과 화면 추가
            composable(
                "route_result/{currentLat}/{currentLng}/{destX}/{destY}/{facilityTypes}",
                arguments = listOf(
                    navArgument("currentLat") { type = NavType.StringType },
                    navArgument("currentLng") { type = NavType.StringType },
                    navArgument("destX") { type = NavType.StringType },
                    navArgument("destY") { type = NavType.StringType },
                    navArgument("facilityTypes") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val currentLat = backStackEntry.arguments?.getString("currentLat")?.toDoubleOrNull() ?: 0.0
                val currentLng = backStackEntry.arguments?.getString("currentLng")?.toDoubleOrNull() ?: 0.0
                val destX = backStackEntry.arguments?.getString("destX")?.toDoubleOrNull() ?: 0.0
                val destY = backStackEntry.arguments?.getString("destY")?.toDoubleOrNull() ?: 0.0
                val facilityTypesString = backStackEntry.arguments?.getString("facilityTypes") ?: ""
                val facilityTypes = facilityTypesString.split(",").filter { it.isNotBlank() }
                
                val origin = com.kakao.vectormap.LatLng.from(currentLat, currentLng)
                val destination = com.kakao.vectormap.LatLng.from(destY, destX)
                
                RouteSelectionScreen(
                    origin = origin,
                    destination = destination,
                    preferredTypes = facilityTypes
                )
            }
            
            composable(Screen.SOS.route) { SOSScreen() }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onLogout = {
                        // Navigate to the login screen or main entry point after logout
                        navController.navigate("login") { // Assuming "login" is the route for the login screen
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true // Clear back stack
                            }
                        }
                    }
                )
            }
            // Add login composable for navigation
            composable("login") { LoginScreen(viewModel = viewModel) } // Assuming LoginScreen takes AuthViewModel
        }
    }
}
