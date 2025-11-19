package com.example.iurankomplek
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.iurankomplek.databinding.ItemListBinding
import com.example.iurankomplek.model.DataItem

class UserAdapter(private var users: MutableList<DataItem>):
    RecyclerView.Adapter<UserAdapter.ListViewHolder>(){
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }
    
    fun setUsers(newUsers: List<DataItem>) {
        val diffCallback = UserDiffCallback(this.users, newUsers)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        this.users.clear()
        this.users.addAll(newUsers)
        diffResult.dispatchUpdatesTo(this)
    }
    
     fun addUser(newUser: DataItem?) {
         newUser?.let { user ->
             // Validate required fields before adding to prevent null values
             if (user.email.isNotBlank() && (user.first_name.isNotBlank() || user.last_name.isNotBlank())) {
                 users.add(user)
                 notifyItemInserted(users.lastIndex)
             }
         }
     }
    
    fun clear(){
        val size = users.size
        users.clear()
        notifyItemRangeRemoved(0, size)
    }
    
    override fun getItemCount(): Int = users.size
    
    override fun onBindViewHolder(holder: ListViewHolder, position: Int){
        val user = users[position]
        
        // Load avatar image with error handling
        val avatarUrl = user.avatar.takeIf { it.isNotBlank() }
        Glide.with(holder.binding.root.context)
            .load(avatarUrl)
            .apply(RequestOptions().override(80, 80).placeholder(R.drawable.icon_avatar).error(R.drawable.icon_avatar))
            .transform(CircleCrop())
            .into(holder.binding.itemAvatar)
        
        // Safely construct and display user name
        val userName = mutableListOf<String>().apply {
            if (user.first_name.isNotBlank()) add(user.first_name)
            if (user.last_name.isNotBlank()) add(user.last_name)
        }.joinToString(" ")
        holder.binding.itemName.text = userName.ifEmpty { "Unknown User" }
        
        // Safely display email
        holder.binding.itemEmail.text = user.email.takeIf { it.isNotBlank() } ?: "No email"
        
        // Safely display address
        holder.binding.itemAddress.text = user.alamat.takeIf { it.isNotBlank() } ?: "No address"
        
        // Safely display iuran perwarga with validation
        val iuranPerwargaValue = if (user.iuran_perwarga >= 0) user.iuran_perwarga else 0
        holder.binding.itemIuranPerwarga.text = "Iuran Perwarga Rp.$iuranPerwargaValue"
        
        // Safely display total iuran individu with validation
        val totalIuranIndividuValue = if (user.total_iuran_individu >= 0) user.total_iuran_individu else 0
        holder.binding.itemIuranIndividu.text = "Total Iuran Individu Rp.$totalIuranIndividuValue"
    }

    class ListViewHolder(val binding: ItemListBinding): RecyclerView.ViewHolder(binding.root)
    
    class UserDiffCallback(
        private val oldList: List<DataItem>,
        private val newList: List<DataItem>
    ) : DiffUtil.Callback() {
        
        override fun getOldListSize(): Int = oldList.size
        
        override fun getNewListSize(): Int = newList.size
        
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // Since there's no explicit ID, use a combination of fields that would uniquely identify a user
            // Using email as it's typically unique, or a combination of name and address
            return oldList[oldItemPosition].email == newList[newItemPosition].email
        }
        
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}