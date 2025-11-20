import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.ActivityLaporanBinding
import com.example.iurankomplek.data.repository.PemanfaatanRepositoryImpl
import com.example.iurankomplek.model.LaporanSummaryItem
import com.example.iurankomplek.model.ValidatedDataItem
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.DataValidator
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.transaction.TransactionDatabase
import com.example.iurankomplek.transaction.TransactionRepository
import com.example.iurankomplek.payment.MockPaymentGateway
import com.example.iurankomplek.viewmodel.FinancialViewModel
import com.example.iurankomplek.viewmodel.FinancialViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LaporanActivity : BaseActivity() {
    private lateinit var adapter: PemanfaatanAdapter
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

        // Initialize ViewModel with repository
        val pemanfaatanRepository = PemanfaatanRepositoryImpl(ApiConfig.getApiService())
        viewModel = ViewModelProvider(this, FinancialViewModel.Factory(pemanfaatanRepository))[FinancialViewModel::class.java]

        adapter = PemanfaatanAdapter(mutableListOf())
        summaryAdapter = LaporanSummaryAdapter(mutableListOf())
        
        binding.rvLaporan.layoutManager = LinearLayoutManager(this)
        binding.rvLaporan.adapter = adapter
        
        binding.rvSummary.layoutManager = LinearLayoutManager(this)
        binding.rvSummary.adapter = summaryAdapter

        observeFinancialState()
        viewModel.loadFinancialData()
    }
    
    private fun observeFinancialState() {
        lifecycleScope.launch {
            viewModel.financialState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UiState.Success -> {
                        binding.progressBar.visibility = View.GONE
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
                        Toast.makeText(this@LaporanActivity, state.error, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
    
    private fun calculateAndSetSummary(dataArray: List<com.example.iurankomplek.model.DataItem>) {
        try {
            // Use FinancialCalculator for all financial calculations
            val totalIuranBulanan = com.example.iurankomplek.utils.FinancialCalculator.calculateTotalIuranBulanan(dataArray)
            val totalPengeluaran = com.example.iurankomplek.utils.FinancialCalculator.calculateTotalPengeluaran(dataArray)
            val totalIuranIndividu = com.example.iurankomplek.utils.FinancialCalculator.calculateTotalIuranIndividu(dataArray)
            val rekapIuran = com.example.iurankomplek.utils.FinancialCalculator.calculateRekapIuran(dataArray)

            // Validate financial calculations before displaying
            if (!com.example.iurankomplek.utils.FinancialCalculator.validateFinancialCalculations(dataArray)) {
                Toast.makeText(this, getString(R.string.invalid_financial_data_detected), Toast.LENGTH_LONG).show()
                return
            }
            
            // Integrate payment transaction data into financial calculations
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
        CoroutineScope(Dispatchers.IO).launch {
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
                
                runOnUiThread {
                    // If there are completed transactions, update the summary to show payment integration
                    if (completedTransactions.isNotEmpty()) {
                        // Update financial calculations to include actual payment data
                        val updatedTotalIuranBulanan = currentTotalIuranBulanan + paymentTotal
                        val updatedRekapIuran = updatedTotalIuranBulanan - currentTotalPengeluaran
                        
                        // Update summary with integrated data
                        val updatedSummaryItems = listOf(
                            LaporanSummaryItem(getString(R.string.jumlah_iuran_bulanan), DataValidator.formatCurrency(updatedTotalIuranBulanan)),
                            LaporanSummaryItem(getString(R.string.total_pengeluaran), DataValidator.formatCurrency(currentTotalPengeluaran)),
                            LaporanSummaryItem(getString(R.string.rekap_total_iuran), DataValidator.formatCurrency(updatedRekapIuran)),
                            LaporanSummaryItem("Total Payments Processed", DataValidator.formatCurrency(paymentTotal))
                        )
                        
                        summaryAdapter.setItems(updatedSummaryItems)
                        
                        Toast.makeText(
                            this@LaporanActivity,
                            "Integrated ${completedTransactions.size} payment transactions (+${DataValidator.formatCurrency(paymentTotal)})",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
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