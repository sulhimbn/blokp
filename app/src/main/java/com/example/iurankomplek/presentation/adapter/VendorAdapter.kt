package com.example.iurankomplek.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.model.Vendor

class VendorAdapter(
    private val onVendorClick: (Vendor) -> Unit
) : ListAdapter<Vendor, VendorAdapter.VendorViewHolder>(VendorDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VendorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vendor, parent, false)
        return VendorViewHolder(view)
    }

    override fun onBindViewHolder(holder: VendorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VendorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.vendorName)
        private val specialtyTextView: TextView = itemView.findViewById(R.id.vendorSpecialty)
        private val contactTextView: TextView = itemView.findViewById(R.id.vendorContact)
        private val ratingTextView: TextView = itemView.findViewById(R.id.vendorRating)

        fun bind(vendor: Vendor) {
            nameTextView.text = vendor.name
            specialtyTextView.text = vendor.specialty
            contactTextView.text = vendor.phoneNumber
            ratingTextView.text = "Rating: ${vendor.rating}/5.0"

            itemView.setOnClickListener {
                onVendorClick(vendor)
            }
        }
    }

    class VendorDiffCallback : DiffUtil.ItemCallback<Vendor>() {
        override fun areItemsTheSame(oldItem: Vendor, newItem: Vendor): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Vendor, newItem: Vendor): Boolean {
            return oldItem == newItem
        }
    }
}