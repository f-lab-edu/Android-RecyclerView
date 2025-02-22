package com.jg.android_recyclerview.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jg.android_recyclerview.databinding.ItemListBinding
import com.jg.android_recyclerview.model.ItemType
import com.jg.android_recyclerview.model.ListItem
import com.jg.android_recyclerview.utils.DiffCallback

class ListSampleAdapter : ListAdapter<ListItem, RecyclerView.ViewHolder>(DiffCallback()) {
    private var onItemClick: ((ListItem) -> Unit)? = null

    fun setOnItemClickListener(listener: (ListItem) -> Unit) {
        onItemClick = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemListBinding.inflate(
            /* inflater = */ inflater,
            /* parent = */ parent,
            /* attachToParent = */false
        )
        return when (viewType) {
            ItemType.NORMAL.ordinal -> NormalViewHolder(binding)
            else -> TrashViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is NormalViewHolder -> holder.bind(item, onItemClick)
            is TrashViewHolder -> holder.bind(item, onItemClick)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type.ordinal
    }
}