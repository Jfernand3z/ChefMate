package com.example.chefmateapp.data.repository

import com.example.chefmateapp.data.model.product.Product
import com.example.chefmateapp.data.model.product.ProductRequest
import com.example.chefmateapp.data.remote.ProductService
import retrofit2.Response

class ProductRepository(private val productService: ProductService) {
    suspend fun getProducts(): Response<List<Product>> {
        return productService.getProducts()
    }

    suspend fun addProduct(request: ProductRequest): Response<Product> {
        return productService.addProduct(request)
    }

    suspend fun updateProduct(id: String, request: ProductRequest): Response<Product> {
        return productService.updateProduct(id, request)
    }

    suspend fun deleteProduct(id: String): Response<Unit> {
        return productService.deleteProduct(id)
    }
}