package com.example.iurankomplek.core.base

import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.launch

abstract class BaseFragment<T> : Fragment() {

    protected abstract val recyclerView: RecyclerView
    protected abstract val progressBar: View
    protected abstract val emptyMessageStringRes: Int

    protected abstract fun createAdapter(): RecyclerView.Adapter<*>
    protected abstract fun initializeViewModel(viewModelProvider: ViewModelProvider)
    protected abstract fun observeViewModelState()
    protected abstract fun loadData()

    override fun onViewCreated(view: View, savedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        initializeViewModel(ViewModelProvider(this))
        observeViewModelState()
        loadData()
    }

    protected open fun setupRecyclerView() {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            setItemViewCacheSize(20)
            recycledViewPool.setMaxRecycledViews(0, 20)
            adapter = createAdapter()
        }
    }

    protected fun <T> observeUiState(
        stateFlow: kotlinx.coroutines.flow.StateFlow<UiState<T>>,
        onDataLoaded: (T) -> Unit,
        showErrorToast: Boolean = true
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            stateFlow.collect { state ->
                when (state) {
                    is UiState.Idle -> {}
                    is UiState.Loading -> {
                        progressBar.visibility = View.VISIBLE
                    }
                    is UiState.Success -> {
                        progressBar.visibility = View.GONE
                        @Suppress("UNCHECKED_CAST")
                        onDataLoaded(state.data as T)
                    }
                    is UiState.Error -> {
                        progressBar.visibility = View.GONE
                        if (showErrorToast) {
                            Toast.makeText(
                                requireContext(),
                                getString(com.example.iurankomplek.R.string.network_error, state.error),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }
}
