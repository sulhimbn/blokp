package com.example.iurankomplek
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.databinding.ItemListBinding
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.utils.ImageLoader
import com.example.iurankomplek.utils.DataValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserAdapter(private var users: MutableList<DataItem>):
    RecyclerView.Adapter<UserAdapter.ListViewHolder>(){
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }
    
    fun setUsers(newUsers: List<DataItem>) {
        GlobalScope.launch(Dispatchers.Default) {
            val diffCallback = UserDiffCallback(this@UserAdapter.users, newUsers)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            
            withContext(Dispatchers.Main) {
                this@UserAdapter.users.clear()
                this@UserAdapter.users.addAll(newUsers)
                diffResult.dispatchUpdatesTo(this@UserAdapter)
            }
        }
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
          
          // Load avatar image with proper caching and error handling using ImageLoader
          ImageLoader.loadCircularImage(
              context = holder.binding.root.context,
              imageView = holder.binding.itemAvatar,
              url = user.avatar
          )
          
          // Safely construct and display user name
          val userName = mutableListOf<String>().apply {
              if (user.first_name.isNotBlank()) add(DataValidator.sanitizeName(user.first_name))
              if (user.last_name.isNotBlank()) add(DataValidator.sanitizeName(user.last_name))
          }.joinToString(" ")
          holder.binding.itemName.text = userName.ifEmpty { "Unknown User" }
          
          // Safely display email
          holder.binding.itemEmail.text = user.email.takeIf { it.isNotBlank() } ?: "No email"
          
          // Safely display address
          holder.binding.itemAddress.text = user.alamat.takeIf { it.isNotBlank() } ?: "No address"
          
          // Safely display iuran perwarga with validation
          val iuranPerwargaValue = if (user.iuran_perwarga >= 0) user.iuran_perwarga else 0
          holder.binding.itemIuranPerwarga.text = "Iuran Perwarga ${DataValidator.formatCurrency(iuranPerwargaValue)}"
          
          // Safely display total iuran individu with validation
          val totalIuranIndividuValue = if (user.total_iuran_individu >= 0) user.total_iuran_individu else 0
          holder.binding.itemIuranIndividu.text = "Total Iuran Individu ${DataValidator.formatCurrency(totalIuranIndividuValue)}"
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