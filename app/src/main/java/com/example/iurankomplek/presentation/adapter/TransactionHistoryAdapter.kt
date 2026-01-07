package com.example.iurankomplek.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.R
import com.example.iurankomplek.payment.MockPaymentGateway
import com.example.iurankomplek.payment.PaymentStatus
import com.example.iurankomplek.payment.RefundResponse
import com.example.iurankomplek.transaction.Transaction
import com.example.iurankomplek.transaction.TransactionDatabase
import com.example.iurankomplek.transaction.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class TransactionHistoryAdapter(
    private val coroutineScope: CoroutineScope
) : ListAdapter<Transaction, TransactionHistoryAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction_history, parent, false)
        return TransactionViewHolder(view, coroutineScope)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TransactionViewHolder(itemView: View, private val coroutineScope: CoroutineScope) : RecyclerView.ViewHolder(itemView) {
        private val tvAmount: TextView = itemView.findViewById(R.id.tv_amount)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_description)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_status)
        private val tvPaymentMethod: TextView = itemView.findViewById(R.id.tv_payment_method)
        private val btnRefund: Button = itemView.findViewById(R.id.btn_refund)

        fun bind(transaction: Transaction) {
            val context = itemView.context
            val formattedAmount = NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(transaction.amount.toDouble())
            tvAmount.text = formattedAmount
            tvDescription.text = transaction.description
            tvDate.text = transaction.createdAt.toString()
            tvStatus.text = transaction.status.name
            tvPaymentMethod.text = transaction.paymentMethod.name

            if (transaction.status == PaymentStatus.COMPLETED) {
                btnRefund.visibility = View.VISIBLE
                btnRefund.setOnClickListener {
                    val transactionRepository = com.example.iurankomplek.transaction.TransactionRepositoryFactory.getMockInstance(context)

                     coroutineScope.launch(Dispatchers.IO) {
                         val result = transactionRepository.refundPayment(transaction.id, "User requested refund")
                         if (result.isSuccess) {
                             runOnUiThread(context) {
                                 tvStatus.text = PaymentStatus.REFUNDED.name
                                 btnRefund.visibility = View.GONE
                                 Toast.makeText(context, context.getString(R.string.refund_processed_successfully), Toast.LENGTH_SHORT).show()
                             }
                         } else {
                             runOnUiThread(context) {
                                 Toast.makeText(context, context.getString(R.string.refund_failed, result.exceptionOrNull()?.message), Toast.LENGTH_LONG).show()
                             }
                         }
                     }
                }
            } else {
                btnRefund.visibility = View.GONE
            }
        }

        private fun runOnUiThread(context: android.content.Context, action: () -> Unit) {
            if (context is android.app.Activity) {
                context.runOnUiThread(action)
            }
        }
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}