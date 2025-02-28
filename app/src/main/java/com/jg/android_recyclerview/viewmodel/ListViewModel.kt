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
    
    // 일반 아이템 목록을 저장하는 변경 가능한 상태 흐름, 초기값은 빈 리스트
    private val _items = MutableStateFlow<List<ListItem>>(emptyList())
     // 휴지통 아이템 목록을 저장하는 변경 가능한 상태 흐름, 초기값은 빈 리스트
    private val _trashItems = MutableStateFlow<List<ListItem>>(emptyList())

    // 현재 뷰 모드를 저장하는 변경 가능한 상태 흐름, 초기값은 NORMAL 모드
    private val _currentMode = MutableStateFlow(ViewMode.NORMAL)
    val currentMode = _currentMode.asStateFlow()

    val displayItems = combine(_items, _trashItems, _currentMode) { items, trashItems, mode ->
        // 세 개의 상태 흐름을 결합하여 현재 모드에 따라 표시할 아이템 목록 계산
        when (mode) {
            ViewMode.NORMAL -> items  // NORMAL 모드일 때 일반 아이템 목록 표시
            ViewMode.TRASH -> trashItems  // TRASH 모드일 때 휴지통 아이템 목록 표시
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())  
    // 결합된 Flow를 StateFlow로 변환, viewModelScope 내에서 즉시 구독 시작(Eagerly), 초기값은 빈 리스트

    init {
        createItems()  // 초기 아이템 생성 메서드 호출
    }

    fun createItems() {
        _items.update {
            // 랜덤 알파벳 아이템 생성 유틸리티 호출
            CreateUtils.createRandomAlphabetItems()
        }
    }


    private val timerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())  

    fun switchItemType(type: ItemType, item: ListItem) {
        when (type) {
            ItemType.NORMAL -> moveToTrash(item)  // 일반 아이템을 휴지통으로 이동
            ItemType.TRASH -> restoreItem(item)  // 휴지통 아이템을 복원
        }
    }

    /**
     *  타이머 시작
     *  [itemId] : 타이머를 시작할 아이템 ID
     *  [remainingSecond] : 남은 시간(초)
     *  [isRecovering] : 복원 중인지 여부
     */
    private fun startItemTimer(
        itemId: String,
        remainingSecond: Int,
        isRecovering: Boolean
    ): Job = timerScope.launch {  // 타이머 코루틴 시작, Job 반환
        // 현재 남은 시간을 로컬 변수로 관리
        var currentSeconds = remainingSecond

        // 0초가 될 때까지 반복
        while (currentSeconds > 0) {
            // 남은 시간 업데이트 (초 -> 밀리초)
            _trashItems.update { items ->
                items.map { if (it.id == itemId) it.copy(remainingTime = currentSeconds * 1000) else it }
            }
            
            delay(1000)
            // 시간 1초 감소
            currentSeconds--
            
            // 아이템 상태 확인 (복원 상태가 변경되었는지)
            val item = _trashItems.value.find { it.id == itemId } ?: break  // 아이템이 없으면 종료
            if (item.isRecovering != isRecovering) break  // 복원 상태가 변경되었으면 종료
        }
        
        // 타이머 종료 후 처리 (0초가 되었을 때)
        withContext(Dispatchers.Main) {  // UI 스레드로 전환
            _trashItems.value.find { it.id == itemId }?.let { item ->  // 아이템 찾기
                item.timerJob?.cancel()  // 기존 타이머 작업 취소
                
                // 복원 중이면 복원 완료, 아니면 완전 삭제
                if (isRecovering) completeRestore(item) else deleteCompletely(item)
            }
        }
    }

    /**
     * 아이템을 휴지통으로 이동
     */
    fun moveToTrash(item: ListItem) {
        if (item.type == ItemType.TRASH || _trashItems.value.any { it.id == item.id }) {
            // 이미 휴지통에 있거나, 휴지통에 같은 ID의 아이템이 있으면
            return  // 함수 종료
        }

        // 일반 아이템 목록에서 해당 아이템 제거
        _items.update { it.filterNot { it.id == item.id } }

        val trashItem = item.copy(  // 휴지통 아이템으로 변환
            type = ItemType.TRASH,  // 타입을 TRASH로 변경
            remainingTime = 3000,  // 남은 시간 3000밀리초(3초) 설정
            timerJob = startItemTimer(item.id, 3, false)  // 3초 타이머 시작, 복원 중 아님
        )

        _trashItems.update { it + trashItem }  // 휴지통 아이템 목록에 추가
    }

    /**
     * 아이템 복원
     */
    fun restoreItem(item: ListItem) {
        // 아직 복원 중이 아니면 복원
        if (!item.isRecovering) {
            item.timerJob?.cancel()  // 기존 타이머 작업 취소

            _trashItems.update { items ->  // 휴지통 아이템 목록 업데이트
                items.map {  // 모든 아이템 매핑
                    if (it.id == item.id) {  // 복원할 아이템인 경우
                        it.copy(
                            isRecovering = true,  // 복원 중 상태로 변경
                            remainingTime = 3000,  // 남은 시간 3000밀리초(3초) 설정
                            timerJob = startItemTimer(item.id, 3, true)  // 3초 복원 타이머 시작
                        )
                    } else it  // 다른 아이템은 그대로 유지
                }
            }
        }
    }

    /**
     * 아이템 복원 완료
     */
    private fun completeRestore(item: ListItem) {
        // 휴지통 아이템 목록에서 제거
        _trashItems.update { it.filterNot { it.id == item.id } }
        _items.update {  // 일반 아이템 목록 업데이트
            it + item.copy(  // 일반 아이템으로 변환하여 추가
                type = ItemType.NORMAL,  // 타입을 NORMAL로 변경
                isRecovering = false,  // 복원 중 상태 해제
                remainingTime = null,  // 남은 시간 초기화
                timerJob = null  // 타이머 작업 초기화
            )
        }
    }

    /**
     * 아이템 완전 삭제
     */
    private fun deleteCompletely(item: ListItem) {
        // 휴지통 아이템 목록에서 제거
        _trashItems.update { list -> list.filterNot { it.id == item.id } }
    }

    /**
     * 일반 모드와 휴지통 모드 전환
     */
    fun switchToTrashOrNormal() {
        // 현재 모드가 NORMAL이면 TRASH로, TRASH면 NORMAL로 전환
        _currentMode.update { if (it == ViewMode.NORMAL) ViewMode.TRASH else ViewMode.NORMAL }
    }

    override fun onCleared() {
        super.onCleared()
        // 모든 휴지통 아이템의 타이머 작업 취소
        _trashItems.value.forEach { it.timerJob?.cancel() }
        // 타이머 코루틴 스코프 취소
        timerScope.cancel()
    }
}