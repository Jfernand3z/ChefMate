package com.example.chefmateapp.data.model.product

import com.google.gson.annotations.SerializedName

data class Product(
    val id: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val category: String,
    @SerializedName("expiration_date") val expirationDate: String?
)

data class ProductListResponse(
    val products: List<Product>
)

data class ProductRequest(
    val name: String,
    val quantity: Double,
    val unit: String,
    val category: String,
    @SerializedName("expiration_date") val expirationDate: String?
)