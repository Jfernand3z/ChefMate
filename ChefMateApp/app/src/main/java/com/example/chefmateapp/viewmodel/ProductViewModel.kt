package com.example.chefmateapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chefmateapp.data.model.product.Product
import com.example.chefmateapp.data.model.product.ProductRequest
import com.example.chefmateapp.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun fetchProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = repository.getProducts()
                if (response.isSuccessful) {
                    _products.value = response.body() ?: emptyList()
                } else if (response.code() != 401) {
                    _errorMessage.value = "Error fetching products: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addProduct(request: ProductRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.addProduct(request)
                if (response.isSuccessful) {
                    fetchProducts()
                } else {
                    _errorMessage.value = "Error adding product"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProduct(id: String, request: ProductRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.updateProduct(id, request)
                if (response.isSuccessful) {
                    fetchProducts()
                } else {
                    _errorMessage.value = "Error updating product"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteProduct(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.deleteProduct(id)
                if (response.isSuccessful) {
                    fetchProducts()
                } else {
                    _errorMessage.value = "Error deleting product"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}