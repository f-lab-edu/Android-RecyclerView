package com.jg.android_recyclerview.model

import kotlinx.coroutines.Job

data class ListItem(
    val id: String,
    val content: String,
    val type: ItemType,
    val remainingTime: Int? = null,
    val isRecovering: Boolean = false,
    val timerJob: Job? = null
)
