package com.example.iurankomplek.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.model.CommunityPost

class CommunityPostAdapter : ListAdapter<CommunityPost, CommunityPostAdapter.CommunityPostViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<CommunityPost>() {
            override fun areItemsTheSame(oldItem: CommunityPost, newItem: CommunityPost): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: CommunityPost, newItem: CommunityPost): Boolean {
                return oldItem == newItem
            }
        }
    }

    class CommunityPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.postTitle)
        private val contentTextView: TextView = itemView.findViewById(R.id.postContent)
        private val categoryTextView: TextView = itemView.findViewById(R.id.postCategory)
        private val likesTextView: TextView = itemView.findViewById(R.id.postLikes)

        fun bind(post: CommunityPost) {
            titleTextView.text = post.title
            contentTextView.text = post.content
            categoryTextView.text = post.category
            likesTextView.text = "Likes: ${post.likes}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityPostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_community_post, parent, false)
        return CommunityPostViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommunityPostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}