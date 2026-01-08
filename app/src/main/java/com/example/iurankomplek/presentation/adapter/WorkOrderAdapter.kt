package com.example.iurankomplek.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.R
import com.example.iurankomplek.model.WorkOrder

class WorkOrderAdapter(
    private val onWorkOrderClick: (WorkOrder) -> Unit
) : ListAdapter<WorkOrder, WorkOrderAdapter.WorkOrderViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = GenericDiffUtil.byId<WorkOrder> { it.id }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkOrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_work_order, parent, false)
        return WorkOrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkOrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WorkOrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.workOrderTitle)
        private val categoryTextView: TextView = itemView.findViewById(R.id.workOrderCategory)
        private val statusTextView: TextView = itemView.findViewById(R.id.workOrderStatus)
        private val priorityTextView: TextView = itemView.findViewById(R.id.workOrderPriority)

        fun bind(workOrder: WorkOrder) {
            titleTextView.text = workOrder.title
            categoryTextView.text = workOrder.category
            statusTextView.text = workOrder.status
            priorityTextView.text = workOrder.priority

            itemView.setOnClickListener {
                onWorkOrderClick(workOrder)
            }
        }
    }
}