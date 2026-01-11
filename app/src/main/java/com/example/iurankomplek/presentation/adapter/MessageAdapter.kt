package com.example.iurankomplek.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.databinding.ItemMessageBinding
import com.example.iurankomplek.model.Message

class MessageAdapter : BaseListAdapter<Message, MessageAdapter.MessageViewHolder>(
    diffById { it.id }
) {

    companion object {
        private const val SENDER_PREFIX = "From: "
    }

    override fun createViewHolderInternal(parent: ViewGroup): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun bindViewHolderInternal(holder: MessageViewHolder, message: Message) {
        holder.bind(message)
    }

    class MessageViewHolder(val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.messageContent.text = message.content
            binding.messageTimestamp.text = message.timestamp
            binding.messageSender.text = "$SENDER_PREFIX${message.senderId}"
        }
    }
}