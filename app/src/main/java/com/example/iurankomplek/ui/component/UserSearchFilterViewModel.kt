package com.example.iurankomplek.ui.component

import com.example.iurankomplek.model.DataItem

class UserSearchFilterViewModel : SearchFilterViewModel<DataItem>() {
    
    override fun applyFilters(
        items: List<DataItem>,
        query: String,
        sort: SortOption
    ): List<DataItem> {
        val filtered = if (query.isBlank()) {
            items
        } else {
            items.filter { matchesQuery(it, query) }
        }
        
        return filtered.sortedWith { item1, item2 ->
            compareItems(item1, item2, sort)
        }
    }
    
    override fun matchesQuery(item: DataItem, query: String): Boolean {
        val normalizedQuery = query.lowercase()
        return item.first_name?.lowercase()?.contains(normalizedQuery) == true ||
               item.last_name?.lowercase()?.contains(normalizedQuery) == true ||
               item.email?.lowercase()?.contains(normalizedQuery) == true ||
               item.alamat?.lowercase()?.contains(normalizedQuery) == true
    }
    
    override fun compareItems(item1: DataItem, item2: DataItem, sort: SortOption): Int {
        return when (sort) {
            SortOption.NAME_ASC -> {
                val name1 = "${item1.first_name ?: ""} ${item1.last_name ?: ""}".trim()
                val name2 = "${item2.first_name ?: ""} ${item2.last_name ?: ""}".trim()
                name1.compareTo(name2, ignoreCase = true)
            }
            SortOption.NAME_DESC -> {
                val name1 = "${item1.first_name ?: ""} ${item1.last_name ?: ""}".trim()
                val name2 = "${item2.first_name ?: ""} ${item2.last_name ?: ""}".trim()
                name2.compareTo(name1, ignoreCase = true)
            }
            SortOption.AMOUNT_HIGH -> item2.total_iuran_individu.compareTo(item1.total_iuran_individu)
            SortOption.AMOUNT_LOW -> item1.total_iuran_individu.compareTo(item2.total_iuran_individu)
            else -> 0
        }
    }
}
