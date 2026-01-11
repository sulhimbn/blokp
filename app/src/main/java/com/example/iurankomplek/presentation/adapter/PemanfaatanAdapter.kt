package com.example.iurankomplek.presentation.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.example.iurankomplek.databinding.ItemPemanfaatanBinding
import com.example.iurankomplek.data.dto.LegacyDataItemDto
import com.example.iurankomplek.utils.InputSanitizer

class PemanfaatanAdapter : BaseListAdapter<LegacyDataItemDto, PemanfaatanAdapter.ListViewHolder>(
    diffById { it.pemanfaatan_iuran }
) {

    companion object {
        private const val PEMANFAATAN_FORMAT = "-%s:"
    }

    override fun createViewHolderInternal(parent: ViewGroup): ListViewHolder {
        val binding = ItemPemanfaatanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun bindViewHolderInternal(holder: ListViewHolder, item: LegacyDataItemDto) {
        holder.bind(item)
    }

    class ListViewHolder(val binding: ItemPemanfaatanBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: LegacyDataItemDto) {
            val sanitizedPemanfaatan = InputSanitizer.sanitizePemanfaatan(item.pemanfaatan_iuran)
            binding.itemPemanfaatan.text = String.format(PEMANFAATAN_FORMAT, sanitizedPemanfaatan)
            binding.itemDanaPemanfaatan.text = InputSanitizer.formatCurrency(if (item.pengeluaran_iuran_warga >= 0) item.pengeluaran_iuran_warga else 0)
        }
    }
}
