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

    fun moveToTrash(item: ListItem) {
        // 휴지통으로 이동
        _items.update { items ->
            items.filterNot {
                it.id == item.id
            }
        }
        val updatedItem = item.copy(
            type = ItemType.TRASH,
            isRecovering = false,
            timerJob = startItemTimer(
                item = item,
                stateFlow = _trashItems,
                onComplete = { deleteCompletely(it) }
            )
        )
        _trashItems.update { currentItems ->
            currentItems + updatedItem
        }
    }

    fun restoreItem(item: ListItem) {

        if (!item.isRecovering) {
            // 기존 Job 취소
            item.timerJob?.cancel()

            // 먼저 복구 상태로 변경
            _trashItems.update { trashItems ->
                trashItems.map { existingItem ->
                    if (existingItem.id == item.id) {
                        existingItem.copy(
                            isRecovering = true,
                            remainingTime = 3000,
                            timerJob = null
                        )
                    } else existingItem
                }
            }

            // 현재 아이템 찾기
            val updatedItem = _trashItems.value.find { it.id == item.id } ?: return

            // 새 타이머 시작
            val newTimerJob = startItemTimer(
                item = updatedItem,
                stateFlow = _trashItems
            ) { completedItem ->
                // 복구 완료 시
                _trashItems.update { items ->
                    items.filterNot { it.id == completedItem.id }
                }
                _items.update { items ->
                    items + completedItem.copy(
                        type = ItemType.NORMAL,
                        isRecovering = false,
                        remainingTime = null,
                        timerJob = null
                    )
                }
            }

            // 타이머 Job 설정
            _trashItems.update { trashItems ->
                trashItems.map { existingItem ->
                    if (existingItem.id == item.id) {
                        existingItem.copy(timerJob = newTimerJob)
                    } else existingItem
                }
            }
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
        timerScope.cancel() // ViewModel 소멸 시 모든 코루틴 취소
    }
}