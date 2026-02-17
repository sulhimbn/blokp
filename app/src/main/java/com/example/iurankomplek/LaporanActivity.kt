package com.example.iurankomplek

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.ActivityLaporanBinding
import com.example.iurankomplek.export.ExportFormat
import com.example.iurankomplek.export.ReportExporter
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.LaporanSummaryItem
import com.example.iurankomplek.utils.DataValidator
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.transaction.TransactionDatabase
import com.example.iurankomplek.transaction.TransactionRepository
import com.example.iurankomplek.payment.MockPaymentGateway
import com.example.iurankomplek.viewmodel.FinancialViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class LaporanActivity : BaseActivity() {
    private lateinit var adapter: PemanfaatanAdapter
    private lateinit var summaryAdapter: LaporanSummaryAdapter
    private lateinit var binding: ActivityLaporanBinding
    private val viewModel: FinancialViewModel by viewModels()
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var reportExporter: ReportExporter
    private var currentDataItems: List<DataItem> = emptyList()
    private var currentSummaryItems: List<LaporanSummaryItem> = emptyList()
    private var lastExportResult: ReportExporter.ExportResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeTransactionRepository()
        reportExporter = ReportExporter(this)

        adapter = PemanfaatanAdapter(mutableListOf())
        summaryAdapter = LaporanSummaryAdapter(mutableListOf())
        
        binding.rvLaporan.layoutManager = LinearLayoutManager(this)
        binding.rvLaporan.adapter = adapter
        
        binding.rvSummary.layoutManager = LinearLayoutManager(this)
        binding.rvSummary.adapter = summaryAdapter

         setupSwipeRefresh()
         observeFinancialState()
         viewModel.loadFinancialData()
     }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_laporan, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_export_pdf -> {
                exportReport(ExportFormat.PDF)
                true
            }
            R.id.action_export_csv -> {
                exportReport(ExportFormat.CSV)
                true
            }
            R.id.action_share -> {
                showShareDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun exportReport(format: ExportFormat) {
        if (currentDataItems.isEmpty() || currentSummaryItems.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_data_to_export), Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val result = reportExporter.exportFinancialReport(
                currentDataItems,
                currentSummaryItems,
                format
            )

            withContext(Dispatchers.Main) {
                result.fold(
                    onSuccess = { exportResult ->
                        lastExportResult = exportResult
                        Toast.makeText(
                            this@LaporanActivity,
                            getString(R.string.export_success),
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    onFailure = { error ->
                        Toast.makeText(
                            this@LaporanActivity,
                            getString(R.string.export_failed) + ": ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        }
    }

    private fun showShareDialog() {
        val formats = ExportFormat.values()
        val formatNames = formats.map { it.getDisplayName() }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_export_format))
            .setItems(formatNames) { _, which ->
                shareReport(formats[which])
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun shareReport(format: ExportFormat) {
        if (currentDataItems.isEmpty() || currentSummaryItems.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_data_to_export), Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val result = reportExporter.exportFinancialReport(
                currentDataItems,
                currentSummaryItems,
                format
            )

            withContext(Dispatchers.Main) {
                result.fold(
                    onSuccess = { exportResult ->
                        val shareIntent = reportExporter.createShareIntent(
                            exportResult.uri,
                            format
                        )
                        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_report)))
                    },
                    onFailure = { error ->
                        Toast.makeText(
                            this@LaporanActivity,
                            getString(R.string.export_failed) + ": ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        }
    }
     
     private fun setupSwipeRefresh() {
         binding.swipeRefreshLayout.setOnRefreshListener {
             viewModel.loadFinancialData()
         }
     }
    
    private fun observeFinancialState() {
        lifecycleScope.launch {
            viewModel.financialState.collectLatest { state ->
                when (state) {
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
                            
                            // Set data pemanfaatan pada adapter
                            adapter.setPemanfaatan(dataArray)
                            
                            // Calculate and set summary items with payment integration
                            calculateAndSetSummary(dataArray)
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
            currentDataItems = dataArray
            val totalIuranBulanan = com.example.iurankomplek.utils.FinancialCalculator.calculateTotalIuranBulanan(dataArray)
            val totalPengeluaran = com.example.iurankomplek.utils.FinancialCalculator.calculateTotalPengeluaran(dataArray)
            val totalIuranIndividu = com.example.iurankomplek.utils.FinancialCalculator.calculateTotalIuranIndividu(dataArray)
            val rekapIuran = com.example.iurankomplek.utils.FinancialCalculator.calculateRekapIuran(dataArray)

            if (!com.example.iurankomplek.utils.FinancialCalculator.validateFinancialCalculations(dataArray)) {
                Toast.makeText(this, getString(R.string.invalid_financial_data_detected), Toast.LENGTH_LONG).show()
                return
            }
            
            integratePaymentTransactions(
                dataArray,
                totalIuranBulanan,
                totalPengeluaran,
                totalIuranIndividu,
                rekapIuran
            )
            
            // Create summary items for the RecyclerView with security validation
            val summaryItems = listOf(
                LaporanSummaryItem(getString(R.string.jumlah_iuran_bulanan), DataValidator.formatCurrency(totalIuranBulanan)),
                LaporanSummaryItem(getString(R.string.total_pengeluaran), DataValidator.formatCurrency(totalPengeluaran)),
                LaporanSummaryItem(getString(R.string.rekap_total_iuran), DataValidator.formatCurrency(rekapIuran))
            )
            
            summaryAdapter.setItems(summaryItems)
        } catch (e: ArithmeticException) {
            Toast.makeText(this, getString(R.string.financial_calculation_overflow_error), Toast.LENGTH_LONG).show()
        } catch (e: IllegalArgumentException) {
            Toast.makeText(this, getString(R.string.invalid_financial_data_detected), Toast.LENGTH_LONG).show()
        }
    }
    
     private fun integratePaymentTransactions(
        validatedDataItems: List<com.example.iurankomplek.model.DataItem>,
        currentTotalIuranBulanan: Int,
        currentTotalPengeluaran: Int,
        currentTotalIuranIndividu: Int,
        currentRekapIuran: Int
    ) {
        // Fetch completed payment transactions from local database to integrate with financial reporting
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Get all completed payment transactions 
                val completedTransactions = transactionRepository.getTransactionsByStatus(
                    com.example.iurankomplek.payment.PaymentStatus.COMPLETED
                ).value
                
                // Calculate total amount from completed payments
                var paymentTotal = 0
                completedTransactions.forEach { transaction ->
                    paymentTotal += transaction.amount.toInt() // Convert BigDecimal to Int for consistency
                }
                
withContext(Dispatchers.Main) {
             if (completedTransactions.isNotEmpty()) {
                 val updatedRekapIuran = currentRekapIuran
                 val updatedSummaryItems = listOf(
                     LaporanSummaryItem(getString(R.string.jumlah_iuran_bulanan), DataValidator.formatCurrency(currentTotalIuranBulanan)),
                     LaporanSummaryItem(getString(R.string.total_pengeluaran), DataValidator.formatCurrency(currentTotalPengeluaran)),
                     LaporanSummaryItem(getString(R.string.rekap_total_iuran), DataValidator.formatCurrency(updatedRekapIuran)),
                     LaporanSummaryItem("Total Payments Processed", DataValidator.formatCurrency(paymentTotal))
                 )
                 currentSummaryItems = updatedSummaryItems
                 summaryAdapter.setItems(updatedSummaryItems)
                 
                 Toast.makeText(
                     this@LaporanActivity,
                     "Integrated ${completedTransactions.size} payment transactions (Total: ${DataValidator.formatCurrency(paymentTotal)})",
                     Toast.LENGTH_LONG
                 ).show()
             } else {
                 val originalSummaryItems = listOf(
                     LaporanSummaryItem(getString(R.string.jumlah_iuran_bulanan), DataValidator.formatCurrency(currentTotalIuranBulanan)),
                     LaporanSummaryItem(getString(R.string.total_pengeluaran), DataValidator.formatCurrency(currentTotalPengeluaran)),
                     LaporanSummaryItem(getString(R.string.rekap_total_iuran), DataValidator.formatCurrency(currentRekapIuran))
                 )
                 currentSummaryItems = originalSummaryItems
                 summaryAdapter.setItems(originalSummaryItems)
             }
         }
             } catch (e: Exception) {
                 withContext(Dispatchers.Main) {
                     Toast.makeText(
                         this@LaporanActivity,
                         "Error integrating payment data: ${e.message}",
                         Toast.LENGTH_LONG
                     ).show()
                 }
             }
        }
    }
    
    private fun initializeTransactionRepository() {
        val transactionDatabase = TransactionDatabase.getDatabase(this)
        val transactionDao = transactionDatabase.transactionDao()
        val mockPaymentGateway = MockPaymentGateway() // In production, this would be a real payment gateway
        transactionRepository = TransactionRepository(mockPaymentGateway, transactionDao)
    }
}