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
import com.example.iurankomplek.utils.InputSanitizer
import com.example.iurankomplek.di.DependencyContainer
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.data.repository.TransactionRepositoryFactory
import com.example.iurankomplek.data.api.models.PemanfaatanResponse
import com.example.iurankomplek.data.dto.LegacyDataItemDto
import com.example.iurankomplek.presentation.viewmodel.FinancialViewModel
import com.example.iurankomplek.presentation.ui.helper.RecyclerViewHelper
import com.example.iurankomplek.presentation.ui.helper.SwipeRefreshHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LaporanActivity : BaseActivity() {
    private lateinit var adapter: PemanfaatanAdapter
    private lateinit var summaryAdapter: LaporanSummaryAdapter
    private lateinit var binding: ActivityLaporanBinding
    private lateinit var viewModel: FinancialViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel with use case and payment integration from DI container
        viewModel = DependencyContainer.provideFinancialViewModel()

        adapter = PemanfaatanAdapter()
        summaryAdapter = LaporanSummaryAdapter()

        RecyclerViewHelper.configureRecyclerView(
            recyclerView = binding.rvLaporan,
            itemCount = 20,
            enableKeyboardNav = true,
            adapter = adapter,
            orientation = resources.configuration.orientation,
            screenWidthDp = resources.configuration.screenWidthDp
        )

        binding.rvSummary.layoutManager = LinearLayoutManager(this)
        binding.rvSummary.setHasFixedSize(true)
        binding.rvSummary.setItemViewCacheSize(20)
        binding.rvSummary.adapter = summaryAdapter

         SwipeRefreshHelper.configureSwipeRefresh(binding.swipeRefreshLayout) {
             viewModel.loadFinancialData()
         }
         observeFinancialState()
         viewModel.loadFinancialData()
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

    private fun handleSuccessState(state: UiState.Success<PemanfaatanResponse>) {
        setUIState(
            loading = false,
            showEmpty = false,
            showError = false,
            showContent = false
        )
        binding.swipeRefreshLayout.isRefreshing = false
        SwipeRefreshHelper.announceRefreshComplete(binding.swipeRefreshLayout, this)

        state.data.data?.let { dataArray ->
            if (dataArray.isEmpty()) {
                setUIState(
                    loading = false,
                    showEmpty = true,
                    showError = false,
                    showContent = false
                )
                return@collect
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
    
    private fun calculateAndSetSummary(dataArray: List<LegacyDataItemDto>) {
        val summary = viewModel.calculateFinancialSummary(dataArray)
        
        if (!summary.isValid) {
            val errorMessage = summary.validationError ?: getString(R.string.invalid_financial_data_detected)
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            return
        }

        summaryAdapter.setItems(createSummaryItems(
            summary.totalIuranBulanan,
            summary.totalPengeluaran,
            summary.rekapIuran
        ))

        integratePaymentTransactions(
            summary.totalIuranBulanan,
            summary.totalPengeluaran,
            summary.rekapIuran
        )
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
            val paymentResult = viewModel.integratePaymentTransactions()

            withContext(Dispatchers.Main) {
                if (paymentResult?.isIntegrated == true) {
                    updateSummaryWithPayments(
                        totalIuranBulanan,
                        totalPengeluaran,
                        rekapIuran,
                        paymentResult.paymentTotal,
                        paymentResult.transactionCount
                    )
                } else if (paymentResult?.error != null) {
                    Toast.makeText(
                        this@LaporanActivity,
                        getString(R.string.error_integrating_payment_data, paymentResult.error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

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
}