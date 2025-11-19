package com.example.iurankomplek
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.DataValidator
import com.example.iurankomplek.utils.NetworkUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LaporanActivity : AppCompatActivity() {
    private lateinit var adapter: PemanfaatanAdapter
    private lateinit var rv_laporan: RecyclerView
    private lateinit var IuranPerwargaTextView: TextView
    private lateinit var jumlahIuranBulananTextView: TextView
    private lateinit var totalIuranTextView: TextView
    private lateinit var pengeluaranTextView: TextView
    
    private var retryCount = 0
    private val maxRetries = 3
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_laporan)
        rv_laporan = findViewById(R.id.rv_laporan)
        jumlahIuranBulananTextView = findViewById(R.id.jumlahIuranBulananTextView)
        totalIuranTextView = findViewById(R.id.totalIuranTextView)
        pengeluaranTextView = findViewById(R.id.pengeluaranTextView)
        adapter = PemanfaatanAdapter(mutableListOf())
        rv_laporan.layoutManager = LinearLayoutManager(this)
        rv_laporan.adapter = adapter
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
                          
                           // Validate financial data to prevent calculations with invalid values
                           var totalIuranBulanan = 0
                           var totalPengeluaran = 0
                           var totalIuranIndividu = 0

                           for (dataItem in dataArray) {
                               // Validate that financial values are non-negative
                               if (dataItem.iuran_perwarga < 0 || dataItem.pengeluaran_iuran_warga < 0 || dataItem.total_iuran_individu < 0) {
                                   Toast.makeText(this@LaporanActivity, "Invalid financial data detected", Toast.LENGTH_LONG).show()
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
                           
                           jumlahIuranBulananTextView.text = "1. Jumlah Iuran Bulanan : ${DataValidator.formatCurrency(totalIuranBulanan)}"
                           pengeluaranTextView.text = "3. Total Pengeluaran : ${DataValidator.formatCurrency(totalPengeluaran)}"
                           totalIuranTextView.text = "4. Rekap Total Iuran : ${DataValidator.formatCurrency(rekapIuran)}"
                          // Set data pemanfaatan pada adapter
                          adapter.setPemanfaatan(dataArray)

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