package com.example.iurankomplek
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.example.iurankomplek.databinding.ItemPemanfaatanBinding
import com.example.iurankomplek.model.DataItem

class PemanfaatanAdapter(private var pemanfaatan: MutableList<DataItem>) :
    RecyclerView.Adapter<PemanfaatanAdapter.ListViewHolder>() {
    
    constructor() : this(mutableListOf())
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemPemanfaatanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }
    
    fun setPemanfaatan(dataItems: List<DataItem>) {
        val diffCallback = PemanfaatanDiffCallback(this.pemanfaatan, dataItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        this.pemanfaatan.clear()
        this.pemanfaatan.addAll(dataItems)
        diffResult.dispatchUpdatesTo(this)
    }
    
    override fun getItemCount(): Int = pemanfaatan.size
    
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val item = pemanfaatan[position]
        holder.binding.itemPemanfaatan.text = "-" + item.pemanfaatan_iuran + ":"
        holder.binding.itemDanaPemanfaatan.text = item.pengeluaran_iuran_warga.toString()
    }
    
    class ListViewHolder(val binding: ItemPemanfaatanBinding): RecyclerView.ViewHolder(binding.root)
    
    class PemanfaatanDiffCallback(
        private val oldList: List<DataItem>,
        private val newList: List<DataItem>
    ) : DiffUtil.Callback() {
        
        override fun getOldListSize(): Int = oldList.size
        
        override fun getNewListSize(): Int = newList.size
        
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // For pemanfaatan items, use pemanfaatan_iuran as it's likely to be unique for each expense
            return oldList[oldItemPosition].pemanfaatan_iuran == newList[newItemPosition].pemanfaatan_iuran
        }
        
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}