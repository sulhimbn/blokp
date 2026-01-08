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
import android.content.res.Configuration
import androidx.recyclerview.widget.GridLayoutManager
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

        // Initialize ViewModel with use case
        val pemanfaatanRepository = PemanfaatanRepositoryFactory.getInstance()
        val loadFinancialDataUseCase = com.example.iurankomplek.domain.usecase.LoadFinancialDataUseCase(pemanfaatanRepository)
        viewModel = ViewModelProvider(this, FinancialViewModel.Factory(loadFinancialDataUseCase))[FinancialViewModel::class.java]

        adapter = PemanfaatanAdapter()
        summaryAdapter = LaporanSummaryAdapter()

        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val gridLayoutManager = GridLayoutManager(this, 2)
            binding.rvLaporan.layoutManager = gridLayoutManager
        } else {
            binding.rvLaporan.layoutManager = LinearLayoutManager(this)
        }
        binding.rvLaporan.setHasFixedSize(true)
        binding.rvLaporan.setItemViewCacheSize(20)
        binding.rvLaporan.focusable = true
        binding.rvLaporan.focusableInTouchMode = true
        binding.rvLaporan.adapter = adapter

        binding.rvSummary.layoutManager = LinearLayoutManager(this)
        binding.rvSummary.setHasFixedSize(true)
        binding.rvSummary.setItemViewCacheSize(10)
        binding.rvSummary.focusable = true
        binding.rvSummary.focusableInTouchMode = true
        binding.rvSummary.adapter = summaryAdapter

        setupRecyclerViewKeyboardNavigation()

         setupSwipeRefresh()
         observeFinancialState()
         viewModel.loadFinancialData()
     }
     
      private fun setupSwipeRefresh() {
          binding.swipeRefreshLayout.setOnRefreshListener {
              viewModel.loadFinancialData()
          }
      }

      private fun announceForAccessibility(text: String) {
          binding.swipeRefreshLayout.announceForAccessibility(text)
      }

      private fun setupRecyclerViewKeyboardNavigation() {
          binding.rvLaporan.setOnKeyListener { _, keyCode, event ->
              if (event.action == android.view.KeyEvent.ACTION_DOWN) {
                  when (keyCode) {
                      android.view.KeyEvent.KEYCODE_DPAD_DOWN,
                      android.view.KeyEvent.KEYCODE_DPAD_UP -> {
                          val layoutManager = binding.rvLaporan.layoutManager as? LinearLayoutManager
                          layoutManager?.let {
                              val firstVisible = it.findFirstVisibleItemPosition()
                              val lastVisible = it.findLastVisibleItemPosition()
                              if (keyCode == android.view.KeyEvent.KEYCODE_DPAD_DOWN && lastVisible < adapter.itemCount - 1) {
                                  binding.rvLaporan.smoothScrollToPosition(lastVisible + 1)
                              } else if (keyCode == android.view.KeyEvent.KEYCODE_DPAD_UP && firstVisible > 0) {
                                  binding.rvLaporan.smoothScrollToPosition(firstVisible - 1)
                              }
                              true
                          } ?: false
                      }
                      else -> false
                  }
              } else {
                  false
              }
          }

          binding.rvSummary.setOnKeyListener { _, keyCode, event ->
              if (event.action == android.view.KeyEvent.ACTION_DOWN) {
                  when (keyCode) {
                      android.view.KeyEvent.KEYCODE_DPAD_DOWN,
                      android.view.KeyEvent.KEYCODE_DPAD_UP -> {
                          val layoutManager = binding.rvSummary.layoutManager as? LinearLayoutManager
                          layoutManager?.let {
                              val firstVisible = it.findFirstVisibleItemPosition()
                              val lastVisible = it.findLastVisibleItemPosition()
                              if (keyCode == android.view.KeyEvent.KEYCODE_DPAD_DOWN && lastVisible < summaryAdapter.itemCount - 1) {
                                  binding.rvSummary.smoothScrollToPosition(lastVisible + 1)
                              } else if (keyCode == android.view.KeyEvent.KEYCODE_DPAD_UP && firstVisible > 0) {
                                  binding.rvSummary.smoothScrollToPosition(firstVisible - 1)
                              }
                              true
                          } ?: false
                      }
                      else -> false
                  }
              } else {
                  false
              }
          }
      }
    
    private fun observeFinancialState() {
        lifecycleScope.launch {
            viewModel.financialState.collect { state ->
                when (state) {
                    is UiState.Idle -> handleIdleState()
                    is UiState.Loading -> handleLoadingState()
                    is UiState.Success -> handleSuccessState(state)
                    is UiState.Error -> handleErrorState(state.error)
                }
            }
        }
    }

    private fun handleIdleState() {
    }

    private fun handleLoadingState() {
        setUIState(
            loading = true,
            showEmpty = false,
            showError = false,
            showContent = false
        )
        binding.swipeRefreshLayout.isRefreshing = true
    }

    private fun handleSuccessState(state: UiState.Success<com.example.iurankomplek.data.dto.PemanfaatanResponse>) {
        setUIState(
            loading = false,
            showEmpty = false,
            showError = false,
            showContent = false
        )
        binding.swipeRefreshLayout.isRefreshing = false
        announceForAccessibility(getString(R.string.swipe_refresh_complete))

        state.data.data?.let { dataArray ->
            if (dataArray.isEmpty()) {
                setUIState(
                    loading = false,
                    showEmpty = true,
                    showError = false,
                    showContent = false
                )
                return
            }

            setUIState(
                loading = false,
                showEmpty = false,
                showError = false,
                showContent = true
            )

            adapter.submitList(dataArray)

            calculateAndSetSummary(dataArray)
        } ?: run {
            setUIState(
                loading = false,
                showEmpty = false,
                showError = true,
                showContent = false
            )
            binding.errorStateTextView.text = getString(R.string.invalid_response_format)
            binding.retryTextView.setOnClickListener { viewModel.loadFinancialData() }
        }
    }

    private fun handleErrorState(error: String) {
        setUIState(
            loading = false,
            showEmpty = false,
            showError = true,
            showContent = false
        )
        binding.errorStateTextView.text = error
        binding.swipeRefreshLayout.isRefreshing = false
        binding.retryTextView.setOnClickListener { viewModel.loadFinancialData() }
    }

    private fun setUIState(loading: Boolean, showEmpty: Boolean, showError: Boolean, showContent: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.emptyStateTextView.visibility = if (showEmpty) View.VISIBLE else View.GONE
        binding.errorStateLayout.visibility = if (showError) View.VISIBLE else View.GONE
        binding.rvLaporan.visibility = if (showContent) View.VISIBLE else View.GONE
        binding.rvSummary.visibility = if (showContent) View.VISIBLE else View.GONE
    }
    
    private fun calculateAndSetSummary(dataArray: List<com.example.iurankomplek.data.dto.LegacyDataItemDto>) {
        try {
            val validateUseCase = com.example.iurankomplek.domain.usecase.ValidateFinancialDataUseCase()
            val calculateTotalsUseCase = com.example.iurankomplek.domain.usecase.CalculateFinancialTotalsUseCase()

            if (!validateUseCase.validateCalculations(dataArray)) {
                Toast.makeText(this, getString(R.string.invalid_financial_data_detected), Toast.LENGTH_LONG).show()
                return
            }

            val totals = calculateTotalsUseCase(dataArray)

            summaryAdapter.setItems(createSummaryItems(totals.totalIuranBulanan, totals.totalPengeluaran, totals.rekapIuran))

            integratePaymentTransactions(
                totals.totalIuranBulanan,
                totals.totalPengeluaran,
                totals.rekapIuran
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