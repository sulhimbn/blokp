package com.example.iurankomplek.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

data class LaporanSummaryItem(
    val title: String,
    val value: String
)

class LaporanSummaryAdapter : ListAdapter<LaporanSummaryItem, LaporanSummaryAdapter.ListViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<LaporanSummaryItem>() {
            override fun areItemsTheSame(oldItem: LaporanSummaryItem, newItem: LaporanSummaryItem): Boolean {
                return oldItem.title == newItem.title
            }

            override fun areContentsTheSame(oldItem: LaporanSummaryItem, newItem: LaporanSummaryItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_laporan, parent, false)
        return ListViewHolder(view)
    }

    override fun getItemCount(): Int = currentList.size

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val item = getItem(position)
        holder.tvTitle.text = item.title
        holder.tvValue.text = item.value
    }

    fun setItems(newItems: List<LaporanSummaryItem>) {
        submitList(newItems)
    }

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.itemLaporanTitle)
        var tvValue: TextView = itemView.findViewById(R.id.itemLaporanValue)
    }
}