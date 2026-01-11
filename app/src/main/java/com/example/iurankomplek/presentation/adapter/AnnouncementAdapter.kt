package com.example.iurankomplek.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.databinding.ItemAnnouncementBinding
import com.example.iurankomplek.model.Announcement

class AnnouncementAdapter : ListAdapter<Announcement, AnnouncementAdapter.AnnouncementViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = GenericDiffUtil.byId<Announcement> { it.id }
    }

    class AnnouncementViewHolder(val binding: ItemAnnouncementBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(announcement: Announcement) {
            binding.announcementTitle.text = announcement.title
            binding.announcementContent.text = announcement.content
            binding.announcementCategory.text = announcement.category
            binding.announcementCreatedAt.text = announcement.createdAt
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementViewHolder {
        val binding = ItemAnnouncementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnnouncementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnnouncementViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}