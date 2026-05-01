package com.example.chefmateapp.data.repository

import com.example.chefmateapp.data.model.recipe.CookResponse
import com.example.chefmateapp.data.model.recipe.Recipe
import com.example.chefmateapp.data.model.recipe.RecipeCookNewRequest
import com.example.chefmateapp.data.model.recipe.RecipeGenerateRequest
import com.example.chefmateapp.data.model.recipe.RecipeHistoryCookRequest
import com.example.chefmateapp.data.remote.RecipeService
import retrofit2.Response

class RecipeRepository(private val recipeService: RecipeService) {
    suspend fun generateRecipes(request: RecipeGenerateRequest): Response<List<Recipe>> =
        recipeService.generateRecipes(request)

    suspend fun cookNewRecipe(request: RecipeCookNewRequest): Response<Recipe> =
        recipeService.cookNewRecipe(request)

    suspend fun cookSavedRecipe(id: String, servings: Int): Response<CookResponse> =
        recipeService.cookSavedRecipe(id, RecipeHistoryCookRequest(servings))

    suspend fun getRecipeHistory(): Response<List<Recipe>> =
        recipeService.getRecipeHistory()
}
