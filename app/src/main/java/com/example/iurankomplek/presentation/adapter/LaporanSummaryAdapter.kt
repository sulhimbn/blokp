package com.example.iurankomplek.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.databinding.ItemLaporanBinding

data class LaporanSummaryItem(
    val title: String,
    val value: String
)

class LaporanSummaryAdapter : ListAdapter<LaporanSummaryItem, LaporanSummaryAdapter.ListViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = GenericDiffUtil.byId<LaporanSummaryItem> { it.title }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemLaporanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun getItemCount(): Int = currentList.size

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.itemLaporanTitle.text = item.title
        holder.binding.itemLaporanValue.text = item.value
    }

    fun setItems(newItems: List<LaporanSummaryItem>) {
        submitList(newItems)
    }

    class ListViewHolder(val binding: ItemLaporanBinding) : RecyclerView.ViewHolder(binding.root)
}