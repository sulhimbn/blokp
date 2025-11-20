package com.example.iurankomplek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.example.iurankomplek.utils.UiState

abstract class BaseViewModel<T : Any> : ViewModel() {
    protected val _items = MutableLiveData<UiState<List<T>>>()
    val items = _items.asLiveData()
    
    protected val _selectedItem = MutableLiveData<UiState<T>>()
    val selectedItem = _selectedItem.asLiveData()
    
    abstract fun loadItems()
    abstract fun saveItem(item: T)
    abstract fun deleteItem(id: String)
}