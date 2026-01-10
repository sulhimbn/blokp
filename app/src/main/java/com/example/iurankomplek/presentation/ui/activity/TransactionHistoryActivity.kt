package com.example.iurankomplek.presentation.ui.activity

import com.example.iurankomplek.core.base.BaseActivity
import com.example.iurankomplek.presentation.adapter.TransactionHistoryAdapter
import com.example.iurankomplek.R
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.ActivityTransactionHistoryBinding
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.presentation.viewmodel.TransactionViewModel
import com.example.iurankomplek.di.DependencyContainer
import com.example.iurankomplek.presentation.ui.helper.RecyclerViewHelper
import com.example.iurankomplek.payment.PaymentStatus
import kotlinx.coroutines.launch

class TransactionHistoryActivity : BaseActivity() {
    private lateinit var binding: ActivityTransactionHistoryBinding
    private lateinit var transactionAdapter: TransactionHistoryAdapter
    private lateinit var viewModel: TransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTransactionHistory()
        observeTransactionsState()
        viewModel.loadTransactionsByStatus(com.example.iurankomplek.payment.PaymentStatus.COMPLETED)
    }

    private fun setupTransactionHistory() {
        viewModel = DependencyContainer.provideTransactionViewModel()

        val transactionRepository = DependencyContainer.provideTransactionRepository()
        transactionAdapter = TransactionHistoryAdapter(lifecycleScope, transactionRepository)

        RecyclerViewHelper.configureRecyclerView(
            recyclerView = binding.rvTransactionHistory,
            itemCount = 20,
            enableKeyboardNav = true,
            adapter = transactionAdapter,
            orientation = resources.configuration.orientation,
            screenWidthDp = resources.configuration.screenWidthDp
        )
    }

    private fun observeTransactionsState() {
        lifecycleScope.launch {
            viewModel.transactionsState.collect { state ->
                when (state) {
                    is UiState.Idle -> {
                    }
                    is UiState.Loading -> {
                        binding.progressBar.visibility = android.view.View.VISIBLE
                    }
                    is UiState.Success -> {
                        binding.progressBar.visibility = android.view.View.GONE
                        transactionAdapter.submitList(state.data)
                    }
                    is UiState.Error -> {
                        binding.progressBar.visibility = android.view.View.GONE
                        Toast.makeText(
                            this@TransactionHistoryActivity,
                            state.error,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}
