package com.example.chefmateapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chefmateapp.data.model.recipe.Recipe
import com.example.chefmateapp.data.model.recipe.RecipeCookNewRequest
import com.example.chefmateapp.data.model.recipe.RecipeGenerateRequest
import com.example.chefmateapp.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RecipeUiState {
    object Idle : RecipeUiState()
    object Loading : RecipeUiState()
    data class Success(val recipes: List<Recipe>) : RecipeUiState()
    data class Error(val message: String) : RecipeUiState()
}

sealed class HistoryUiState {
    object Idle : HistoryUiState()
    object Loading : HistoryUiState()
    data class Success(val recipes: List<Recipe>) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val _generateState = MutableStateFlow<RecipeUiState>(RecipeUiState.Idle)
    val generateState: StateFlow<RecipeUiState> = _generateState.asStateFlow()

    private val _historyState = MutableStateFlow<HistoryUiState>(HistoryUiState.Idle)
    val historyState: StateFlow<HistoryUiState> = _historyState.asStateFlow()

    private val _cookingId = MutableStateFlow<String?>(null)
    val cookingId: StateFlow<String?> = _cookingId.asStateFlow()

    private val _cookedSet = MutableStateFlow<Set<Int>>(emptySet())
    val cookedSet: StateFlow<Set<Int>> = _cookedSet.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun generateRecipes(request: RecipeGenerateRequest) {
        viewModelScope.launch {
            _generateState.value = RecipeUiState.Loading
            _cookedSet.value = emptySet()
            try {
                val response = repository.generateRecipes(request)
                if (response.isSuccessful) {
                    _generateState.value = RecipeUiState.Success(response.body() ?: emptyList())
                } else {
                    _generateState.value = RecipeUiState.Error("Error ${response.code()}: No se pudo generar recetas")
                }
            } catch (e: Exception) {
                _generateState.value = RecipeUiState.Error("Error de conexión: ${e.localizedMessage ?: "No se pudo contactar al servidor"}")

            }
        }
    }

    fun cookNewRecipe(recipe: Recipe, index: Int, servings: Int) {
        viewModelScope.launch {
            _cookingId.value = "new_$index"
            try {
                val response = repository.cookNewRecipe(RecipeCookNewRequest(recipe, servings))
                if (response.isSuccessful) {
                    _cookedSet.value = _cookedSet.value + index
                    _snackbarMessage.value = "¡${recipe.name} guardada y cocinada!"
                } else {
                    _snackbarMessage.value = "Error al cocinar la receta"
                }
            } catch (e: Exception) {
                _snackbarMessage.value = "Error de red: ${e.localizedMessage}"
            } finally {
                _cookingId.value = null
            }
        }
    }

    fun fetchHistory() {
        viewModelScope.launch {
            _historyState.value = HistoryUiState.Loading
            try {
                val response = repository.getRecipeHistory()
                if (response.isSuccessful) {
                    _historyState.value = HistoryUiState.Success(response.body() ?: emptyList())
                } else {
                    _historyState.value = HistoryUiState.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                _historyState.value = HistoryUiState.Error("Error de red: ${e.localizedMessage}")
            }
        }
    }

    fun cookSavedRecipe(recipeId: String, recipeName: String, servings: Int) {
        viewModelScope.launch {
            _cookingId.value = recipeId
            try {
                val response = repository.cookSavedRecipe(recipeId, servings)
                if (response.isSuccessful) {
                    _snackbarMessage.value = "¡$recipeName cocinada! Stock actualizado."
                    fetchHistory()
                } else {
                    _snackbarMessage.value = "Error al cocinar"
                }
            } catch (e: Exception) {
                _snackbarMessage.value = "Error de red: ${e.localizedMessage}"
            } finally {
                _cookingId.value = null
            }
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun resetGenerateState() {
        _generateState.value = RecipeUiState.Idle
        _cookedSet.value = emptySet()
    }
}
