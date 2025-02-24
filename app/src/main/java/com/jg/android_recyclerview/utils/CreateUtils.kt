package com.jg.android_recyclerview.utils

import com.jg.android_recyclerview.model.ItemType
import com.jg.android_recyclerview.model.ListItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

object CreateUtils {
    private const val TOTAL_ITEMS = 50

    fun createRandomAlphabetItems() : List<ListItem> {
        return List(TOTAL_ITEMS) { index ->
            val randomChar = ('A'..'Z').random()
            ListItem(
                id = index.toString(),
                content = randomChar.toString(),
                type = ItemType.NORMAL,
                isRecovering = false,
                timerJob = null
            )
        }
    }
}