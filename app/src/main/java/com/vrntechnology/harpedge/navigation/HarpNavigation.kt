package com.vrntechnology.harpedge.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vrntechnology.harpedge.data.model.UserRole
import com.vrntechnology.harpedge.data.repository.AuthRepository
import com.vrntechnology.harpedge.data.repository.HarpRepository
import com.vrntechnology.harpedge.ui.alerts.AlertsScreen
import com.vrntechnology.harpedge.ui.analytics.AnalyticsScreen
import com.vrntechnology.harpedge.ui.auth.AuthViewModel
import com.vrntechnology.harpedge.ui.auth.LoginScreen
import com.vrntechnology.harpedge.ui.dashboard.*
import com.vrntechnology.harpedge.ui.events.EventDetailScreen
import com.vrntechnology.harpedge.ui.events.EventsScreen
import com.vrntechnology.harpedge.ui.fingerprint.HazardFingerprintScreen
import com.vrntechnology.harpedge.ui.health.SensorHealthScreen
import com.vrntechnology.harpedge.ui.inspection.FieldVerificationScreen
import com.vrntechnology.harpedge.ui.map.MapScreen
import com.vrntechnology.harpedge.ui.nodes.AddNodeScreen
import com.vrntechnology.harpedge.ui.nodes.NodeDetailScreen
import com.vrntechnology.harpedge.ui.nodes.NodeListScreen
import com.vrntechnology.harpedge.ui.profile.ProfileScreen
import com.vrntechnology.harpedge.ui.settings.AuditLogScreen
import com.vrntechnology.harpedge.ui.settings.SettingsScreen
import com.vrntechnology.harpedge.ui.theme.*

object HarpDestinations {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val NODES = "nodes"
    const val ADD_NODE = "add_node"
    const val NODE_DETAIL = "node_detail/{nodeId}"
    const val MAP = "map"
    const val EVENTS = "events"
    const val EVENT_DETAIL = "event_detail/{eventId}"
    const val ALERTS = "alerts"
    const val FINGERPRINT = "fingerprint/{eventId}"
    const val SENSOR_HEALTH = "sensor_health/{nodeId}"
    const val FIELD_VERIFY = "field_verify/{eventId}"
    const val ANALYTICS = "analytics"
    const val SETTINGS = "settings"
    const val AUDIT_LOGS = "audit_logs"
    const val PROFILE = "profile"
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun HarpNavGraph(
    navController: NavHostController = rememberNavController(),
    authRepository: AuthRepository,
    harpRepository: HarpRepository,
    authViewModel: AuthViewModel = remember { AuthViewModel(authRepository) },
    dashboardViewModel: DashboardViewModel = remember { DashboardViewModel(harpRepository) }
) {
    val currentUser by authRepository.currentUser.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val userRole = currentUser?.role ?: UserRole.ADMIN

    val bottomNavItems = when (userRole) {
        UserRole.ADMIN -> listOf(
            BottomNavItem("Home", HarpDestinations.DASHBOARD, Icons.Default.Dashboard, "tab_home"),
            BottomNavItem("Nodes", HarpDestinations.NODES, Icons.Default.Sensors, "tab_nodes"),
            BottomNavItem("Map", HarpDestinations.MAP, Icons.Default.Map, "tab_map"),
            BottomNavItem("Events", HarpDestinations.EVENTS, Icons.Default.Timeline, "tab_events"),
            BottomNavItem("Settings", HarpDestinations.SETTINGS, Icons.Default.Settings, "tab_settings")
        )
        UserRole.AUTHORITY -> listOf(
            BottomNavItem("Home", HarpDestinations.DASHBOARD, Icons.Default.Dashboard, "tab_home"),
            BottomNavItem("Map", HarpDestinations.MAP, Icons.Default.Map, "tab_map"),
            BottomNavItem("Alerts", HarpDestinations.ALERTS, Icons.Default.Notifications, "tab_alerts"),
            BottomNavItem("Events", HarpDestinations.EVENTS, Icons.Default.Timeline, "tab_events"),
            BottomNavItem("Profile", HarpDestinations.PROFILE, Icons.Default.Person, "tab_profile")
        )
        UserRole.VIEWER -> listOf(
            BottomNavItem("Home", HarpDestinations.DASHBOARD, Icons.Default.Dashboard, "tab_home"),
            BottomNavItem("Map", HarpDestinations.MAP, Icons.Default.Map, "tab_map"),
            BottomNavItem("Alerts", HarpDestinations.ALERTS, Icons.Default.Notifications, "tab_alerts"),
            BottomNavItem("Profile", HarpDestinations.PROFILE, Icons.Default.Person, "tab_profile")
        )
    }

    val shouldShowBottomBar = currentUser != null &&
            currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        containerColor = HarpNavyDark,
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = HarpNavySurface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("harp_bottom_navigation")
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(HarpDestinations.DASHBOARD) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text(item.label, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = HarpNavyDark,
                                selectedTextColor = HarpTeal,
                                indicatorColor = HarpTeal,
                                unselectedIconColor = HarpTextSecondary,
                                unselectedTextColor = HarpTextSecondary
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = HarpDestinations.LOGIN,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Login / Auth
            composable(HarpDestinations.LOGIN) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onNavigateToDashboard = {
                        navController.navigate(HarpDestinations.DASHBOARD) {
                            popUpTo(HarpDestinations.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            // Role-routed Dashboard
            composable(HarpDestinations.DASHBOARD) {
                when (userRole) {
                    UserRole.ADMIN -> AdminDashboardScreen(
                        dashboardViewModel = dashboardViewModel,
                        currentUser = currentUser,
                        onNavigateToMap = { navController.navigate(HarpDestinations.MAP) },
                        onNavigateToInspect = { eventId ->
                            navController.navigate("event_detail/$eventId")
                        },
                        onNavigateToNodes = { navController.navigate(HarpDestinations.NODES) },
                        onNavigateToEvents = { navController.navigate(HarpDestinations.EVENTS) },
                        onNavigateToAnalytics = { navController.navigate(HarpDestinations.ANALYTICS) },
                        onNavigateToSettings = { navController.navigate(HarpDestinations.SETTINGS) },
                        onSignOut = {
                            authViewModel.signOut()
                            navController.navigate(HarpDestinations.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                    UserRole.AUTHORITY -> AuthorityDashboardScreen(
                        dashboardViewModel = dashboardViewModel,
                        currentUser = currentUser,
                        onNavigateToMap = { navController.navigate(HarpDestinations.MAP) },
                        onNavigateToVerify = { eventId ->
                            navController.navigate("field_verify/$eventId")
                        },
                        onNavigateToAlerts = { navController.navigate(HarpDestinations.ALERTS) },
                        onNavigateToEvents = { navController.navigate(HarpDestinations.EVENTS) },
                        onSignOut = {
                            authViewModel.signOut()
                            navController.navigate(HarpDestinations.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                    UserRole.VIEWER -> ViewerDashboardScreen(
                        dashboardViewModel = dashboardViewModel,
                        currentUser = currentUser,
                        onNavigateToMap = { navController.navigate(HarpDestinations.MAP) },
                        onSignOut = {
                            authViewModel.signOut()
                            navController.navigate(HarpDestinations.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }

            // Nodes List
            composable(HarpDestinations.NODES) {
                val nodes by harpRepository.nodes.collectAsState()
                NodeListScreen(
                    nodes = nodes,
                    userRole = userRole,
                    onAddNode = { navController.navigate(HarpDestinations.ADD_NODE) },
                    onSelectNode = { node ->
                        navController.navigate("node_detail/${node.nodeId}")
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Add Node
            composable(HarpDestinations.ADD_NODE) {
                AddNodeScreen(
                    repository = harpRepository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Node Detail
            composable(HarpDestinations.NODE_DETAIL) { backStackEntry ->
                val nodeId = backStackEntry.arguments?.getString("nodeId") ?: ""
                val nodes by harpRepository.nodes.collectAsState()
                val node = nodes.find { it.nodeId == nodeId } ?: nodes.firstOrNull()

                if (node != null) {
                    NodeDetailScreen(
                        node = node,
                        repository = harpRepository,
                        userRole = userRole,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToHealth = { nId ->
                            navController.navigate("sensor_health/$nId")
                        }
                    )
                }
            }

            // Interactive GIS Map
            composable(HarpDestinations.MAP) {
                MapScreen(
                    dashboardViewModel = dashboardViewModel,
                    userRole = userRole,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEvent = { eventId ->
                        navController.navigate("event_detail/$eventId")
                    },
                    onNavigateToNode = { nodeId ->
                        navController.navigate("node_detail/$nodeId")
                    }
                )
            }

            // Events List
            composable(HarpDestinations.EVENTS) {
                val events by harpRepository.events.collectAsState()
                EventsScreen(
                    events = events,
                    onSelectEvent = { eventId ->
                        navController.navigate("event_detail/$eventId")
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Event Detail
            composable(HarpDestinations.EVENT_DETAIL) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                val events by harpRepository.events.collectAsState()
                val event = events.find { it.eventId == eventId } ?: events.firstOrNull()

                if (event != null) {
                    EventDetailScreen(
                        event = event,
                        repository = harpRepository,
                        userRole = userRole,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToFingerprint = { eId ->
                            navController.navigate("fingerprint/$eId")
                        },
                        onNavigateToVerify = { eId ->
                            navController.navigate("field_verify/$eId")
                        },
                        onNavigateToMap = { navController.navigate(HarpDestinations.MAP) }
                    )
                }
            }

            // Alerts
            composable(HarpDestinations.ALERTS) {
                AlertsScreen(
                    repository = harpRepository,
                    currentUser = currentUser,
                    onSelectAlert = { eventId ->
                        navController.navigate("event_detail/$eventId")
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Hazard Fingerprint
            composable(HarpDestinations.FINGERPRINT) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                HazardFingerprintScreen(
                    eventId = eventId,
                    repository = harpRepository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Sensor Health Diagnostics
            composable(HarpDestinations.SENSOR_HEALTH) { backStackEntry ->
                val nodeId = backStackEntry.arguments?.getString("nodeId") ?: ""
                SensorHealthScreen(
                    nodeId = nodeId,
                    repository = harpRepository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Field Verification
            composable(HarpDestinations.FIELD_VERIFY) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                FieldVerificationScreen(
                    eventId = eventId,
                    repository = harpRepository,
                    currentUser = currentUser,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // AI Analytics & Research Bench
            composable(HarpDestinations.ANALYTICS) {
                AnalyticsScreen(
                    repository = harpRepository,
                    userRole = userRole,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Settings & Thresholds
            composable(HarpDestinations.SETTINGS) {
                SettingsScreen(
                    repository = harpRepository,
                    userRole = userRole,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAuditLogs = { navController.navigate(HarpDestinations.AUDIT_LOGS) }
                )
            }

            // Audit Logs
            composable(HarpDestinations.AUDIT_LOGS) {
                AuditLogScreen(
                    repository = harpRepository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Profile
            composable(HarpDestinations.PROFILE) {
                ProfileScreen(
                    user = currentUser,
                    onSignOut = {
                        authViewModel.signOut()
                        navController.navigate(HarpDestinations.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
