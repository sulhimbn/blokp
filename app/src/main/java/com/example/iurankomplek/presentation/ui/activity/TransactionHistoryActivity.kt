package com.example.iurankomplek.presentation.ui.activity

import com.example.iurankomplek.core.base.BaseActivity
import com.example.iurankomplek.presentation.adapter.TransactionHistoryAdapter
import com.example.iurankomplek.R
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.iurankomplek.databinding.ActivityTransactionHistoryBinding
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.presentation.viewmodel.TransactionViewModel
import com.example.iurankomplek.di.DependencyContainer
import com.example.iurankomplek.presentation.ui.helper.RecyclerViewHelper
import com.example.iurankomplek.presentation.ui.helper.StateManager
import com.example.iurankomplek.payment.PaymentStatus
import kotlinx.coroutines.launch

class TransactionHistoryActivity : BaseActivity() {
    private lateinit var binding: ActivityTransactionHistoryBinding
    private lateinit var transactionAdapter: TransactionHistoryAdapter
    private lateinit var viewModel: TransactionViewModel
    private lateinit var stateManager: StateManager

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

        stateManager = StateManager.create(
            progressBar = binding.progressBar,
            emptyStateTextView = binding.tvEmptyState,
            errorStateLayout = null,
            errorStateTextView = null,
            retryTextView = null,
            recyclerView = binding.rvTransactionHistory,
            scope = lifecycleScope,
            context = this
        )

        transactionAdapter = TransactionHistoryAdapter { transaction ->
            lifecycleScope.launch {
                viewModel.refundPayment(transaction.id, "User requested refund")
            }
        }

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
        stateManager.observeState(viewModel.transactionsState, onSuccess = { transactions ->
            if (transactions.isEmpty()) {
                stateManager.showEmpty()
            }
        }, onError = { error ->
            Toast.makeText(
                this@TransactionHistoryActivity,
                error,
                Toast.LENGTH_LONG
            ).show()
        })

        lifecycleScope.launch {
            viewModel.refundState.collect { state ->
                when (state) {
                    is UiState.Idle -> {
                    }
                    is UiState.Loading -> {
                    }
                    is UiState.Success -> {
                        Toast.makeText(
                            this@TransactionHistoryActivity,
                            getString(R.string.refund_processed_successfully),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is UiState.Error -> {
                        Toast.makeText(
                            this@TransactionHistoryActivity,
                            getString(R.string.refund_failed, state.error),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}
