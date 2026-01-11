package com.example.iurankomplek.presentation.ui.activity

import com.example.iurankomplek.core.base.BaseActivity
import com.example.iurankomplek.presentation.adapter.PemanfaatanAdapter
import com.example.iurankomplek.presentation.adapter.LaporanSummaryAdapter
import com.example.iurankomplek.presentation.adapter.LaporanSummaryItem
import com.example.iurankomplek.R
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.iurankomplek.databinding.ActivityLaporanBinding
import com.example.iurankomplek.utils.InputSanitizer
import com.example.iurankomplek.di.DependencyContainer
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.data.api.models.PemanfaatanResponse
import com.example.iurankomplek.data.dto.LegacyDataItemDto
import com.example.iurankomplek.domain.model.FinancialItem
import com.example.iurankomplek.presentation.viewmodel.FinancialViewModel
import com.example.iurankomplek.presentation.ui.helper.RecyclerViewHelper
import com.example.iurankomplek.presentation.ui.helper.SwipeRefreshHelper
import com.example.iurankomplek.presentation.ui.helper.StateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LaporanActivity : BaseActivity() {
    private lateinit var adapter: PemanfaatanAdapter
    private lateinit var summaryAdapter: LaporanSummaryAdapter
    private lateinit var binding: ActivityLaporanBinding
    private lateinit var viewModel: FinancialViewModel
    private lateinit var stateManager: StateManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel with use case and payment integration from DI container
        viewModel = DependencyContainer.provideFinancialViewModel()

        adapter = PemanfaatanAdapter()
        summaryAdapter = LaporanSummaryAdapter()

        stateManager = StateManager.create(
            progressBar = binding.stateManagementInclude?.progressBar,
            emptyStateTextView = binding.stateManagementInclude?.emptyStateTextView,
            errorStateLayout = binding.stateManagementInclude?.errorStateLayout,
            errorStateTextView = binding.stateManagementInclude?.errorStateTextView,
            retryTextView = binding.stateManagementInclude?.retryTextView,
            recyclerView = binding.rvLaporan,
            scope = lifecycleScope,
            context = this
        )

        RecyclerViewHelper.configureRecyclerView(
            recyclerView = binding.rvLaporan,
            itemCount = 20,
            enableKeyboardNav = true,
            adapter = adapter,
            orientation = resources.configuration.orientation,
            screenWidthDp = resources.configuration.screenWidthDp
        )

        RecyclerViewHelper.configureRecyclerView(
            recyclerView = binding.rvSummary,
            itemCount = 20,
            enableKeyboardNav = true,
            adapter = summaryAdapter,
            orientation = resources.configuration.orientation,
            screenWidthDp = resources.configuration.screenWidthDp
        )

        SwipeRefreshHelper.configureSwipeRefresh(binding.swipeRefreshLayout) {
            viewModel.loadFinancialData()
        }
        observeFinancialState()
        viewModel.loadFinancialData()
    }
     
    
    private fun observeFinancialState() {
        stateManager.observeState(viewModel.financialState, onSuccess = { data ->
            binding.swipeRefreshLayout.isRefreshing = false
            SwipeRefreshHelper.announceRefreshComplete(binding.swipeRefreshLayout, this)

            data.data?.let { dataArray ->
                if (dataArray.isEmpty()) {
                    stateManager.showEmpty()
                } else {
                    stateManager.showSuccess()
                    binding.rvSummary.visibility = View.VISIBLE

                    adapter.submitList(dataArray)
                    calculateAndSetSummary(dataArray)
                }
            } ?: run {
                stateManager.showError(
                    errorMessage = getString(R.string.invalid_response_format),
                    onRetry = { viewModel.loadFinancialData() }
                )
            }
        }, onError = { error ->
            binding.swipeRefreshLayout.isRefreshing = false
            binding.stateManagementInclude?.errorStateTextView?.text = error
            binding.stateManagementInclude?.retryTextView?.setOnClickListener { viewModel.loadFinancialData() }
        })
    }
    
    private fun calculateAndSetSummary(dataArray: List<LegacyDataItemDto>) {
        val financialItems = FinancialItem.fromLegacyDataItemDtoList(dataArray)
        val summary = viewModel.calculateFinancialSummary(financialItems)
        
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