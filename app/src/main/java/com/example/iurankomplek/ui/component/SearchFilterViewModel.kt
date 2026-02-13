package com.example.iurankomplek.ui.component

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

abstract class SearchFilterViewModel<T> : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    private val _sortOption = MutableStateFlow(SortOption.NAME_ASC)
    val sortOption: StateFlow<SortOption> = _sortOption
    
    private val _filteredItems = MutableStateFlow<List<T>>(emptyList())
    val filteredItems: StateFlow<List<T>> = _filteredItems
    
    private val _allItems = MutableStateFlow<List<T>>(emptyList())
    
    private val _isFilterActive = MutableStateFlow(false)
    val isFilterActive: StateFlow<Boolean> = _isFilterActive
    
    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    _allItems.map { items ->
                        applyFilters(items, query, _sortOption.value)
                    }
                }
                .collect { filtered ->
                    _filteredItems.value = filtered
                }
        }
    }
    
    fun setItems(items: List<T>) {
        _allItems.value = items
        applyCurrentFilters()
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        updateFilterActiveState()
    }
    
    fun setSortOption(option: SortOption) {
        _sortOption.value = option
        applyCurrentFilters()
        updateFilterActiveState()
    }
    
    fun clearFilters() {
        _searchQuery.value = ""
        _sortOption.value = SortOption.NAME_ASC
        applyCurrentFilters()
        _isFilterActive.value = false
    }
    
    private fun applyCurrentFilters() {
        val filtered = applyFilters(_allItems.value, _searchQuery.value, _sortOption.value)
        _filteredItems.value = filtered
    }
    
    private fun updateFilterActiveState() {
        _isFilterActive.value = _searchQuery.value.isNotEmpty() || 
                               _sortOption.value != SortOption.NAME_ASC
    }
    
    protected abstract fun applyFilters(
        items: List<T>,
        query: String,
        sort: SortOption
    ): List<T>
    
    protected abstract fun matchesQuery(item: T, query: String): Boolean
    
    protected abstract fun compareItems(item1: T, item2: T, sort: SortOption): Int
}
