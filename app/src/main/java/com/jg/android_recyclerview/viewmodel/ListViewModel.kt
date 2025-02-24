package com.jg.android_recyclerview.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jg.android_recyclerview.model.ItemType
import com.jg.android_recyclerview.model.ListItem
import com.jg.android_recyclerview.model.ViewMode
import com.jg.android_recyclerview.utils.CreateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListViewModel : ViewModel() {
    private val _items = MutableStateFlow<List<ListItem>>(emptyList())
    private val _trashItems = MutableStateFlow<List<ListItem>>(emptyList())
    private val _currentMode = MutableStateFlow(ViewMode.NORMAL)
    val currentMode = _currentMode.asStateFlow()

    val displayItems = combine(_items, _trashItems, _currentMode) { items, trashItems, mode ->
        when (mode) {
            ViewMode.NORMAL -> items
            ViewMode.TRASH -> trashItems
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        createItems()
    }

    fun createItems() {
        _items.update {
            CreateUtils.createRandomAlphabetItems()
        }
    }

    private val timerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun switchItemType(type: ItemType, item: ListItem) {
        when (type) {
            ItemType.NORMAL -> moveToTrash(item)
            ItemType.TRASH -> restoreItem(item)
        }
    }

    private fun startItemTimer(
        item: ListItem,
        stateFlow: MutableStateFlow<List<ListItem>>,
        onComplete: (ListItem) -> Unit
    ): Job {
        return timerScope.launch {
            try {
                (3 downTo 1).forEach { time ->
                    stateFlow.update { currentList ->
                        currentList.map { listItem ->
                            if (listItem.id == item.id) {
                                listItem.copy(
                                    remainingTime = time * 1000,
                                    isRecovering = item.isRecovering
                                )
                            } else listItem
                        }
                    }
                    delay(1000L)
                }
                onComplete(item)
            } catch (e: Exception) {
                // 타이머 취소 시 예외 처리
                println("Timer cancelled for item ${item.id}")
            }
        }
    }

    private fun startItemTimer(
        itemId: String,
        remainingSeconds: Int,
        isRecovering: Boolean
    ): Job = timerScope.launch {
        try {
            if (remainingSeconds <= 0) {
                withContext(Dispatchers.Main) {
                    _trashItems.value.find { it.id == itemId }?.let { item ->
                        item.timerJob?.cancel()
                        if (item.isRecovering) {
                            completeRestore(item)
                        } else {
                            deleteCompletely(item)
                        }
                    }
                }
                return@launch
            }

            _trashItems.update { items ->
                items.map {
                    if (it.id == itemId) {
                        it.copy(remainingTime = remainingSeconds * 1000)
                    } else it
                }
            }

            delay(1000)

            _trashItems.value.find { it.id == itemId }?.let { updatedItem ->
                if (updatedItem.isRecovering == isRecovering) {
                    startItemTimer(itemId, remainingSeconds - 1, isRecovering)
                }
            }
        } catch (e: Exception) {
            println("Timer cancelled for item $itemId")
        }
    }

    fun moveToTrash(item: ListItem) {
        if (item.type == ItemType.TRASH || _trashItems.value.any { it.id == item.id }) {
            return
        }

        _items.update { it.filterNot { it.id == item.id } }

        val trashItem = item.copy(
            type = ItemType.TRASH,
            remainingTime = 3000,
            timerJob = startItemTimer(item.id, 3, false)
        )

        _trashItems.update { it + trashItem }
    }

    fun restoreItem(item: ListItem) {
        if (!item.isRecovering) {
            item.timerJob?.cancel()

            _trashItems.update { items ->
                items.map {
                    if (it.id == item.id) {
                        it.copy(
                            isRecovering = true,
                            remainingTime = 3000,
                            timerJob = startItemTimer(item.id, 3, true)
                        )
                    } else it
                }
            }
        }

    }

    private fun completeRestore(item: ListItem) {
        _trashItems.update { it.filterNot { it.id == item.id } }
        _items.update {
            it + item.copy(
                type = ItemType.NORMAL,
                isRecovering = false,
                remainingTime = null,
                timerJob = null
            )
        }
    }

    private fun deleteCompletely(item: ListItem) {
        _trashItems.update { list -> list.filterNot { it.id == item.id } }
    }

    fun switchToTrashOrNormal() {
        _currentMode.update { if (it == ViewMode.NORMAL) ViewMode.TRASH else ViewMode.NORMAL }
    }

    override fun onCleared() {
        super.onCleared()
        _trashItems.value.forEach { it.timerJob?.cancel() }
        timerScope.cancel() // ViewModel 소멸 시 모든 코루틴 취소
    }
}