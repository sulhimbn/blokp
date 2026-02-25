package com.example.iurankomplek
import com.example.iurankomplek.utils.Constants

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
import com.example.iurankomplek.viewmodel.FinancialDataState
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
    private lateinit var reportExporter: ReportExporter
    private var currentDataItems: List<DataItem> = emptyList()
    private var currentSummaryItems: List<LaporanSummaryItem> = emptyList()
    private var lastExportResult: ReportExporter.ExportResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        reportExporter = ReportExporter(this)

        adapter = PemanfaatkanAdapter(mutableListOf(), lifecycleScope)
        summaryAdapter = LaporanSummaryAdapter()
        
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
            Toast.makeText(this, getString(R.string.no_data_to_export), Constants.Toast.DURATION_LONG).show()
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
                            Constants.Toast.DURATION_LONG
                        ).show()
                    },
                    onFailure = { error ->
                        Toast.makeText(
                            this@LaporanActivity,
                            getString(R.string.export_failed) + ": ${error.message}",
                            Constants.Toast.DURATION_LONG
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
            Toast.makeText(this, getString(R.string.no_data_to_export), Constants.Toast.DURATION_LONG).show()
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
                            Constants.Toast.DURATION_LONG
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
                    is FinancialDataState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.swipeRefreshLayout.isRefreshing = true
                    }
                    is FinancialDataState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.swipeRefreshLayout.isRefreshing = false
                        
                        state.response.data.let { dataArray ->
                            if (dataArray.isEmpty()) {
                                Toast.makeText(this@LaporanActivity, getString(R.string.no_financial_data_available), Constants.Toast.DURATION_LONG).show()
                                return@let
                            }
                            
                            // Set data pemanfaatan pada adapter
                            adapter.setPemanfaatan(dataArray)
                            currentDataItems = dataArray
                            
                            // Use summary from ViewModel (includes payment integration)
                            val summary = state.summary
                            
                            if (!summary.isValid) {
                                Toast.makeText(this@LaporanActivity, getString(R.string.invalid_financial_data_detected), Constants.Toast.DURATION_LONG).show()
                                return@let
                            }
                            
                            // Build summary items from ViewModel state
                            val summaryItems = mutableListOf(
                                LaporanSummaryItem(getString(R.string.jumlah_iuran_bulanan), DataValidator.formatCurrency(summary.totalIuranBulanan)),
                                LaporanSummaryItem(getString(R.string.total_pengeluaran), DataValidator.formatCurrency(summary.totalPengeluaran)),
                                LaporanSummaryItem(getString(R.string.rekap_total_iuran), DataValidator.formatCurrency(summary.rekapIuran))
                            )
                            
                            // Add payment data if available
                            if (summary.completedTransactionsCount > 0) {
                                summaryItems.add(
                                    LaporanSummaryItem(
                                        "Total Payments Processed",
                                        DataValidator.formatCurrency(summary.totalPaymentsProcessed)
                                    )
                                )
                                Toast.makeText(
                                    this@LaporanActivity,
                                    "Integrated ${summary.completedTransactionsCount} payment transactions (Total: ${DataValidator.formatCurrency(summary.totalPaymentsProcessed)})",
                                    Constants.Toast.DURATION_LONG
                                ).show()
                            }
                            
                            currentSummaryItems = summaryItems
                            summaryAdapter.setItems(summaryItems)
                        }
                    }
                    is FinancialDataState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.swipeRefreshLayout.isRefreshing = false
                        Toast.makeText(this@LaporanActivity, state.message, Constants.Toast.DURATION_LONG).show()
                    }
                }
            }
        }
    }
}
