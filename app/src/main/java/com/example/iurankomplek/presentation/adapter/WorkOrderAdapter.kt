package com.example.iurankomplek.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.databinding.ItemWorkOrderBinding
import com.example.iurankomplek.model.WorkOrder

class WorkOrderAdapter(
    private val onWorkOrderClick: (WorkOrder) -> Unit
) : BaseListAdapter<WorkOrder, WorkOrderAdapter.WorkOrderViewHolder>(
    diffById { it.id }
) {

    override fun createViewHolderInternal(parent: ViewGroup): WorkOrderViewHolder {
        val binding = ItemWorkOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WorkOrderViewHolder(binding, onWorkOrderClick)
    }

    override fun bindViewHolderInternal(holder: WorkOrderViewHolder, workOrder: WorkOrder) {
        holder.bind(workOrder)
    }

    inner class WorkOrderViewHolder(
        val binding: ItemWorkOrderBinding,
        private val onWorkOrderClick: (WorkOrder) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onWorkOrderClick(getItemAt(position))
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