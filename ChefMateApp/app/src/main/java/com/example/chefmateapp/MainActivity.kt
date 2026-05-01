package com.example.chefmateapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.chefmateapp.ui.theme.AppTypography
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chefmateapp.data.remote.NetworkModule
import com.example.chefmateapp.data.repository.AuthRepository
import com.example.chefmateapp.data.repository.ProductRepository
import com.example.chefmateapp.data.repository.RecipeRepository
import com.example.chefmateapp.ui.screens.auth.AuthScreen
import com.example.chefmateapp.ui.screens.inventory.InventoryScreen
import com.example.chefmateapp.ui.screens.recipes.RecipeGeneratorScreen
import com.example.chefmateapp.ui.screens.recipes.RecipeHistoryScreen
import com.example.chefmateapp.ui.theme.*
import com.example.chefmateapp.viewmodel.AuthViewModel
import com.example.chefmateapp.viewmodel.ProductViewModel
import com.example.chefmateapp.viewmodel.RecipeViewModel

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector, val iconSelected: ImageVector)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authRepository = AuthRepository(NetworkModule.authService)
        val authViewModel = AuthViewModel(authRepository)

        val productRepository = ProductRepository(NetworkModule.productService)
        val productViewModel = ProductViewModel(productRepository)

        val recipeRepository = RecipeRepository(NetworkModule.recipeService)
        val recipeViewModel = RecipeViewModel(recipeRepository)

        enableEdgeToEdge()
        setContent {
            ChefMateAppTheme {
                val rootNav = rememberNavController()

                NavHost(navController = rootNav, startDestination = "auth") {
                    composable(
                        "auth",
                        enterTransition = { fadeIn(animationSpec = tween(300)) },
                        exitTransition = { fadeOut(animationSpec = tween(300)) }
                    ) {
                        AuthScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = {
                                rootNav.navigate("home") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(
                        "home",
                        enterTransition = {
                            slideInVertically(initialOffsetY = { it / 4 }, animationSpec = tween(400)) +
                            fadeIn(animationSpec = tween(400))
                        },
                        exitTransition = { fadeOut(animationSpec = tween(300)) }
                    ) {
                        HomeScaffold(
                            productViewModel = productViewModel,
                            authViewModel = authViewModel,
                            recipeViewModel = recipeViewModel,
                            onLogout = {
                                rootNav.navigate("auth") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScaffold(
    productViewModel: ProductViewModel,
    authViewModel: AuthViewModel,
    recipeViewModel: RecipeViewModel,
    onLogout: () -> Unit
) {
    val navItems = listOf(
        BottomNavItem("inventory", "Inventario", Icons.Outlined.Inventory2, Icons.Filled.Inventory2),
        BottomNavItem("chef_ia", "Chef IA", Icons.Outlined.AutoAwesome, Icons.Filled.AutoAwesome),
        BottomNavItem("history", "Mis Recetas", Icons.Outlined.MenuBook, Icons.Filled.MenuBook)
    )

    var currentTab by remember { mutableStateOf("inventory") }
    val products by productViewModel.products.collectAsState()

    LaunchedEffect(currentTab) {
        if (currentTab == "inventory") productViewModel.fetchProducts()
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Surface,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        clip = false,
                        ambientColor = Color(0xFF000000).copy(alpha = 0.08f),
                        spotColor = Color(0xFF000000).copy(alpha = 0.08f)
                    )
            ) {
                navItems.forEach { item ->
                    val selected = currentTab == item.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = item.route },
                        icon = {
                            val scale by animateFloatAsState(
                                targetValue = if (selected) 1.15f else 1f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "icon_scale"
                            )
                            Icon(
                                imageVector = if (selected) item.iconSelected else item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.size((24 * scale).dp)
                            )
                        },
                        label = {
                            Text(
                                item.label,
                                style = AppTypography.labelSmall,
                                fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold
                                else androidx.compose.ui.text.font.FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Primary,
                            selectedTextColor = Primary,
                            indicatorColor = PrimaryLight,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        )
                    )
                }
            }
        },
        containerColor = Background
    ) { padding ->
        AnimatedContent(
            targetState = currentTab,
            transitionSpec = {
                (slideInHorizontally(
                    initialOffsetX = { if (targetState > initialState) it / 3 else -it / 3 },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(250))).togetherWith(
                    slideOutHorizontally(
                        targetOffsetX = { if (targetState > initialState) -it / 3 else it / 3 },
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(200))
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()),
            label = "tab_transition"
        ) { tab ->
            when (tab) {
                "inventory" -> InventoryScreen(
                    productViewModel = productViewModel,
                    authViewModel = authViewModel,
                    onLogout = onLogout
                )
                "chef_ia" -> RecipeGeneratorScreen(
                    viewModel = recipeViewModel,
                    availableProducts = products
                )
                "history" -> RecipeHistoryScreen(viewModel = recipeViewModel)
            }
        }
    }
}