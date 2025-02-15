package com.jg.android_recyclerview.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jg.android_recyclerview.R
import com.jg.android_recyclerview.databinding.ItemListBinding
import com.jg.android_recyclerview.model.ItemType
import com.jg.android_recyclerview.model.ListItem

class MainAdapterWithoutDiff : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<ListItem> = emptyList()
    private var onItemClick: ((ListItem) -> Unit) ?= null

    fun setOnItemClickListener(listener: (ListItem) -> Unit) {
        onItemClick = listener
    }

    fun submitList(newList: List<ListItem>) {
        items = newList.toList()
        notifyDataSetChanged() // 갱신
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
            is NormalViewHolder -> holder.bind(item)
            is TrashViewHolder -> holder.bind(item)
        }
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int) = items[position].type.ordinal

    inner class NormalViewHolder(private val binding: ItemListBinding) :
            RecyclerView.ViewHolder(binding.root) {
                fun bind(item: ListItem) {
                    binding.apply {
                        tvContent.text = item.content
                        progressBar.visibility = View.GONE
                        ivTrash.setOnClickListener {
                            onItemClick?.invoke(item)
                        }
                    }
                }
            }

    inner class TrashViewHolder(private val binding: ItemListBinding) :
            RecyclerView.ViewHolder(binding.root) {
                fun bind(item: ListItem) {
                    binding.apply {
                        tvContent.text = "${item.content} (휴지통)"
                        progressBar.visibility = View.VISIBLE
                        progressBar.progress = item.remainingTime ?: 3000
                        ivTrash.setImageResource(R.drawable.ic_restore)
                        ivTrash.setOnClickListener {
                            tvContent.text = "${item.content} (복구중)"
                            onItemClick?.invoke(item)
                        }
                    }
                }
            }
}