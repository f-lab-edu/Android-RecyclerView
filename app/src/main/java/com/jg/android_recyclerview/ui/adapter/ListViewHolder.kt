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

    override fun bind(item: ListItem, onItemClick: ((ListItem) -> Unit)?) {
        binding.apply {
            tvContent.text = item.content
            progressBar.visibility = View.GONE   // 복구 했다가, 다시 삭제로 왔을 경우 업데이트가 필요
            layoutConstraint.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }
}

class TrashViewHolder(private val binding: ItemListBinding) : ListViewHolder(binding) {
    override fun bind(item: ListItem, onItemClick: ((ListItem) -> Unit)?) {
        binding.apply {
            progressBar.visibility = View.VISIBLE
            ivTrash.setImageResource(R.drawable.ic_restore)
            progressBar.progress = item.remainingTime ?: 3000

            when {
                item.isRecovering -> {
                    // 복구 진행 중
                    tvContent.text = "${item.content} (복구중)"
                    progressBar.apply {
                        progressTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(context, R.color.progress_restore)
                        )
                        contentDescription = "복구 진행중"
                    }
                }

                else -> {
                    // 삭제 진행 중
                    tvContent.text = "${item.content} (휴지통)"
                    progressBar.apply {
                        progressTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(context, R.color.progress_delete)
                        )
                        progressBar.contentDescription = "삭제 진행중"
                    }
                }
            }

            layoutConstraint.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }
}