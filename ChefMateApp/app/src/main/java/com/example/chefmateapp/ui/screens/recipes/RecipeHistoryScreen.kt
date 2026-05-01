package com.example.chefmateapp.ui.screens.recipes

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chefmateapp.data.model.recipe.Recipe
import com.example.chefmateapp.ui.theme.*
import com.example.chefmateapp.viewmodel.HistoryUiState
import com.example.chefmateapp.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeHistoryScreen(viewModel: RecipeViewModel) {
    val state by viewModel.historyState.collectAsState()
    val cookingId by viewModel.cookingId.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.fetchHistory() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Info, Primary)))
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.MenuBook, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Mis Recetas", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = AppTypography.titleLarge.fontFamily)
                        Text("Historial y stock disponible", color = Color.White.copy(alpha = 0.80f), style = AppTypography.bodySmall)
                    }
                }
            }

            PullToRefreshBox(
                isRefreshing = state is HistoryUiState.Loading,
                onRefresh = { viewModel.fetchHistory() },
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                when (val s = state) {
                    is HistoryUiState.Idle, is HistoryUiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Primary)
                        }
                    }
                    is HistoryUiState.Error -> {
                        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(48.dp), tint = Danger)
                                Text(s.message, color = TextMuted, style = AppTypography.bodyMedium)
                                Button(onClick = { viewModel.fetchHistory() }, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                                    Text("Reintentar")
                                }
                            }
                        }
                    }
                    is HistoryUiState.Success -> {
                        if (s.recipes.isEmpty()) {
                            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Icon(Icons.Outlined.MenuBook, contentDescription = null, modifier = Modifier.size(72.dp), tint = BorderMedium)
                                    Text("Aún no tienes recetas guardadas", color = TextMuted, style = AppTypography.bodyMedium)
                                    Text("Genera recetas con el Chef IA", color = TextMuted, style = AppTypography.bodySmall)
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(s.recipes) { recipe ->
                                    HistoryRecipeCard(
                                        recipe = recipe,
                                        isCooking = cookingId == recipe.id,
                                        onCook = { servings ->
                                            recipe.id?.let { viewModel.cookSavedRecipe(it, recipe.name, servings) }
                                        }
                                    )
                                }
                                item { Spacer(Modifier.height(16.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HistoryRecipeCard(
    recipe: Recipe,
    isCooking: Boolean,
    onCook: (Int) -> Unit
) {
    val maxServings = recipe.maxServings ?: 0
    var servings by remember { mutableIntStateOf(1) }
    var isExpanded by remember { mutableStateOf(false) }

    val availColor = when {
        maxServings == 0 -> Danger
        maxServings <= 2 -> Warning
        else -> Primary
    }
    val availBg = when {
        maxServings == 0 -> DangerLight
        maxServings <= 2 -> WarningLight
        else -> PrimaryLight
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, availColor.copy(alpha = 0.25f))
    ) {
        Column {
            LinearProgressIndicator(
                progress = { if (maxServings == 0) 0f else (maxServings.toFloat() / 10f).coerceIn(0.05f, 1f) },
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = availColor,
                trackColor = BorderLight
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (recipe.type == "reposteria") AccentLight else PrimaryLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (recipe.type == "reposteria") Icons.Outlined.Cake else Icons.Outlined.RamenDining,
                            contentDescription = null,
                            tint = if (recipe.type == "reposteria") Accent else Primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(recipe.name, style = AppTypography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text(recipe.type.replaceFirstChar { it.uppercase() }, style = AppTypography.labelSmall) },
                                colors = SuggestionChipDefaults.suggestionChipColors(containerColor = if (recipe.type == "reposteria") AccentLight else PrimaryLight, labelColor = if (recipe.type == "reposteria") Accent else Primary),
                                modifier = Modifier.height(24.dp)
                            )
                            Icon(Icons.Outlined.Timer, contentDescription = null, modifier = Modifier.size(13.dp), tint = TextMuted)
                            Text("${recipe.prepTimeMinutes} min", style = AppTypography.labelSmall, color = TextMuted)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(availBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (maxServings == 0) Icon(Icons.Outlined.Warning, contentDescription = null, modifier = Modifier.size(12.dp), tint = availColor)
                            else Icon(Icons.Outlined.People, contentDescription = null, modifier = Modifier.size(12.dp), tint = availColor)
                            Text(
                                if (maxServings == 0) "Sin stock" else "$maxServings porc.",
                                style = AppTypography.labelSmall, color = availColor, fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(recipe.description, style = AppTypography.bodySmall, color = TextSecondary, maxLines = if (isExpanded) Int.MAX_VALUE else 2, overflow = TextOverflow.Ellipsis)

                AnimatedVisibility(visible = isExpanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                    Column {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderLight)
                        Text("Ingredientes (por porción)", style = AppTypography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        recipe.ingredients.forEach { ing ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(ing.name, style = AppTypography.bodySmall, color = TextPrimary)
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${ing.quantity} ${ing.unit}", style = AppTypography.bodySmall, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                                    if (servings > 1) {
                                        Text("× $servings = ${"%.2f".format(ing.quantity * servings)} ${ing.unit}", style = AppTypography.labelSmall, color = TextMuted)
                                    }
                                }
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderLight)
                        Text("Pasos", style = AppTypography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        recipe.steps.forEachIndexed { i, step ->
                            Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(PrimaryLight), contentAlignment = Alignment.Center) {
                                    Text("${i + 1}", style = AppTypography.labelSmall, color = Primary, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(step, style = AppTypography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (maxServings > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Text("Porciones:", style = AppTypography.labelMedium, color = TextSecondary)
                        FilledTonalIconButton(
                            onClick = { if (servings > 1) servings-- },
                            modifier = Modifier.size(30.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = PrimaryLight, contentColor = Primary)
                        ) { Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        Text("$servings", style = AppTypography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        FilledTonalIconButton(
                            onClick = { if (servings < maxServings) servings++ },
                            modifier = Modifier.size(30.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = PrimaryLight, contentColor = Primary)
                        ) { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        Text("/ $maxServings", style = AppTypography.labelSmall, color = TextMuted)
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BorderMedium)
                    ) {
                        val rot by animateFloatAsState(if (isExpanded) 180f else 0f, label = "chev")
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(18.dp).rotate(rot), tint = TextSecondary)
                        Spacer(Modifier.width(4.dp))
                        Text(if (isExpanded) "Ocultar" else "Ver receta", style = AppTypography.labelMedium, color = TextSecondary)
                    }
                    Button(
                        onClick = { onCook(servings) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        enabled = maxServings > 0 && !isCooking,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            disabledContainerColor = BorderMedium
                        )
                    ) {
                        if (isCooking) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(6.dp))
                            Text("Cocinando…", style = AppTypography.labelMedium)
                        } else {
                            Icon(Icons.Outlined.OutdoorGrill, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (maxServings > 0) Color.White else TextMuted)
                            Spacer(Modifier.width(6.dp))
                            Text("Cocinar", style = AppTypography.labelMedium, color = if (maxServings > 0) Color.White else TextMuted)
                        }
                    }
                }
            }
        }
    }
}
