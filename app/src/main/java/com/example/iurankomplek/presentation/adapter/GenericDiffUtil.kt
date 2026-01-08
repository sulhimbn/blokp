package com.example.iurankomplek.presentation.adapter

import androidx.recyclerview.widget.DiffUtil

class GenericDiffUtil<T : Any>(
    private val areItemsTheSameCallback: (oldItem: T, newItem: T) -> Boolean,
    private val areContentsTheSameCallback: (oldItem: T, newItem: T) -> Boolean
) : DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return areItemsTheSameCallback(oldItem, newItem)
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return areContentsTheSameCallback(oldItem, newItem)
    }

    companion object {
        fun <T : Any> byId(idSelector: (T) -> Any): GenericDiffUtil<T> {
            return GenericDiffUtil(
                areItemsTheSameCallback = { oldItem, newItem -> idSelector(oldItem) == idSelector(newItem) },
                areContentsTheSameCallback = { oldItem, newItem -> oldItem == newItem }
            )
        }
    }
}
