package com.example.iurankomplek.presentation.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.example.iurankomplek.databinding.ItemPemanfaatanBinding
import com.example.iurankomplek.data.dto.LegacyDataItemDto
import com.example.iurankomplek.utils.InputSanitizer

class PemanfaatanAdapter : ListAdapter<LegacyDataItemDto, PemanfaatanAdapter.ListViewHolder>(PemanfaatanDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemPemanfaatanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.itemPemanfaatan.text = "-" + InputSanitizer.sanitizePemanfaatan(item.pemanfaatan_iuran) + ":"
        // Validate that financial values are non-negative before displaying and apply security formatting
        val pengeluaranValue = if (item.pengeluaran_iuran_warga >= 0) item.pengeluaran_iuran_warga else 0
        holder.binding.itemDanaPemanfaatan.text = InputSanitizer.formatCurrency(pengeluaranValue)
    }
    
    class ListViewHolder(val binding: ItemPemanfaatanBinding): RecyclerView.ViewHolder(binding.root)

    object PemanfaatanDiffCallback : DiffUtil.ItemCallback<LegacyDataItemDto>() {
        override fun areItemsTheSame(oldItem: LegacyDataItemDto, newItem: LegacyDataItemDto): Boolean {
            // For pemanfaatan items, use pemanfaatan_iuran as it's likely to be unique for each expense
            return oldItem.pemanfaatan_iuran == newItem.pemanfaatan_iuran
        }

        override fun areContentsTheSame(oldItem: LegacyDataItemDto, newItem: LegacyDataItemDto): Boolean {
            return oldItem == newItem
        }
    }
}