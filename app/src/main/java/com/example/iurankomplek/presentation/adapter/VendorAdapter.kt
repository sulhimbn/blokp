package com.example.iurankomplek.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.databinding.ItemVendorBinding
import com.example.iurankomplek.model.Vendor

class VendorAdapter(
    private val onVendorClick: (Vendor) -> Unit
) : ListAdapter<Vendor, VendorAdapter.VendorViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = GenericDiffUtil.byId<Vendor> { it.id }
        private const val RATING_PREFIX = "Rating: "
        private const val RATING_SUFFIX = "/5.0"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VendorViewHolder {
        val binding = ItemVendorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VendorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VendorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VendorViewHolder(val binding: ItemVendorBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onVendorClick(getItem(position))
                }
            }
        }

        fun bind(vendor: Vendor) {
            binding.vendorName.text = vendor.name
            binding.vendorSpecialty.text = vendor.specialty
            binding.vendorContact.text = vendor.phoneNumber
            binding.vendorRating.text = "$RATING_PREFIX${vendor.rating}$RATING_SUFFIX"
        }
    }
}