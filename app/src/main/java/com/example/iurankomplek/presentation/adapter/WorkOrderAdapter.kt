package com.example.iurankomplek.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.databinding.ItemWorkOrderBinding
import com.example.iurankomplek.model.WorkOrder

class WorkOrderAdapter(
    private val onWorkOrderClick: (WorkOrder) -> Unit
) : ListAdapter<WorkOrder, WorkOrderAdapter.WorkOrderViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = GenericDiffUtil.byId<WorkOrder> { it.id }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkOrderViewHolder {
        val binding = ItemWorkOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WorkOrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkOrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WorkOrderViewHolder(val binding: ItemWorkOrderBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onWorkOrderClick(getItem(position))
                }
            }
        }

        fun bind(workOrder: WorkOrder) {
            binding.workOrderTitle.text = workOrder.title
            binding.workOrderCategory.text = workOrder.category
            binding.workOrderStatus.text = workOrder.status
            binding.workOrderPriority.text = workOrder.priority
        }
    }
}