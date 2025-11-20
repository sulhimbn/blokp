package com.example.iurankomplek

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

data class LaporanSummaryItem(
    val title: String,
    val value: String
)

class LaporanSummaryAdapter(private var items: MutableList<LaporanSummaryItem>) :
    RecyclerView.Adapter<LaporanSummaryAdapter.ListViewHolder>() {

    constructor() : this(mutableListOf())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_laporan, parent, false)
        return ListViewHolder(view)
    }

    fun setItems(newItems: List<LaporanSummaryItem>) {
        val diffCallback = LaporanSummaryDiffCallback(this.items, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        this.items.clear()
        this.items.addAll(newItems)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.tvValue.text = item.value
    }

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.itemLaporanTitle)
        var tvValue: TextView = itemView.findViewById(R.id.itemLaporanValue)
    }

    class LaporanSummaryDiffCallback(
        private val oldList: List<LaporanSummaryItem>,
        private val newList: List<LaporanSummaryItem>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].title == newList[newItemPosition].title
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}