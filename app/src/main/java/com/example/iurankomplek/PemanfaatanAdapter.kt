package com.example.iurankomplek
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.example.iurankomplek.databinding.ItemPemanfaatanBinding
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.utils.DataValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PemanfaatanAdapter(
    private var pemanfaatan: MutableList<DataItem>,
    private val coroutineScope: CoroutineScope
) : RecyclerView.Adapter<PemanfaatanAdapter.ListViewHolder>() {
    
    constructor() : this(mutableListOf(), CoroutineScope(Dispatchers.Default))
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemPemanfaatanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }
    
    fun setPemanfaatan(dataItems: List<DataItem>) {
        coroutineScope.launch(Dispatchers.Default) {
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
            // For pemanfaatan items, use pemanfaatan_iuran as it's likely to be unique for each expense
            return oldList[oldItemPosition].pemanfaatan_iuran == newList[newItemPosition].pemanfaatan_iuran
        }
        
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}