package com.jg.android_recyclerview.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jg.android_recyclerview.R
import com.jg.android_recyclerview.databinding.ItemListBinding
import com.jg.android_recyclerview.model.ItemType
import com.jg.android_recyclerview.model.ListItem

class ListWithoutDiffAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<ListItem>()
    private var onItemClick: ((ListItem) -> Unit)? = null

    fun submitList(newList: List<ListItem>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemListBinding.inflate(inflater, parent, false)
        return when (viewType) {
            ItemType.NORMAL.ordinal -> NormalViewHolder(binding)
            else -> TrashViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is NormalViewHolder -> holder.bind(item, onItemClick)
            is TrashViewHolder -> holder.bind(item, onItemClick)
        }
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int) = items[position].type.ordinal

}