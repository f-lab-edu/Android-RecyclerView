package com.jg.android_recyclerview.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jg.android_recyclerview.databinding.ItemListBinding
import com.jg.android_recyclerview.model.ListItem
import com.jg.android_recyclerview.utils.DiffCallback

class MainAdapter : ListAdapter<ListItem, MainAdapter.NormalViewHolder>(DiffCallback()) {
    private var onItemClick: ((ListItem) -> Unit)? = null

    fun setOnItemClickListener(listener: (ListItem) -> Unit) {
        onItemClick = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NormalViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemListBinding.inflate(
            /* inflater = */ inflater,
            /* parent = */ parent,
            /* attachToParent = */false)
        return NormalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NormalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type.ordinal
    }

    inner class NormalViewHolder(private val binding: ItemListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ListItem) {
            binding.apply {
                tvContent.text = item.content
                ivTrash.setOnClickListener {
                    onItemClick?.invoke(item)
                }
            }
        }
    }

}