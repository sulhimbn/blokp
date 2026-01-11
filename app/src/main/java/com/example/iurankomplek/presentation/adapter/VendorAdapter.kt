package com.example.iurankomplek.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.databinding.ItemVendorBinding
import com.example.iurankomplek.model.Vendor

class VendorAdapter(
    private val onVendorClick: (Vendor) -> Unit
) : BaseListAdapter<Vendor, VendorAdapter.VendorViewHolder>(
    diffById { it.id }
) {

    companion object {
        private const val RATING_PREFIX = "Rating: "
        private const val RATING_SUFFIX = "/5.0"
    }

    override fun createViewHolderInternal(parent: ViewGroup): VendorViewHolder {
        val binding = ItemVendorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VendorViewHolder(binding, onVendorClick)
    }

    override fun bindViewHolderInternal(holder: VendorViewHolder, vendor: Vendor) {
        holder.bind(vendor)
    }

    inner class VendorViewHolder(
        val binding: ItemVendorBinding,
        private val onVendorClick: (Vendor) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

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