package com.example.iurankomplek.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.databinding.ItemLaporanBinding

data class LaporanSummaryItem(
    val title: String,
    val value: String
)

class LaporanSummaryAdapter : BaseListAdapter<LaporanSummaryItem, LaporanSummaryAdapter.ListViewHolder>(
    diffById { it.title }
) {

    override fun getItemCount(): Int = currentList.size

    override fun createViewHolderInternal(parent: ViewGroup): ListViewHolder {
        val binding = ItemLaporanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun bindViewHolderInternal(holder: ListViewHolder, item: LaporanSummaryItem) {
        holder.binding.itemLaporanTitle.text = item.title
        holder.binding.itemLaporanValue.text = item.value
    }

    fun setItems(newItems: List<LaporanSummaryItem>) {
        submitList(newItems)
    }

    class ListViewHolder(val binding: ItemLaporanBinding) : RecyclerView.ViewHolder(binding.root)
}