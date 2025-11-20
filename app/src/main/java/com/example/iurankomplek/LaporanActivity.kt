package com.example.iurankomplek

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.ActivityLaporanBinding
import com.example.iurankomplek.model.LaporanSummaryItem
import com.example.iurankomplek.utils.DataValidator
import com.example.iurankomplek.network.ApiConfig

class LaporanActivity : BaseActivity() {
    private lateinit var adapter: PemanfaatanAdapter
    private lateinit var summaryAdapter: LaporanSummaryAdapter
    private lateinit var binding: ActivityLaporanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = PemanfaatanAdapter(mutableListOf())
        summaryAdapter = LaporanSummaryAdapter(mutableListOf())
        
        binding.rvLaporan.layoutManager = LinearLayoutManager(this)
        binding.rvLaporan.adapter = adapter
        
        binding.rvSummary.layoutManager = LinearLayoutManager(this)
        binding.rvSummary.adapter = summaryAdapter

        // Load financial data with retry logic from BaseActivity and progress indicator
        getPemanfaatan()
    }
    
    private fun getPemanfaatan() {
        // Show progress bar when starting the API call
        binding.progressBar.visibility = View.VISIBLE
        
        executeWithRetry(
            operation = { ApiConfig.getApiService().getPemanfaatan() },
            onSuccess = { response ->
                // Hide progress bar after successful response
                binding.progressBar.visibility = View.GONE
                
                response.data?.let { dataArray ->
                    if (dataArray.isEmpty()) {
                         Toast.makeText(this, getString(R.string.no_financial_data_available), Toast.LENGTH_LONG).show()
                        return@let
                    }
                    
                    // Validate the data array before processing to prevent potential security issues
                    val validatedData = dataArray.map { item ->
                        // Validate and sanitize data fields to prevent injection attacks
                        item.copy(
                            pemanfaatan_iuran = DataValidator.sanitizePemanfaatan(item.pemanfaatan_iuran)
                        )
                    }
                    
                    // Validate financial data to prevent calculations with invalid values
                    var totalIuranBulanan = 0
                    var totalPengeluaran = 0
                    var totalIuranIndividu = 0

                    for (dataItem in validatedData) {
                        // Validate that financial values are non-negative
                        if (dataItem.iuran_perwarga < 0 || dataItem.pengeluaran_iuran_warga < 0 || dataItem.total_iuran_individu < 0) {
                             Toast.makeText(this, getString(R.string.invalid_financial_data_detected), Toast.LENGTH_LONG).show()
                            return@let
                        }
                        
                         // Check for potential integer overflow before adding
                         if (totalIuranBulanan > Int.MAX_VALUE - dataItem.iuran_perwarga) {
                             Toast.makeText(this, getString(R.string.financial_data_exceeds_maximum), Toast.LENGTH_LONG).show()
                             return@let
                         }
                        
                         if (totalPengeluaran > Int.MAX_VALUE - dataItem.pengeluaran_iuran_warga) {
                             Toast.makeText(this, getString(R.string.financial_data_exceeds_maximum), Toast.LENGTH_LONG).show()
                             return@let
                         }
                        
                         // Check for potential overflow when multiplying by 3
                         if (dataItem.total_iuran_individu > Int.MAX_VALUE / 3) {
                             Toast.makeText(this, getString(R.string.financial_data_exceeds_maximum), Toast.LENGTH_LONG).show()
                             return@let
                         }
                        
                         if (totalIuranIndividu > Int.MAX_VALUE - (dataItem.total_iuran_individu * 3)) {
                             Toast.makeText(this, getString(R.string.financial_data_exceeds_maximum), Toast.LENGTH_LONG).show()
                             return@let
                         }
                        
                        totalIuranBulanan += dataItem.iuran_perwarga
                        totalPengeluaran += dataItem.pengeluaran_iuran_warga
                        totalIuranIndividu += dataItem.total_iuran_individu * 3
                    }

                    val rekapIuran = totalIuranIndividu - totalPengeluaran
                     // Validate financial calculations before displaying
                     if (totalIuranBulanan < 0 || totalPengeluaran < 0 || totalIuranIndividu < 0 || rekapIuran < 0) {
                         Toast.makeText(this, getString(R.string.invalid_financial_data_detected), Toast.LENGTH_LONG).show()
                         return@let
                     }
                    
                     // Create summary items for the RecyclerView with security validation
                     val summaryItems = listOf(
                         LaporanSummaryItem(getString(R.string.jumlah_iuran_bulanan), DataValidator.formatCurrency(totalIuranBulanan)),
                         LaporanSummaryItem(getString(R.string.total_pengeluaran), DataValidator.formatCurrency(totalPengeluaran)),
                         LaporanSummaryItem(getString(R.string.rekap_total_iuran), DataValidator.formatCurrency(rekapIuran))
                     )
                    
                    summaryAdapter.setItems(summaryItems)
                    // Set data pemanfaatan pada adapter
                    adapter.setPemanfaatan(validatedData)
                } ?: run {
                     Toast.makeText(this, getString(R.string.invalid_response_format), Toast.LENGTH_LONG).show()
                }
            },
            onError = { error ->
                // Hide progress bar after final failure
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        )
    }
}