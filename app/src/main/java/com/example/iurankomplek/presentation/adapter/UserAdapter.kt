package com.example.iurankomplek.presentation.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.databinding.ItemListBinding
import com.example.iurankomplek.data.dto.LegacyDataItemDto
import com.example.iurankomplek.utils.ImageLoader
import com.example.iurankomplek.utils.InputSanitizer

class UserAdapter : BaseListAdapter<LegacyDataItemDto, UserAdapter.ListViewHolder>(
    diffById { it.email }
) {

    companion object {
        private const val IURAN_PERWARGA_PREFIX = "Iuran Perwarga "
        private const val TOTAL_IURAN_INDIVIDU_PREFIX = "Total Iuran Individu "
    }

    override fun createViewHolderInternal(parent: ViewGroup): ListViewHolder {
        val binding = ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun bindViewHolderInternal(holder: ListViewHolder, user: LegacyDataItemDto) {
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
        holder.binding.itemIuranPerwarga.text = "$IURAN_PERWARGA_PREFIX${InputSanitizer.formatCurrency(iuranPerwargaValue)}"

        // Safely display total iuran individu with validation
        val totalIuranIndividuValue = if (user.total_iuran_individu >= 0) user.total_iuran_individu else 0
        holder.binding.itemIuranIndividu.text = "$TOTAL_IURAN_INDIVIDU_PREFIX${InputSanitizer.formatCurrency(totalIuranIndividuValue)}"
    }

    class ListViewHolder(val binding: ItemListBinding) : RecyclerView.ViewHolder(binding.root)
}