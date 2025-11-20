package com.example.iurankomplek
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.databinding.ActivityLaporanBinding
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.DataValidator
import com.example.iurankomplek.utils.NetworkUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LaporanActivity : AppCompatActivity() {
    private lateinit var adapter: PemanfaatanAdapter
    private lateinit var summaryAdapter: LaporanSummaryAdapter
    private lateinit var binding: ActivityLaporanBinding
    
    private var retryCount = 0
    private val maxRetries = 3
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
        getPemanfaatan()
    }
    private fun getPemanfaatan(currentRetryCount: Int = 0) {
        // Check network connectivity before making API call
        if (!NetworkUtils.isNetworkAvailable(this)) {
            if (currentRetryCount == 0) {
                Toast.makeText(this, "No internet connection. Please check your network settings.", Toast.LENGTH_LONG).show()
            }
            return
        }
        
        val apiService = ApiConfig.getApiService()
        val client = apiService.getPemanfaatan()
         client.enqueue(object : Callback<PemanfaatanResponse> {
             override fun onResponse(call: Call<PemanfaatanResponse>, response: Response<PemanfaatanResponse>) {
                  if (response.isSuccessful) {
                      val responseBody = response.body()
                      if (responseBody != null && responseBody.data != null) {
                           val dataArray = responseBody.data
                           
                           if (dataArray.isEmpty()) {
                               Toast.makeText(this@LaporanActivity, "No financial data available", Toast.LENGTH_LONG).show()
                               return
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
                                    Toast.makeText(this@LaporanActivity, "Invalid financial data detected", Toast.LENGTH_LONG).show()
                                    return
                                }
                                
                                // Check for potential integer overflow before adding
                                if (totalIuranBulanan > Int.MAX_VALUE - dataItem.iuran_perwarga) {
                                    Toast.makeText(this@LaporanActivity, "Financial data exceeds maximum allowed value", Toast.LENGTH_LONG).show()
                                    return
                                }
                                
                                if (totalPengeluaran > Int.MAX_VALUE - dataItem.pengeluaran_iuran_warga) {
                                    Toast.makeText(this@LaporanActivity, "Financial data exceeds maximum allowed value", Toast.LENGTH_LONG).show()
                                    return
                                }
                                
                                // Check for potential overflow when multiplying by 3
                                if (dataItem.total_iuran_individu > Int.MAX_VALUE / 3) {
                                    Toast.makeText(this@LaporanActivity, "Financial data exceeds maximum allowed value", Toast.LENGTH_LONG).show()
                                    return
                                }
                                
                                if (totalIuranIndividu > Int.MAX_VALUE - (dataItem.total_iuran_individu * 3)) {
                                    Toast.makeText(this@LaporanActivity, "Financial data exceeds maximum allowed value", Toast.LENGTH_LONG).show()
                                    return
                                }
                                
                                totalIuranBulanan += dataItem.iuran_perwarga
                                totalPengeluaran += dataItem.pengeluaran_iuran_warga
                                totalIuranIndividu += dataItem.total_iuran_individu * 3
                            }

                           val rekapIuran = totalIuranIndividu - totalPengeluaran
                           // Validate financial calculations before displaying
                           if (totalIuranBulanan < 0 || totalPengeluaran < 0 || totalIuranIndividu < 0 || rekapIuran < 0) {
                               Toast.makeText(this@LaporanActivity, "Invalid financial data detected", Toast.LENGTH_LONG).show()
                               return
                           }
                           
                            // Create summary items for the RecyclerView with security validation
                            val summaryItems = listOf(
                                LaporanSummaryItem("1. Jumlah Iuran Bulanan", DataValidator.formatCurrency(totalIuranBulanan)),
                                LaporanSummaryItem("3. Total Pengeluaran", DataValidator.formatCurrency(totalPengeluaran)),
                                LaporanSummaryItem("4. Rekap Total Iuran", DataValidator.formatCurrency(rekapIuran))
                            )
                            
                            summaryAdapter.setItems(summaryItems)
                            // Set data pemanfaatan pada adapter
                            adapter.setPemanfaatan(validatedData)
                      } else {
                          Toast.makeText(this@LaporanActivity, "Invalid response format", Toast.LENGTH_LONG).show()
                      }
                  } else {
                      if (currentRetryCount < maxRetries) {
                          Handler(Looper.getMainLooper()).postDelayed({
                              getPemanfaatan(currentRetryCount + 1)
                          }, 1000L * (currentRetryCount + 1)) // Exponential backoff
                      } else {
                          Toast.makeText(this@LaporanActivity, "Failed to retrieve data after ${maxRetries + 1} attempts. Please check your connection and try again.", Toast.LENGTH_LONG).show()
                      }
                  }
             }
            override fun onFailure(call: Call<PemanfaatanResponse>, t: Throwable) {
                if (currentRetryCount < maxRetries) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        getPemanfaatan(currentRetryCount + 1)
                    }, 1000L * (currentRetryCount + 1)) // Exponential backoff
                } else {
                    Toast.makeText(this@LaporanActivity, "Network error: ${t.message}. Failed after ${maxRetries + 1} attempts. Please check your connection and try again.", Toast.LENGTH_LONG).show()
                    t.printStackTrace()
                }
            }
        })
    }
}