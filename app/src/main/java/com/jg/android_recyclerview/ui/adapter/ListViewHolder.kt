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
            //progressBar.visibility = View.GONE   // 복구 했다가, 다시 삭제로 왔을 경우 업데이트가 필요
            layoutConstraint.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }
}

class TrashViewHolder(private val binding: ItemListBinding) : ListViewHolder(binding) {
    override fun bind(item: ListItem, onItemClick: ((ListItem) -> Unit)?) {
        binding.apply {
            ivTrash.setImageResource(R.drawable.ic_restore)

            val seconds = (item.remainingTime ?: 3000) / 1000

            when {
                item.isRecovering -> {
                    tvContent.text = "${item.content} (복구중)"
                    tvTimer.text = "$seconds 초 후 복구됨"
                }
                else -> {
                    tvContent.text = "${item.content} (휴지통)"
                    tvTimer.text = "$seconds 초 후 삭제됨"
                }
            }

            layoutConstraint.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }
}