package com.example.iurankomplek.ui.component

data class SearchFilterState(
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.NAME_ASC,
    val isFilterActive: Boolean = false
)

enum class SortOption(val displayName: String) {
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    DATE_NEWEST("Newest First"),
    DATE_OLDEST("Oldest First"),
    AMOUNT_HIGH("Amount (High-Low)"),
    AMOUNT_LOW("Amount (Low-High)")
}
