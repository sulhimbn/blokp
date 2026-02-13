package com.example.iurankomplek

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.ActivityDashboardBinding
import com.example.iurankomplek.model.DashboardData
import com.example.iurankomplek.model.PaymentStatus
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.viewmodel.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardActivity : BaseActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupQuickActions()
        setupSwipeRefresh()
        observeDashboardState()
    }

    private fun setupQuickActions() {
        binding.apply {
            btnViewUsers.setOnClickListener {
                startActivity(Intent(this@DashboardActivity, MainActivity::class.java))
            }
            
            btnViewReports.setOnClickListener {
                startActivity(Intent(this@DashboardActivity, LaporanActivity::class.java))
            }
            
            btnMakePayment.setOnClickListener {
                startActivity(Intent(this@DashboardActivity, PaymentActivity::class.java))
            }
            
            btnViewAnnouncements.setOnClickListener {
                startActivity(Intent(this@DashboardActivity, CommunicationActivity::class.java))
            }
            
            btnViewTransactions.setOnClickListener {
                startActivity(Intent(this@DashboardActivity, TransactionHistoryActivity::class.java))
            }
            
            btnViewVendors.setOnClickListener {
                startActivity(Intent(this@DashboardActivity, VendorManagementActivity::class.java))
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshDashboard()
        }
    }

    private fun observeDashboardState() {
        lifecycleScope.launch {
            viewModel.dashboardState.collectLatest { state ->
                when (state) {
                    is UiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.swipeRefreshLayout.isRefreshing = true
                    }
                    is UiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.swipeRefreshLayout.isRefreshing = false
                        updateDashboardUI(state.data)
                    }
                    is UiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.swipeRefreshLayout.isRefreshing = false
                        Toast.makeText(this@DashboardActivity, state.error, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun updateDashboardUI(data: DashboardData) {
        binding.apply {
            val summary = data.financialSummary
            
            tvTotalDue.text = viewModel.formatCurrency(summary.totalDue)
            tvTotalCollected.text = viewModel.formatCurrency(summary.totalCollected)
            tvTotalExpenses.text = viewModel.formatCurrency(summary.totalExpenses)
            tvBalance.text = viewModel.formatCurrency(summary.balance)
            
            tvPaymentStatus.text = when (summary.paymentStatus) {
                PaymentStatus.EXCELLENT -> "Excellent"
                PaymentStatus.GOOD -> "Good"
                PaymentStatus.FAIR -> "Fair"
                PaymentStatus.POOR -> "Poor"
            }
            
            val statusColor = when (summary.paymentStatus) {
                PaymentStatus.EXCELLENT -> android.R.color.holo_green_dark
                PaymentStatus.GOOD -> android.R.color.holo_green_light
                PaymentStatus.FAIR -> android.R.color.holo_orange_light
                PaymentStatus.POOR -> android.R.color.holo_red_light
            }
            tvPaymentStatus.setTextColor(resources.getColor(statusColor, theme))
            
            tvResidentStats.text = "${summary.paidResidents}/${summary.totalResidents} Residents Paid"
            
            tvUnreadAnnouncements.text = data.unreadAnnouncements.toString()
            tvUnreadMessages.text = data.unreadMessages.toString()
            
            val syncTime = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(data.lastSyncTime))
            tvLastSync.text = "Last synced: $syncTime"
        }
    }
}
