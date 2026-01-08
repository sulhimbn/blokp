package com.example.iurankomplek.presentation.ui.activity

import com.example.iurankomplek.core.base.BaseActivity
import com.example.iurankomplek.presentation.adapter.PemanfaatanAdapter
import com.example.iurankomplek.presentation.adapter.LaporanSummaryAdapter
import com.example.iurankomplek.presentation.adapter.LaporanSummaryItem
import com.example.iurankomplek.R
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.ActivityLaporanBinding
import com.example.iurankomplek.data.repository.PemanfaatanRepositoryFactory
import com.example.iurankomplek.data.mapper.EntityMapper
import com.example.iurankomplek.model.ValidatedDataItem
import com.example.iurankomplek.utils.InputSanitizer
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.data.database.TransactionDatabase
import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.data.repository.TransactionRepositoryFactory
import com.example.iurankomplek.payment.MockPaymentGateway
import com.example.iurankomplek.presentation.viewmodel.FinancialViewModel
import com.example.iurankomplek.presentation.viewmodel.FinancialViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LaporanActivity : BaseActivity() {
    private lateinit var adapter: PemanfaatanAdapter
    private lateinit var summaryAdapter: LaporanSummaryAdapter
    private lateinit var binding: ActivityLaporanBinding
    private lateinit var viewModel: FinancialViewModel
    private lateinit var transactionRepository: TransactionRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize transaction repository for payment integration
        initializeTransactionRepository()

        // Initialize ViewModel with repository
        val pemanfaatanRepository = PemanfaatanRepositoryFactory.getInstance()
        viewModel = ViewModelProvider(this, FinancialViewModel.Factory(pemanfaatanRepository))[FinancialViewModel::class.java]

        adapter = PemanfaatanAdapter()
        summaryAdapter = LaporanSummaryAdapter()

        binding.rvLaporan.layoutManager = LinearLayoutManager(this)
        binding.rvLaporan.adapter = adapter

        binding.rvSummary.layoutManager = LinearLayoutManager(this)
        binding.rvSummary.adapter = summaryAdapter

         setupSwipeRefresh()
         observeFinancialState()
         viewModel.loadFinancialData()
     }
     
     private fun setupSwipeRefresh() {
         binding.swipeRefreshLayout.setOnRefreshListener {
             viewModel.loadFinancialData()
         }
     }
    
    private fun observeFinancialState() {
        lifecycleScope.launch {
            viewModel.financialState.collect { state ->
                when (state) {
                    is UiState.Idle -> {
                    }
                     is UiState.Loading -> {
                         binding.progressBar.visibility = View.VISIBLE
                         binding.swipeRefreshLayout.isRefreshing = true
                     }
                       is UiState.Success -> {
                           binding.progressBar.visibility = View.GONE
                           binding.swipeRefreshLayout.isRefreshing = false
                           state.data.data?.let { dataArray ->
                              if (dataArray.isEmpty()) {
                                  Toast.makeText(this@LaporanActivity, getString(R.string.no_financial_data_available), Toast.LENGTH_LONG).show()
                                  return@let
                              }
                              
                            // Convert LegacyDataItemDto to DataItem and set on adapter
                               val dataItems = EntityMapper.toDataItemList(dataArray)
                               adapter.submitList(dataItems)

                               // Calculate and set summary items with payment integration
                               calculateAndSetSummary(dataItems)
                            } ?: run {
                                Toast.makeText(this@LaporanActivity, getString(R.string.invalid_response_format), Toast.LENGTH_LONG).show()
                            }
                  }
                      is UiState.Error -> {
                         binding.progressBar.visibility = View.GONE
                         binding.swipeRefreshLayout.isRefreshing = false
                         Toast.makeText(this@LaporanActivity, state.error, Toast.LENGTH_LONG).show()
                     }
                }
            }
        }
    }
    
    private fun calculateAndSetSummary(dataArray: List<com.example.iurankomplek.model.DataItem>) {
        try {
            val calculator = com.example.iurankomplek.utils.FinancialCalculator

            if (!calculator.validateFinancialCalculations(dataArray)) {
                Toast.makeText(this, getString(R.string.invalid_financial_data_detected), Toast.LENGTH_LONG).show()
                return
            }

            val totalIuranBulanan = calculator.calculateTotalIuranBulanan(dataArray)
            val totalPengeluaran = calculator.calculateTotalPengeluaran(dataArray)
            val totalIuranIndividu = calculator.calculateTotalIuranIndividu(dataArray)
            val rekapIuran = calculator.calculateRekapIuran(dataArray)

            summaryAdapter.setItems(createSummaryItems(totalIuranBulanan, totalPengeluaran, rekapIuran))

            integratePaymentTransactions(
                totalIuranBulanan,
                totalPengeluaran,
                rekapIuran
            )
        } catch (e: ArithmeticException) {
            Toast.makeText(this, getString(R.string.financial_calculation_overflow_error), Toast.LENGTH_LONG).show()
        } catch (e: IllegalArgumentException) {
            Toast.makeText(this, getString(R.string.invalid_financial_data_detected), Toast.LENGTH_LONG).show()
        }
    }

    private fun createSummaryItems(
        totalIuranBulanan: Int,
        totalPengeluaran: Int,
        rekapIuran: Int
    ): List<LaporanSummaryItem> = listOf(
        LaporanSummaryItem(getString(R.string.jumlah_iuran_bulanan), InputSanitizer.formatCurrency(totalIuranBulanan)),
        LaporanSummaryItem(getString(R.string.total_pengeluaran), InputSanitizer.formatCurrency(totalPengeluaran)),
        LaporanSummaryItem(getString(R.string.rekap_total_iuran), InputSanitizer.formatCurrency(rekapIuran))
    )
    
    private fun integratePaymentTransactions(
        totalIuranBulanan: Int,
        totalPengeluaran: Int,
        rekapIuran: Int
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val completedTransactions = fetchCompletedTransactions()
                val paymentTotal = calculatePaymentTotal(completedTransactions)

                withContext(Dispatchers.Main) {
                    if (completedTransactions.isNotEmpty()) {
                        updateSummaryWithPayments(totalIuranBulanan, totalPengeluaran, rekapIuran, paymentTotal, completedTransactions.size)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@LaporanActivity,
                        getString(R.string.error_integrating_payment_data, e.message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private suspend fun fetchCompletedTransactions() =
        transactionRepository.getTransactionsByStatus(
            com.example.iurankomplek.payment.PaymentStatus.COMPLETED
        ).first()

    private fun calculatePaymentTotal(transactions: List<com.example.iurankomplek.data.transaction.Transaction>) =
        transactions.sumOf { it.amount.toInt() }

    private fun updateSummaryWithPayments(
        totalIuranBulanan: Int,
        totalPengeluaran: Int,
        rekapIuran: Int,
        paymentTotal: Int,
        transactionCount: Int
    ) {
        val baseSummaryItems = createSummaryItems(totalIuranBulanan, totalPengeluaran, rekapIuran)
        val updatedSummaryItems = baseSummaryItems + LaporanSummaryItem(
            getString(R.string.total_payments_processed),
            InputSanitizer.formatCurrency(paymentTotal)
        )

        summaryAdapter.setItems(updatedSummaryItems)

        Toast.makeText(
            this,
            getString(R.string.integrated_payment_transactions, transactionCount, InputSanitizer.formatCurrency(paymentTotal)),
            Toast.LENGTH_LONG
        ).show()
    }
    
    private fun initializeTransactionRepository() {
        transactionRepository = TransactionRepositoryFactory.getMockInstance(this)
    }
}