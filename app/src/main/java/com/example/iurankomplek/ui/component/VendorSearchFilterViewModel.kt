package com.example.iurankomplek.ui.component

import com.example.iurankomplek.model.Vendor

class VendorSearchFilterViewModel : SearchFilterViewModel<Vendor>() {
    
    override fun applyFilters(
        items: List<Vendor>,
        query: String,
        sort: SortOption
    ): List<Vendor> {
        val filtered = if (query.isBlank()) {
            items
        } else {
            items.filter { matchesQuery(it, query) }
        }
        
        return filtered.sortedWith { item1, item2 ->
            compareItems(item1, item2, sort)
        }
    }
    
    override fun matchesQuery(item: Vendor, query: String): Boolean {
        val normalizedQuery = query.lowercase()
        return item.name.lowercase().contains(normalizedQuery) ||
               item.category.lowercase().contains(normalizedQuery) ||
               item.contact.lowercase().contains(normalizedQuery)
    }
    
    override fun compareItems(item1: Vendor, item2: Vendor, sort: SortOption): Int {
        return when (sort) {
            SortOption.NAME_ASC -> item1.name.compareTo(item2.name, ignoreCase = true)
            SortOption.NAME_DESC -> item2.name.compareTo(item1.name, ignoreCase = true)
            SortOption.DATE_NEWEST -> item2.id.compareTo(item1.id)
            SortOption.DATE_OLDEST -> item1.id.compareTo(item2.id)
            else -> item1.name.compareTo(item2.name, ignoreCase = true)
        }
    }
}
