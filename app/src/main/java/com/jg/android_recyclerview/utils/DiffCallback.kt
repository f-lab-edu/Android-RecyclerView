package com.jg.android_recyclerview.utils

import androidx.recyclerview.widget.DiffUtil
import com.jg.android_recyclerview.model.ListItem

class DiffCallback : DiffUtil.ItemCallback<ListItem>() {
    override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return oldItem == newItem &&
                oldItem.remainingTime == newItem.remainingTime
    }
}