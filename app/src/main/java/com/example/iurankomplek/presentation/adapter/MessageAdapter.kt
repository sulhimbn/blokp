package com.example.iurankomplek.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.R
import com.example.iurankomplek.model.Message

class MessageAdapter : ListAdapter<Message, MessageAdapter.MessageViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = GenericDiffUtil.byId<Message> { it.id }
        private const val SENDER_PREFIX = "From: "
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val contentTextView: TextView = itemView.findViewById(R.id.messageContent)
        private val timestampTextView: TextView = itemView.findViewById(R.id.messageTimestamp)
        private val senderTextView: TextView = itemView.findViewById(R.id.messageSender)

        fun bind(message: Message) {
            contentTextView.text = message.content
            timestampTextView.text = message.timestamp
            senderTextView.text = "$SENDER_PREFIX${message.senderId}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}