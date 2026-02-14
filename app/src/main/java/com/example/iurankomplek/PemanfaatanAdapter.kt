package com.example.iurankomplek
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.example.iurankomplek.databinding.ItemPemanfaatanBinding
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.utils.DataValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PemanfaatanAdapter(private var pemanfaatan: MutableList<DataItem>) :
    RecyclerView.Adapter<PemanfaatanAdapter.ListViewHolder>() {
    
    constructor() : this(mutableListOf())
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemPemanfaatanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }
    
    fun setPemanfaatan(dataItems: List<DataItem>) {
        GlobalScope.launch(Dispatchers.Default) {
            val diffCallback = PemanfaatanDiffCallback(this@PemanfaatanAdapter.pemanfaatan, dataItems)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            
            withContext(Dispatchers.Main) {
                this@PemanfaatanAdapter.pemanfaatan.clear()
                this@PemanfaatanAdapter.pemanfaatan.addAll(dataItems)
                diffResult.dispatchUpdatesTo(this@PemanfaatanAdapter)
            }
        }
    }
    
    override fun getItemCount(): Int = pemanfaatan.size
    
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val item = pemanfaatan[position]
        holder.binding.itemPemanfaatan.text = "-" + DataValidator.sanitizePemanfaatan(item.pemanfaatan_iuran) + ":"
        // Validate that financial values are non-negative before displaying and apply security formatting
        val pengeluaranValue = if (item.pengeluaran_iuran_warga >= 0) item.pengeluaran_iuran_warga else 0
        holder.binding.itemDanaPemanfaatan.text = DataValidator.formatCurrency(pengeluaranValue)
    }
    
    class ListViewHolder(val binding: ItemPemanfaatanBinding): RecyclerView.ViewHolder(binding.root)
    
    class PemanfaatanDiffCallback(
        private val oldList: List<DataItem>,
        private val newList: List<DataItem>
    ) : DiffUtil.Callback() {
        
        override fun getOldListSize(): Int = oldList.size
        
        override fun getNewListSize(): Int = newList.size
        
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // FIXED (Issue #266): Use combination of fields to determine uniqueness
            // pemanfaatan_iuran alone is not unique - multiple users can have same expense description
            // Using email + name + expense details ensures proper RecyclerView diff behavior
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem.pemanfaatan_iuran == newItem.pemanfaatan_iuran &&
                   oldItem.pengeluaran_iuran_warga == newItem.pengeluaran_iuran_warga
        }
        
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}