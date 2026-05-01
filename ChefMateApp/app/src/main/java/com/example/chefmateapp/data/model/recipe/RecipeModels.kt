package com.example.chefmateapp.data.model.recipe

import com.google.gson.annotations.SerializedName

data class RecipeIngredient(
    val name: String,
    val quantity: Double,
    val unit: String
)

data class Recipe(
    val id: String? = null,
    val name: String,
    val type: String,
    val description: String,
    val ingredients: List<RecipeIngredient>,
    val steps: List<String>,
    @SerializedName("prep_time_minutes") val prepTimeMinutes: Int,
    val location: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("max_servings") val maxServings: Int? = null
)

data class RecipeGenerateRequest(
    val servings: Int,
    val location: String? = null,
    @SerializedName("selected_products") val selectedProducts: List<String>? = null,
    @SerializedName("priority_product") val priorityProduct: String? = null,
    @SerializedName("recipe_type") val recipeType: String? = null
)

data class RecipeCookNewRequest(
    val recipe: Recipe,
    val servings: Int
)

data class RecipeHistoryCookRequest(
    val servings: Int
)

data class CookResponse(
    val success: Boolean,
    val message: String
)
