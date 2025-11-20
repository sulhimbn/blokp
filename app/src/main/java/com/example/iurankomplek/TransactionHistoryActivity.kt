package com.example.iurankomplek

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.ActivityTransactionHistoryBinding
import com.example.iurankomplek.payment.PaymentStatus
import com.example.iurankomplek.transaction.Transaction
import com.example.iurankomplek.transaction.TransactionDatabase
import com.example.iurankomplek.transaction.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransactionHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionHistoryBinding
    private lateinit var transactionAdapter: TransactionHistoryAdapter
    private lateinit var transactionRepository: TransactionRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTransactionHistory()
        loadTransactionHistory()
    }

    private fun setupTransactionHistory() {
        // Initialize the repository
        val transactionDatabase = TransactionDatabase.getDatabase(this)
        val transactionDao = transactionDatabase.transactionDao()
        val mockPaymentGateway = com.example.iurankomplek.payment.MockPaymentGateway()
        transactionRepository = TransactionRepository(mockPaymentGateway, transactionDao)

        // Initialize the adapter
        transactionAdapter = TransactionHistoryAdapter()

        // Setup RecyclerView
        binding.rvTransactionHistory.layoutManager = LinearLayoutManager(this)
        binding.rvTransactionHistory.adapter = transactionAdapter
    }

    private fun loadTransactionHistory() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // For now, we'll get all transactions - in a real app, we'd filter by actual user ID
                // Using a placeholder user ID for demo purposes
                val transactions = transactionRepository.getTransactionsByStatus(PaymentStatus.COMPLETED).value
                runOnUiThread {
                    transactionAdapter.submitList(transactions)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@TransactionHistoryActivity, 
                        "Failed to load transaction history: ${e.message}", 
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}