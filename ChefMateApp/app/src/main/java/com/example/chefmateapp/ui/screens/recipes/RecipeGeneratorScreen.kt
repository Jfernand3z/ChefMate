package com.example.chefmateapp.ui.screens.recipes

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chefmateapp.data.model.product.Product
import com.example.chefmateapp.data.model.recipe.Recipe
import com.example.chefmateapp.data.model.recipe.RecipeGenerateRequest
import com.example.chefmateapp.ui.theme.*
import com.example.chefmateapp.viewmodel.RecipeUiState
import com.example.chefmateapp.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun RecipeGeneratorScreen(
    viewModel: RecipeViewModel,
    availableProducts: List<Product>
) {
    val state by viewModel.generateState.collectAsState()
    val cookingId by viewModel.cookingId.collectAsState()
    val cookedSet by viewModel.cookedSet.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    var servings by remember { mutableIntStateOf(2) }
    var location by remember { mutableStateOf("") }
    var priorityProduct by remember { mutableStateOf("") }
    var recipeType by remember { mutableStateOf("") }
    var selectedProducts by remember { mutableStateOf(setOf<String>()) }
    var showFilters by remember { mutableStateOf(false) }
    var expandedIndex by remember { mutableIntStateOf(-1) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    val today = remember {
        java.util.Calendar.getInstance().apply { set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0); set(java.util.Calendar.SECOND, 0) }.timeInMillis
    }
    val vigentProducts = remember(availableProducts) {
        availableProducts.filter { p ->
            if (p.quantity <= 0) return@filter false
            if (p.expirationDate == null) return@filter true
            try {
                val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                (fmt.parse(p.expirationDate.take(10))?.time ?: 0L) > today
            } catch (e: Exception) { true }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(Primary, Color(0xFF059669)))
                        )
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
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text("Chef IA", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = AppTypography.titleLarge.fontFamily)
                            Text("Genera recetas con tu inventario", color = Color.White.copy(alpha = 0.80f), style = AppTypography.bodySmall)
                        }
                    }
                }
            }

            item {
                // Servings card + filters toggle
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Servings row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Porciones", style = AppTypography.labelLarge, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                Text("¿Cuántas personas comerán?", style = AppTypography.labelSmall, color = TextMuted)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                FilledTonalIconButton(
                                    onClick = { if (servings > 1) servings-- },
                                    modifier = Modifier.size(38.dp),
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = PrimaryLight,
                                        contentColor = Primary
                                    )
                                ) { Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                Text(
                                    text = "$servings",
                                    modifier = Modifier.widthIn(min = 40.dp),
                                    style = AppTypography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Primary,
                                    textAlign = TextAlign.Center
                                )
                                FilledTonalIconButton(
                                    onClick = { servings++ },
                                    modifier = Modifier.size(38.dp),
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = PrimaryLight,
                                        contentColor = Primary
                                    )
                                ) { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = BorderLight)

                        // Filters toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showFilters = !showFilters }
                                .clip(RoundedCornerShape(10.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (showFilters) PrimaryLight else SurfaceSecondary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Outlined.Tune,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (showFilters) Primary else TextMuted
                                    )
                                }
                                Column {
                                    Text(
                                        "Filtros avanzados",
                                        style = AppTypography.labelLarge,
                                        color = if (showFilters) Primary else TextSecondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (location.isNotBlank() || selectedProducts.isNotEmpty() || priorityProduct.isNotBlank() || recipeType.isNotBlank()) {
                                        Text("Filtros activos", style = AppTypography.labelSmall, color = Accent)
                                    }
                                }
                            }
                            val rotation by animateFloatAsState(if (showFilters) 180f else 0f, label = "arrow")
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.rotate(rotation).size(20.dp),
                                tint = TextMuted
                            )
                        }
                    }
                }
            }

            item {
                AnimatedVisibility(
                    visible = showFilters,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("Parámetros del Prompt", style = AppTypography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

                            OutlinedTextField(
                                value = location,
                                onValueChange = { location = it },
                                label = { Text("Ubicación") },
                                placeholder = { Text("Ej: La Paz, Cochabamba…") },
                                leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = TextMuted) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                            )

                            Column {
                                Text("Tipo de receta", style = AppTypography.labelMedium, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("" to "Cualquiera", "plato" to "Plato", "reposteria" to "Repostería").forEach { (value, label) ->
                                        FilterChip(
                                            selected = recipeType == value,
                                            onClick = { recipeType = value },
                                            label = { Text(label, style = AppTypography.labelMedium) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Primary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }

                            if (vigentProducts.isNotEmpty()) {
                                Column {
                                    Text("Ingredientes a considerar", style = AppTypography.labelMedium, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                                    Text("(vacío = todos)", style = AppTypography.labelSmall, color = TextMuted)
                                    Spacer(Modifier.height(8.dp))
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        vigentProducts.forEach { prod ->
                                            val isSelected = selectedProducts.contains(prod.name)
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    selectedProducts = if (isSelected) selectedProducts - prod.name else selectedProducts + prod.name
                                                    if (!selectedProducts.contains(priorityProduct)) priorityProduct = ""
                                                },
                                                label = { Text(prod.name, style = AppTypography.labelMedium) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Primary,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                        }
                                    }
                                }

                                val priorityOptions = if (selectedProducts.isEmpty()) vigentProducts else vigentProducts.filter { selectedProducts.contains(it.name) }
                                if (priorityOptions.isNotEmpty()) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.Star, contentDescription = null, tint = Accent, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Ingrediente prioritario", style = AppTypography.labelMedium, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            priorityOptions.forEach { prod ->
                                                val isPriority = priorityProduct == prod.name
                                                FilterChip(
                                                    selected = isPriority,
                                                    onClick = { priorityProduct = if (isPriority) "" else prod.name },
                                                    label = {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            if (isPriority) {
                                                                Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                                                Spacer(Modifier.width(4.dp))
                                                            }
                                                            Text(prod.name, style = AppTypography.labelMedium)
                                                        }
                                                    },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = Accent,
                                                        selectedLabelColor = Color.White
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        viewModel.generateRecipes(
                            RecipeGenerateRequest(
                                servings = servings,
                                location = location.ifBlank { null },
                                selectedProducts = if (selectedProducts.isEmpty()) null else selectedProducts.toList(),
                                priorityProduct = priorityProduct.ifBlank { null },
                                recipeType = recipeType.ifBlank { null }
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color.White,
                        disabledContainerColor = BorderMedium
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 0.dp),
                    enabled = state !is RecipeUiState.Loading
                ) {
                    if (state is RecipeUiState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("Consultando al Chef IA…", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.White)
                    } else {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                        Spacer(Modifier.width(10.dp))
                        Text("Generar Recetas con IA", fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 0.5.sp, color = Color.White)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            when (val s = state) {
                is RecipeUiState.Idle -> item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp, bottom = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(PrimaryLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.RamenDining, contentDescription = null, modifier = Modifier.size(48.dp), tint = Primary)
                        }
                        Text("Listo para cocinar", style = AppTypography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(
                            "Ajusta las porciones, activa filtros y pulsa el botón de arriba para que la IA genere tus recetas.",
                            color = TextMuted,
                            style = AppTypography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
                is RecipeUiState.Error -> item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = DangerLight),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Danger.copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Danger, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(s.message, color = Danger, style = AppTypography.bodySmall)
                        }
                    }
                }
                is RecipeUiState.Success -> {
                    if (s.recipes.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("La IA no pudo generar recetas con el inventario actual.", color = TextMuted, style = AppTypography.bodyMedium)
                            }
                        }
                    } else {
                        itemsIndexed(s.recipes) { idx, recipe ->
                            RecipeCard(
                                recipe = recipe,
                                isExpanded = expandedIndex == idx,
                                isCooking = cookingId == "new_$idx",
                                isCooked = cookedSet.contains(idx),
                                servings = servings,
                                onToggleExpand = { expandedIndex = if (expandedIndex == idx) -1 else idx },
                                onCook = { viewModel.cookNewRecipe(recipe, idx, servings) },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
                is RecipeUiState.Loading -> item {
                    var textIndex by remember { mutableIntStateOf(0) }
                    val loadingTexts = listOf(
                        "Afilando los cuchillos...",
                        "Buscando ingredientes secretos...",
                        "Calentando los fogones...",
                        "Consultando el recetario de la abuela...",
                        "Añadiendo una pizca de magia...",
                        "Calculando tiempos de cocción..."
                    )
                    
                    LaunchedEffect(Unit) {
                        while(true) {
                            kotlinx.coroutines.delay(2500)
                            textIndex = (textIndex + 1) % loadingTexts.size
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "loading_inf")
                        val offsetY by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = -15f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "loading_bounce"
                        )
                        
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = Icons.Outlined.OutdoorGrill, 
                                contentDescription = null, 
                                modifier = Modifier.size(72.dp).offset(y = offsetY.dp), 
                                tint = Primary
                            )
                        }
                        Spacer(Modifier.height(24.dp))
                        
                        Crossfade(targetState = textIndex, label = "text_anim") { idx ->
                            Text(
                                text = loadingTexts[idx], 
                                style = AppTypography.titleMedium, 
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        
                        Text(
                            text = "Analizando tus ingredientes para encontrar la mejor combinación culinaria.", 
                            style = AppTypography.bodySmall, 
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RecipeCard(
    recipe: Recipe,
    isExpanded: Boolean,
    isCooking: Boolean,
    isCooked: Boolean,
    servings: Int,
    onToggleExpand: () -> Unit,
    onCook: () -> Unit,
    modifier: Modifier = Modifier
) {
    val elevation by animateDpAsState(if (isExpanded) 6.dp else 2.dp, label = "elev")
    val scale by animateFloatAsState(if (isCooked) 1f else 1f, label = "scale")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (isCooked) PrimaryLight.copy(alpha = 0.3f) else Surface),
        elevation = CardDefaults.cardElevation(elevation),
        border = if (isCooked) BorderStroke(1.5.dp, Primary.copy(alpha = 0.5f)) else null
    ) {
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
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(recipe.type.replaceFirstChar { it.uppercase() }, style = AppTypography.labelSmall) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (recipe.type == "reposteria") AccentLight else PrimaryLight,
                                labelColor = if (recipe.type == "reposteria") Accent else Primary
                            ),
                            modifier = Modifier.height(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Outlined.Timer, contentDescription = null, modifier = Modifier.size(13.dp), tint = TextMuted)
                        Text(" ${recipe.prepTimeMinutes} min", style = AppTypography.labelSmall, color = TextMuted)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(recipe.description, style = AppTypography.bodySmall, color = TextSecondary, maxLines = if (isExpanded) Int.MAX_VALUE else 2, overflow = TextOverflow.Ellipsis)

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                recipe.ingredients.take(3).forEach { ing ->
                    SuggestionChip(
                        onClick = {},
                        label = { Text("${ing.name} · ${ing.quantity}${ing.unit}", style = AppTypography.labelSmall, maxLines = 1) },
                        modifier = Modifier.height(26.dp)
                    )
                }
                if (recipe.ingredients.size > 3) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("+${recipe.ingredients.size - 3}", style = AppTypography.labelSmall) },
                        modifier = Modifier.height(26.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BorderLight)
                    Text("Ingredientes completos", style = AppTypography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    recipe.ingredients.forEach { ing ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(ing.name, style = AppTypography.bodySmall, color = TextPrimary)
                            Text("${ing.quantity} ${ing.unit}", style = AppTypography.bodySmall, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BorderLight)
                    Text("Pasos", style = AppTypography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    recipe.steps.forEachIndexed { i, step ->
                        Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier.size(22.dp).clip(CircleShape).background(PrimaryLight),
                                contentAlignment = Alignment.Center
                            ) { Text("${i + 1}", style = AppTypography.labelSmall, color = Primary, fontWeight = FontWeight.Bold) }
                            Spacer(Modifier.width(8.dp))
                            Text(step, style = AppTypography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onToggleExpand,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BorderMedium)
                ) {
                    val rot by animateFloatAsState(if (isExpanded) 180f else 0f, label = "chevron")
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(18.dp).rotate(rot), tint = TextSecondary)
                    Spacer(Modifier.width(4.dp))
                    Text(if (isExpanded) "Ocultar" else "Ver receta", style = AppTypography.labelMedium, color = TextSecondary)
                }
                Button(
                    onClick = { if (!isCooked) onCook() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isCooking && !isCooked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        disabledContainerColor = if (isCooked) Primary.copy(alpha = 0.7f) else BorderMedium
                    )
                ) {
                    if (isCooking) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(6.dp))
                        Text("Cocinando…", style = AppTypography.labelMedium, color = Color.White)
                    } else if (isCooked) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("¡Cocinado!", style = AppTypography.labelMedium, color = Color.White)
                    } else {
                        Icon(Icons.Outlined.OutdoorGrill, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("Cocinar", style = AppTypography.labelMedium, color = Color.White)
                    }
                }
            }
        }
    }
}
