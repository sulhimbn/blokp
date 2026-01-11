package com.example.iurankomplek.presentation.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class BaseListAdapter<T, VH : RecyclerView.ViewHolder>(
    diffCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, VH>(diffCallback) {

    protected abstract fun createViewHolderInternal(parent: ViewGroup): VH

    protected abstract fun bindViewHolderInternal(holder: VH, item: T)

    protected fun getItemAt(position: Int): T = getItem(position)

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return createViewHolderInternal(parent)
    }

    final override fun onBindViewHolder(holder: VH, position: Int) {
        bindViewHolderInternal(holder, getItem(position))
    }

    companion object {
        fun <T : Any> diffById(idSelector: (T) -> Any): DiffUtil.ItemCallback<T> {
            return GenericDiffUtil.byId(idSelector)
        }
    }
}

    final override fun onBindViewHolder(holder: VH, position: Int) {
        bindViewHolderInternal(holder, getItem(position))
    }
}
