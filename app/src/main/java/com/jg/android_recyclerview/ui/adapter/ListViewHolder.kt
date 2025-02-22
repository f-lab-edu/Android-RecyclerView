package com.jg.android_recyclerview.ui.adapter

import android.content.res.ColorStateList
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jg.android_recyclerview.R
import com.jg.android_recyclerview.databinding.ItemListBinding
import com.jg.android_recyclerview.model.ListItem


sealed class ListViewHolder(binding: ItemListBinding) : RecyclerView.ViewHolder(binding.root) {
    abstract fun bind(item: ListItem, onItemClick: ((ListItem) -> Unit)?)
}

class NormalViewHolder(private val binding: ItemListBinding) : ListViewHolder(binding) {
    init {
        binding.progressBar.visibility = View.GONE
    }
    override fun bind(item: ListItem, onItemClick: ((ListItem) -> Unit)?) {
        binding.apply {
            tvContent.text = item.content

            ivTrash.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }
}

class TrashViewHolder(private val binding: ItemListBinding) : ListViewHolder(binding) {
    override fun bind(item: ListItem, onItemClick: ((ListItem) -> Unit)?) {
        binding.apply {
            tvContent.text = "${item.content} (휴지통)"
            progressBar.visibility = View.VISIBLE
            progressBar.progress = item.remainingTime ?: 3000
            ivTrash.setImageResource(R.drawable.ic_restore)
            ivTrash.setOnClickListener {
                tvContent.text = "${item.content} (복구중)"

                // 색상 변경
                progressBar.apply {
                    progressTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            context,
                            if (item.isRecovering) R.color.progress_restore else R.color.progress_delete
                        )
                    )
                    contentDescription = if (item.isRecovering) "복구 진행중" else "삭제 진행중"
                }

                onItemClick?.invoke(item)
            }
        }
    }
}