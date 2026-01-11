package com.example.iurankomplek.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.databinding.ItemCommunityPostBinding
import com.example.iurankomplek.model.CommunityPost

class CommunityPostAdapter : BaseListAdapter<CommunityPost, CommunityPostAdapter.CommunityPostViewHolder>(
    diffById { it.id }
) {

    companion object {
        private const val LIKES_PREFIX = "Likes: "
    }

    override fun createViewHolderInternal(parent: ViewGroup): CommunityPostViewHolder {
        val binding = ItemCommunityPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommunityPostViewHolder(binding)
    }

    override fun bindViewHolderInternal(holder: CommunityPostViewHolder, post: CommunityPost) {
        holder.bind(post)
    }

    class CommunityPostViewHolder(val binding: ItemCommunityPostBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: CommunityPost) {
            binding.postTitle.text = post.title
            binding.postContent.text = post.content
            binding.postCategory.text = post.category
            binding.postLikes.text = "$LIKES_PREFIX${post.likes}"
        }
    }
}