package com.example.iurankomplek

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.ActivityLaporanBinding
import com.example.iurankomplek.model.LaporanSummaryItem
import com.example.iurankomplek.model.ValidatedDataItem
import com.example.iurankomplek.utils.DataValidator
import com.example.iurankomplek.utils.FinancialCalculator
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
                    
                     try {
                         // Validate the data array before processing to prevent potential security issues
                         val validatedData = dataArray.map { item ->
                             // Use ValidatedDataItem for enhanced validation
                             ValidatedDataItem.fromDataItem(item)
                         }
                         
                         // Convert back to regular DataItem for the FinancialCalculator
                         val validatedDataItems = validatedData.map { item ->
                             com.example.iurankomplek.model.DataItem(
                                 first_name = item.first_name,
                                 last_name = item.last_name,
                                 email = item.email,
                                 alamat = item.alamat,
                                 iuran_perwarga = item.iuran_perwarga,
                                 total_iuran_rekap = item.total_iuran_rekap,
                                 jumlah_iuran_bulanan = item.jumlah_iuran_bulanan,
                                 total_iuran_individu = item.total_iuran_individu,
                                 pengeluaran_iuran_warga = item.pengeluaran_iuran_warga,
                                 pemanfaatan_iuran = item.pemanfaatan_iuran,
                                 avatar = item.avatar
                             )
                         }
                         
                         // Use FinancialCalculator for all financial calculations
                         val totalIuranBulanan = FinancialCalculator.calculateTotalIuranBulanan(validatedDataItems)
                         val totalPengeluaran = FinancialCalculator.calculateTotalPengeluaran(validatedDataItems)
                         val totalIuranIndividu = FinancialCalculator.calculateTotalIuranIndividu(validatedDataItems)
                         val rekapIuran = FinancialCalculator.calculateRekapIuran(validatedDataItems)

                         // Validate financial calculations before displaying
                         if (!FinancialCalculator.validateFinancialCalculations(validatedDataItems)) {
                             Toast.makeText(this, getString(R.string.invalid_financial_data_detected), Toast.LENGTH_LONG).show()
                             return@let
                         }
                     } catch (e: ArithmeticException) {
                         Toast.makeText(this, getString(R.string.financial_calculation_overflow_error), Toast.LENGTH_LONG).show()
                         return@let
                     } catch (e: IllegalArgumentException) {
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
                     adapter.setPemanfaatan(validatedDataItems)
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