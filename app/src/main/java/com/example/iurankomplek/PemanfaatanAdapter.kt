package com.example.iurankomplek
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.utils.DataValidator

class PemanfaatanAdapter(private var pemanfaatan: MutableList<DataItem>) :
    RecyclerView.Adapter<PemanfaatanAdapter.ListViewHolder>() {
    
    constructor() : this(mutableListOf())
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_pemanfaatan, parent, false)
        return ListViewHolder(view)
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
        holder.tvPemanfaatan.text = "-" + DataValidator.sanitizePemanfaatan(item.pemanfaatan_iuran) + ":"
        holder.tvTotalIuranRekap.text = DataValidator.formatCurrency(item.pengeluaran_iuran_warga)
    }
    
    class ListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var tvPemanfaatan: TextView = itemView.findViewById(R.id.itemPemanfaatan)
        var tvTotalIuranRekap: TextView = itemView.findViewById(R.id.itemDanaPemanfaatan)
    }
    
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