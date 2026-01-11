package com.example.iurankomplek.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.databinding.ItemAnnouncementBinding
import com.example.iurankomplek.model.Announcement

class AnnouncementAdapter : BaseListAdapter<Announcement, AnnouncementAdapter.AnnouncementViewHolder>(
    diffById { it.id }
) {

    override fun createViewHolderInternal(parent: ViewGroup): AnnouncementViewHolder {
        val binding = ItemAnnouncementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnnouncementViewHolder(binding)
    }

    override fun bindViewHolderInternal(holder: AnnouncementViewHolder, announcement: Announcement) {
        holder.bind(announcement)
    }

    class AnnouncementViewHolder(val binding: ItemAnnouncementBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(announcement: Announcement) {
            binding.announcementTitle.text = announcement.title
            binding.announcementContent.text = announcement.content
            binding.announcementCategory.text = announcement.category
            binding.announcementCreatedAt.text = announcement.createdAt
        }
    }
}