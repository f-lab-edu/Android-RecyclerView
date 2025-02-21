package com.jg.android_recyclerview.utils

import com.jg.android_recyclerview.model.ItemType
import com.jg.android_recyclerview.model.ListItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

object CreateUtils {
    const val TIMER_DURATION = 3000L  // 3초
    private const val TOTAL_ITEMS = 50

    fun createRandomAlphabetItems() : List<ListItem> {
        return List(TOTAL_ITEMS) { index ->
            val randomChar = ('A'..'Z').random()
            ListItem(
                id = index.toString(),
                content = randomChar.toString(),
                type = ItemType.NORMAL,
                isRecovering = false,
                timerJob = Job()
            )
        }
    }

    fun createTimerFlow() = flow {
        var remainingTime = TIMER_DURATION
        while (remainingTime > 0) {
            emit(remainingTime)
            delay(1000)
            remainingTime -= 1000
        }
        emit(0)
    }
}