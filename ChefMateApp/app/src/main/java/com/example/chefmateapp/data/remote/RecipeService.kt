package com.example.chefmateapp.data.remote

import com.example.chefmateapp.data.model.recipe.CookResponse
import com.example.chefmateapp.data.model.recipe.Recipe
import com.example.chefmateapp.data.model.recipe.RecipeCookNewRequest
import com.example.chefmateapp.data.model.recipe.RecipeGenerateRequest
import com.example.chefmateapp.data.model.recipe.RecipeHistoryCookRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RecipeService {
    @POST("ChefMate/recipes/generate")
    suspend fun generateRecipes(@Body request: RecipeGenerateRequest): Response<List<Recipe>>

    @POST("ChefMate/recipes/cook")
    suspend fun cookNewRecipe(@Body request: RecipeCookNewRequest): Response<Recipe>

    @POST("ChefMate/recipes/{id}/cook")
    suspend fun cookSavedRecipe(
        @Path("id") id: String,
        @Body request: RecipeHistoryCookRequest
    ): Response<CookResponse>

    @GET("ChefMate/recipes")
    suspend fun getRecipeHistory(): Response<List<Recipe>>

    @GET("ChefMate/recipes/{id}")
    suspend fun getRecipe(@Path("id") id: String): Response<Recipe>
}
