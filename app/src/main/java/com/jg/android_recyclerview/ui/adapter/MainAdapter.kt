package com.jg.android_recyclerview.ui.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jg.android_recyclerview.R
import com.jg.android_recyclerview.databinding.ItemListBinding
import com.jg.android_recyclerview.model.ItemType
import com.jg.android_recyclerview.model.ListItem
import com.jg.android_recyclerview.utils.DiffCallback

class MainAdapter : ListAdapter<ListItem, RecyclerView.ViewHolder>(DiffCallback()) {
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
            is NormalViewHolder -> holder.bind(item)
            is TrashViewHolder -> holder.bind(item)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type.ordinal
    }

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

        private var isRestoreMode = false

        fun bind(item: ListItem) {
            binding.apply {
                tvContent.text = "${item.content} (휴지통)"
                progressBar.visibility = View.VISIBLE
                progressBar.progress = item.remainingTime ?: 3000
                ivTrash.setImageResource(R.drawable.ic_restore)
                ivTrash.setOnClickListener {
                    tvContent.text = "${item.content} (복구중)"

                    isRestoreMode = !isRestoreMode  // 모드 토글

                    // 색상 변경
                    progressBar.apply {
                        progressTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(
                                context,
                                if (isRestoreMode) R.color.progress_restore else R.color.progress_delete
                            )
                        )
                        contentDescription = if (isRestoreMode) "복구 진행중" else "삭제 진행중"
                    }

                    onItemClick?.invoke(item)
                }
            }
        }
    }

}