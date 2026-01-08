package com.example.iurankomplek.presentation.adapter
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.databinding.ItemListBinding
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.utils.ImageLoader
import com.example.iurankomplek.utils.InputSanitizer

class UserAdapter : ListAdapter<DataItem, UserAdapter.ListViewHolder>(UserDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int){
        val user = getItem(position)

        // Load avatar image with proper caching and error handling using ImageLoader
        ImageLoader.loadCircularImage(
            context = holder.binding.root.context,
            imageView = holder.binding.itemAvatar,
            url = user.avatar
        )

        // Safely construct and display user name (optimized: no list allocation)
        val firstName = InputSanitizer.sanitizeName(user.first_name).takeIf { it.isNotBlank() }
        val lastName = InputSanitizer.sanitizeName(user.last_name).takeIf { it.isNotBlank() }
        holder.binding.itemName.text = when {
            firstName != null && lastName != null -> "$firstName $lastName"
            firstName != null -> firstName
            lastName != null -> lastName
            else -> "Unknown User"
        }

        // Safely display email
        holder.binding.itemEmail.text = user.email.takeIf { it.isNotBlank() } ?: "No email"

        // Safely display address
        holder.binding.itemAddress.text = user.alamat.takeIf { it.isNotBlank() } ?: "No address"

        // Safely display iuran perwarga with validation
        val iuranPerwargaValue = if (user.iuran_perwarga >= 0) user.iuran_perwarga else 0
        holder.binding.itemIuranPerwarga.text = "Iuran Perwarga ${InputSanitizer.formatCurrency(iuranPerwargaValue)}"

        // Safely display total iuran individu with validation
        val totalIuranIndividuValue = if (user.total_iuran_individu >= 0) user.total_iuran_individu else 0
        holder.binding.itemIuranIndividu.text = "Total Iuran Individu ${InputSanitizer.formatCurrency(totalIuranIndividuValue)}"
    }

    class ListViewHolder(val binding: ItemListBinding): RecyclerView.ViewHolder(binding.root){
        val tvUserName: TextView
            get() = binding.itemName
        val tvEmail: TextView
            get() = binding.itemEmail
        val tvAvatar: ImageView
            get() = binding.itemAvatar
        val tvAddress: TextView
            get() = binding.itemAddress
        val tvIuranPerwarga: TextView
            get() = binding.itemIuranPerwarga
        val tvIuranIndividu: TextView
            get() = binding.itemIuranIndividu
    }

    object UserDiffCallback : DiffUtil.ItemCallback<DataItem>() {
        override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            // Since there's no explicit ID, use a combination of fields that would uniquely identify a user
            // Using email as it's typically unique, or a combination of name and address
            return oldItem.email == newItem.email
        }

        override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem == newItem
        }
    }
}