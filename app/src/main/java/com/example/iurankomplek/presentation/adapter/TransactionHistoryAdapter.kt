package com.example.iurankomplek.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.databinding.ItemTransactionHistoryBinding
import com.example.iurankomplek.payment.PaymentStatus
import com.example.iurankomplek.data.entity.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionHistoryAdapter(
    private val onRefundRequested: (Transaction) -> Unit
) : BaseListAdapter<Transaction, TransactionHistoryAdapter.TransactionViewHolder>(
    diffById { it.id }
) {

    companion object {
        private val CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        private val BD_HUNDRED = java.math.BigDecimal("100")
        private val DATE_FORMATTER = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US)
    }

    override fun createViewHolderInternal(parent: ViewGroup): TransactionViewHolder {
        val binding = ItemTransactionHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding, onRefundRequested)
    }

    override fun bindViewHolderInternal(holder: TransactionViewHolder, transaction: Transaction) {
        holder.bind(transaction)
    }

    class TransactionViewHolder(val binding: ItemTransactionHistoryBinding, private val onRefundRequested: (Transaction) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        private var currentTransaction: Transaction? = null

        init {
            binding.btnRefund.setOnClickListener {
                currentTransaction?.let { onRefundRequested(it) }
            }
        }

        fun bind(transaction: Transaction) {
            currentTransaction = transaction
            val amountInCurrency = java.math.BigDecimal(transaction.amount).divide(BD_HUNDRED, 2, java.math.RoundingMode.HALF_UP)
            val formattedAmount = CURRENCY_FORMATTER.format(amountInCurrency)
            binding.tvAmount.text = formattedAmount
            binding.tvDescription.text = transaction.description
            binding.tvDate.text = DATE_FORMATTER.format(transaction.createdAt)
            binding.tvStatus.text = transaction.status.name
            binding.tvPaymentMethod.text = transaction.paymentMethod.name

            if (transaction.status == PaymentStatus.COMPLETED) {
                binding.btnRefund.visibility = View.VISIBLE
            } else {
                binding.btnRefund.visibility = View.GONE
            }
        }
    }
}