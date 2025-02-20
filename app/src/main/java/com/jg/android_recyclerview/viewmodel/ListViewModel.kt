package com.jg.android_recyclerview.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jg.android_recyclerview.model.ItemType
import com.jg.android_recyclerview.model.ListItem
import com.jg.android_recyclerview.model.ViewMode
import com.jg.android_recyclerview.utils.CreateUtils
import com.jg.android_recyclerview.utils.CreateUtils.createTimerFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
        _items.value = CreateUtils.createRandomAlphabetItems()
    }

    private val timerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val timerJobs = mutableMapOf<String, Job>()

    private fun startTimer(item: ListItem, onTimerComplete: (ListItem) -> Unit) {
        // 이전 타이머 취소 (중복 실행 : 같은 아이템에 대해 여러 타이머가 실행될 여지를 위함)
        timerJobs[item.id]?.cancel()

        // TODO 뭔가 타이밍 이슈가 있는데 잡질 못한다..
        timerJobs[item.id] = timerScope.launch {
            createTimerFlow().collect { remainingTime ->
                // 현재 아이템이 휴지통에 있는 경우에만 업데이트
                if (_trashItems.value.any { it.id == item.id }) {
                    val updatedTrashItems = _trashItems.value.toMutableList()
                    val index = updatedTrashItems.indexOfFirst { it.id == item.id }
                    if (index != -1) {
                        updatedTrashItems[index] = updatedTrashItems[index].copy(
                            remainingTime = remainingTime.toInt()
                        )
                        _trashItems.value = updatedTrashItems
                    }
                } else {
                    // 휴지통에 없으면 타이머 취소
                    timerJobs[item.id]?.cancel()
                    timerJobs.remove(item.id)
                    return@collect
                }
            }
            onTimerComplete(item)
            timerJobs.remove(item.id)
        }

    }

    fun switchItemType(type: ItemType, item: ListItem) {
        when (type) {
            ItemType.NORMAL -> moveToTrash(item)
            ItemType.TRASH -> restoreItem(item)
        }
    }

    fun moveToTrash(item: ListItem) {
        updateItems(
            item = item,
            fromList = _items,
            toList = _trashItems,
            newType = ItemType.TRASH,
            onTimerComplete = { deleteCompletely(it) }
        )
    }

    fun restoreItem(item: ListItem) {
        updateItems(
            item = item,
            fromList = _trashItems,
            toList = _items,
            newType = ItemType.NORMAL,
            onTimerComplete = { }
        )
    }

    private fun updateItems(
        item: ListItem,
        fromList: MutableStateFlow<List<ListItem>>,
        toList: MutableStateFlow<List<ListItem>>,
        newType: ItemType,
        onTimerComplete: (ListItem) -> Unit
    ) {
        // 기존 타이머 취소
        timerJobs[item.id]?.cancel()
        timerJobs.remove(item.id)

        val currentFromList = fromList.value.toMutableList()
        val currentToList = toList.value.toMutableList()

        currentFromList.remove(item)
        val updatedItem = item.copy(
            type = newType,
            remainingTime = if (newType == ItemType.TRASH) 3000 else null
        )

        if (newType == ItemType.TRASH) {
            currentToList.add(updatedItem)

            // 휴지통으로 이동 시 즉시 리스트 갱신
            fromList.value = currentFromList
            toList.value = currentToList

            startTimer(updatedItem, onTimerComplete)
        } else {
            // 복구 시 타이머 완료 후 리스트 갱신
            startTimer(updatedItem) {
                currentToList.add(updatedItem)
                fromList.value = currentFromList
                toList.value = currentToList
            }
        }
    }

    private fun deleteCompletely(item: ListItem) {
        // 휴지통에서 완전 삭제
        val currentTrashItems = _trashItems.value.toMutableList()
        currentTrashItems.removeAll { it.id == item.id }
        _trashItems.value = currentTrashItems
    }

    fun switchToTrashOrNormal() {
        _currentMode.value = when (_currentMode.value) {
            ViewMode.NORMAL -> ViewMode.TRASH
            ViewMode.TRASH -> ViewMode.NORMAL
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJobs.values.forEach { it.cancel() }
        timerScope.cancel() // ViewModel 소멸 시 모든 코루틴 취소
    }
}