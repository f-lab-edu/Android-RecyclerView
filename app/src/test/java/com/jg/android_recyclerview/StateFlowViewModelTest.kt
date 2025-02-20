package com.jg.android_recyclerview

import com.jg.android_recyclerview.viewmodel.StateFlowViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlin.test.DefaultAsserter.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StateFlowViewModelTest {
    private lateinit var viewModel: StateFlowViewModel

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    @Before
    fun setup() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        viewModel = StateFlowViewModel()

        // 초기화 위한 코루틴 스코프
        val job = launch {
            viewModel.displayItems.collect { }
        }

        // 아이템 생성
        viewModel.createItems()

        advanceTimeBy(500)

        job.cancel()

        // 검증
        val items = viewModel.displayItems.value
        assertTrue("초기 아이템 없음: $items", items.isNotEmpty())
    }

    @Test
    fun `아이템 휴지통 리스트로 이동 되는지 확인`() = runTest {
        val initialItems = viewModel.displayItems.value
        assertFalse(initialItems.isEmpty()) // 아이템 확인
        val targetItems = initialItems[0] // 첫번째 아이템 삭제

        // Flow 수집을 위한 코루틴 스코프
        val job = launch {
            viewModel.displayItems.collect { }
        }

        viewModel.moveToTrash(targetItems)

        // 코루틴이 실행될 시간 제공
        // TODO 꼭 실행될 시간을 제공해야 할까? 흠..
        advanceTimeBy(500)

        // 이제 아이템이 제거되었는지 확인
        assertFalse(
            viewModel.displayItems.value.contains(targetItems),
            "아이템이 아직 리스트에 있음: ${viewModel.displayItems.value}"
        )
        viewModel.switchToTrashOrNormal()

        advanceTimeBy(500)  // 상태 변경 대기

        assertTrue(viewModel.displayItems.value.any { it.id == targetItems.id })

        job.cancel()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}