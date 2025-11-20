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
import com.example.iurankomplek.utils.DataValidator

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
        
        // Load avatar image with error handling and validation
        val avatarUrl = if (DataValidator.isValidUrl(user.avatar)) user.avatar else null
        Glide.with(holder.binding.root.context)
            .load(avatarUrl)
            .apply(RequestOptions().override(80, 80).placeholder(R.drawable.icon_avatar).error(R.drawable.icon_avatar))
            .transform(CircleCrop())
            .into(holder.binding.itemAvatar)
        
        // Safely construct and display user name using validator
        val userName = DataValidator.sanitizeName("${DataValidator.sanitizeName(user.first_name)} ${DataValidator.sanitizeName(user.last_name)}".trim())
        holder.binding.itemName.text = userName
        
        // Safely display email using validator
        holder.binding.itemEmail.text = DataValidator.sanitizeEmail(user.email)
        
        // Safely display address using validator
        holder.binding.itemAddress.text = DataValidator.sanitizeAddress(user.alamat)
        
        // Safely display iuran perwarga with validation
        val iuranPerwargaValue = DataValidator.formatCurrency(user.iuran_perwarga)
        holder.binding.itemIuranPerwarga.text = "Iuran Perwarga $iuranPerwargaValue"
        
        // Safely display total iuran individu with validation
        val totalIuranIndividuValue = DataValidator.formatCurrency(user.total_iuran_individu)
        holder.binding.itemIuranIndividu.text = "Total Iuran Individu $totalIuranIndividuValue"
    }
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