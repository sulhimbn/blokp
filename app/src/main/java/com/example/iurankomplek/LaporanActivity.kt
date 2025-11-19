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
import com.example.iurankomplek.utils.NetworkUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LaporanActivity : BaseActivity() {
    private lateinit var adapter: PemanfaatanAdapter
    private lateinit var rv_laporan: RecyclerView
    private lateinit var IuranPerwargaTextView: TextView
    private lateinit var jumlahIuranBulananTextView: TextView
    private lateinit var totalIuranTextView: TextView
    private lateinit var pengeluaranTextView: TextView
    
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
    
    private fun getPemanfaatan() {
        executeWithRetry(
            operation = { ApiConfig.getApiService().getPemanfaatan() },
            onSuccess = { response ->
                response.data?.let { dataArray ->
                    if (dataArray.isEmpty()) {
                        Toast.makeText(this, "No financial data available", Toast.LENGTH_LONG).show()
                        return@let
                    }
                    
                    // Validate financial data to prevent calculations with invalid values
                    var totalIuranBulanan = 0
                    var totalPengeluaran = 0
                    var totalIuranIndividu = 0

                    for (dataItem in dataArray) {
                        // Validate that financial values are non-negative
                        if (dataItem.iuran_perwarga < 0 || dataItem.pengeluaran_iuran_warga < 0 || dataItem.total_iuran_individu < 0) {
                            Toast.makeText(this, "Invalid financial data detected", Toast.LENGTH_LONG).show()
                            return@let
                        }
                        
                        totalIuranBulanan += dataItem.iuran_perwarga
                        totalPengeluaran += dataItem.pengeluaran_iuran_warga
                        totalIuranIndividu += dataItem.total_iuran_individu * 3
                    }

                    val rekapIuran = totalIuranIndividu - totalPengeluaran
                    jumlahIuranBulananTextView.text = "1. Jumlah Iuran Bulanan : $totalIuranBulanan"
                    pengeluaranTextView.text = "3. Total Pengeluaran : $totalPengeluaran"
                    totalIuranTextView.text = "4. Rekap Total Iuran : $rekapIuran"
                    // Set data pemanfaatan pada adapter
                    adapter.setPemanfaatan(dataArray)
                } ?: run {
                    Toast.makeText(this, "Invalid response format", Toast.LENGTH_LONG).show()
                }
            },
            onError = { error ->
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        )
    }
}