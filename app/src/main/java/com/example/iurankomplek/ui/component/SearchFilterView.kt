package com.example.iurankomplek.ui.component

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.iurankomplek.databinding.ViewSearchFilterBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchFilterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewSearchFilterBinding.inflate(
        LayoutInflater.from(context), this, true
    )
    
    private var onSearchQueryChanged: ((String) -> Unit)? = null
    private var onSortOptionChanged: ((SortOption) -> Unit)? = null
    private var onClearFilters: (() -> Unit)? = null
    
    init {
        setupSearchInput()
        setupSortSpinner()
        setupClearButton()
    }
    
    private fun setupSearchInput() {
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                onSearchQueryChanged?.invoke(s?.toString() ?: "")
            }
        })
    }
    
    private fun setupSortSpinner() {
        val adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            SortOption.values().map { it.displayName }
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        
        binding.sortSpinner.adapter = adapter
        binding.sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                SortOption.values().getOrNull(position)?.let { option ->
                    onSortOptionChanged?.invoke(option)
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun setupClearButton() {
        binding.clearButton.setOnClickListener {
            binding.searchInput.text?.clear()
            binding.sortSpinner.setSelection(0)
            onClearFilters?.invoke()
        }
    }
    
    fun setSearchQuery(query: String) {
        binding.searchInput.setText(query)
        binding.searchInput.setSelection(query.length)
    }
    
    fun setSortOption(option: SortOption) {
        val position = SortOption.values().indexOf(option)
        if (position >= 0) {
            binding.sortSpinner.setSelection(position)
        }
    }
    
    fun setOnSearchQueryChanged(listener: (String) -> Unit) {
        onSearchQueryChanged = listener
    }
    
    fun setOnSortOptionChanged(listener: (SortOption) -> Unit) {
        onSortOptionChanged = listener
    }
    
    fun setOnClearFilters(listener: () -> Unit) {
        onClearFilters = listener
    }
    
    fun setFilterActive(isActive: Boolean) {
        binding.clearButton.visibility = if (isActive) VISIBLE else GONE
    }
    
    fun observeViewModel(
        lifecycleOwner: LifecycleOwner,
        viewModel: SearchFilterViewModel<*>
    ) {
        lifecycleOwner.lifecycleScope.launch {
            viewModel.searchQuery.collectLatest { query ->
                if (binding.searchInput.text?.toString() != query) {
                    setSearchQuery(query)
                }
            }
        }
        
        lifecycleOwner.lifecycleScope.launch {
            viewModel.sortOption.collectLatest { option ->
                setSortOption(option)
            }
        }
        
        lifecycleOwner.lifecycleScope.launch {
            viewModel.isFilterActive.collectLatest { isActive ->
                setFilterActive(isActive)
            }
        }
    }
}
