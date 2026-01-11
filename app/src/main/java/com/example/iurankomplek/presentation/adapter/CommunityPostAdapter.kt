package com.example.iurankomplek.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.databinding.ItemCommunityPostBinding
import com.example.iurankomplek.model.CommunityPost

class CommunityPostAdapter : ListAdapter<CommunityPost, CommunityPostAdapter.CommunityPostViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = GenericDiffUtil.byId<CommunityPost> { it.id }
        private const val LIKES_PREFIX = "Likes: "
    }

    class CommunityPostViewHolder(val binding: ItemCommunityPostBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: CommunityPost) {
            binding.postTitle.text = post.title
            binding.postContent.text = post.content
            binding.postCategory.text = post.category
            binding.postLikes.text = "$LIKES_PREFIX${post.likes}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityPostViewHolder {
        val binding = ItemCommunityPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommunityPostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommunityPostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}