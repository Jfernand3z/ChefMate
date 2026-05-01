package com.example.chefmateapp.data.remote

import com.example.chefmateapp.data.model.product.ProductListResponse
import com.example.chefmateapp.data.model.product.ProductRequest
import com.example.chefmateapp.data.model.product.Product
import retrofit2.Response
import retrofit2.http.*

interface ProductService {
    @GET("ChefMate/products")
    suspend fun getProducts(): Response<List<Product>>

    @POST("ChefMate/products")
    suspend fun addProduct(@Body request: ProductRequest): Response<Product>

    @PUT("ChefMate/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: String,
        @Body request: ProductRequest
    ): Response<Product>

    @DELETE("ChefMate/products/{id}")
    suspend fun deleteProduct(@Path("id") id: String): Response<Unit>
}