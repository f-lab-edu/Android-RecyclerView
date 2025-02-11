package com.jg.android_recyclerview.model

data class ListItem(
    val id: String,
    val content: String,
    val type: ItemType,
    val remainingTime: Int? = null
)
