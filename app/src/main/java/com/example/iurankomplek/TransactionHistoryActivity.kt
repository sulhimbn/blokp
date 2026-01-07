package com.example.iurankomplek

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.ActivityTransactionHistoryBinding
import com.example.iurankomplek.transaction.TransactionRepositoryFactory
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.viewmodel.TransactionViewModel
import com.example.iurankomplek.viewmodel.TransactionViewModelFactory
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
        viewModel.loadTransactionsByStatus(PaymentStatus.COMPLETED.name)
    }

    private fun setupTransactionHistory() {
        val transactionRepository = TransactionRepositoryFactory.getInstance(this)
        viewModel = ViewModelProvider(
            this,
            TransactionViewModelFactory.getInstance(transactionRepository)
        )[TransactionViewModel::class.java]

        transactionAdapter = TransactionHistoryAdapter(lifecycleScope)

        binding.rvTransactionHistory.layoutManager = LinearLayoutManager(this)
        binding.rvTransactionHistory.adapter = transactionAdapter
    }

    private fun observeTransactionsState() {
        lifecycleScope.launch {
            viewModel.transactionsState.collect { state ->
                when (state) {
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
                            getString(R.string.failed_to_load_transaction_history, state.error),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}
