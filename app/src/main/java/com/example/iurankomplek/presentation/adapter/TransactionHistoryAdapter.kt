package com.example.iurankomplek.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.databinding.ItemTransactionHistoryBinding
import com.example.iurankomplek.payment.PaymentStatus
import com.example.iurankomplek.data.entity.Transaction
import java.text.NumberFormat
import java.util.Locale

class TransactionHistoryAdapter(
    private val onRefundRequested: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionHistoryAdapter.TransactionViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = GenericDiffUtil.byId<Transaction> { it.id }
        private val CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding, onRefundRequested)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
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
            val formattedAmount = CURRENCY_FORMATTER.format(transaction.amount.toDouble())
            binding.tvAmount.text = formattedAmount
            binding.tvDescription.text = transaction.description
            binding.tvDate.text = transaction.createdAt.toString()
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