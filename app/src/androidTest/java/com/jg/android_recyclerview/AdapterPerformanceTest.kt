package com.jg.android_recyclerview

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jg.android_recyclerview.model.ItemType
import com.jg.android_recyclerview.model.ListItem
import com.jg.android_recyclerview.ui.adapter.MainAdapter
import com.jg.android_recyclerview.ui.adapter.MainAdapterWithoutDiff
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureTimeMillis

@RunWith(AndroidJUnit4::class)
class AdapterPerformanceTest {

    @Test
    fun compareAdapterPerformance() {
        val diffAdapter = MainAdapter()
        val normalAdapter = MainAdapterWithoutDiff()

        // Test 데이터
        fun createTestData(size: Int): List<ListItem> =
            List(size) { index ->
                ListItem(
                    id = "$index",
                    content = "Item $index",
                    type = if (index % 2 == 0) ItemType.NORMAL else ItemType.TRASH,
                    remainingTime = if (index % 3 == 0) 3000 else null
                )
            }

        // 1. 대량 데이터 초기 로딩
        // 실행 또는 화면 전환시 발생하는 연산 성능 차이 보기 위함
        //  결과 : Diff 0ms, Normal 1ms
        val largeDataSet = createTestData(1000)
        measureTimeMillis {
            diffAdapter.submitList(largeDataSet)
        }.also { println("DiffUtil 대량 데이터 로딩 : $it ms")}

        measureTimeMillis {
            normalAdapter.submitList(largeDataSet)
        }.also { println("일반 Adapter 대량 데이터 로딩 : $it ms") }

        // 2. 단일 아이템 업데이트
        // 좋아요, 체크박스 변경등 단일 아이템 변경 시 인터렉션 반응 속도이지만, 성능에 차이 확인 하기 위함
        //  결과 : Diff : 0ms, Normal : 0ms
        val updateList = largeDataSet.toMutableList().apply {
            this[500] = this[500].copy(
                content = "Updated Item",
                type = if (this[500].type == ItemType.NORMAL) ItemType.TRASH else ItemType.NORMAL,
                remainingTime = if (this[500].remainingTime == null) 3000 else null)
        }

        measureTimeMillis {
            diffAdapter.submitList(updateList)
        }.also { println("DiffUtil 대량 데이터 로딩 : $it ms")}

        measureTimeMillis {
            normalAdapter.submitList(updateList)
        }.also { println("일반 Adapter 대량 데이터 로딩 : $it ms") }

        // 3. 아이템 순서 변경
        // 드래그&드롭, 정렬 기능 사용시 처리에 UI/UX 영향의 차이이 이지만 성능에도 차이가 있는지 확인 목적
        // 결과 : Diff : 0ms, Normal : 0ms
        val shuffledList = largeDataSet.shuffled()

        measureTimeMillis {
            diffAdapter.submitList(shuffledList)
        }.also { println("DiffUtil 대량 데이터 로딩 : $it ms") }

        measureTimeMillis {
            normalAdapter.submitList(shuffledList)
        }.also { println("일반 Adapter 대량 데이터 로딩 : $it ms") }

        // 4. 연속적인 작은 업데이트
        // 실시간 데이터 업데이터, 주기적 개신 상황에 따라 갱신 성능 차이를 보기 위함
        // 결과 : Diff : 0ms, Normal : 0 or 1ms
        repeat(10) { iteration ->
            val smallUpdate = largeDataSet.toMutableList().apply {
                (0..9).forEach { index ->
                    this[index] = this[index].copy(content = "Update $iteration")
                }
            }

            measureTimeMillis {
                diffAdapter.submitList(smallUpdate)
            }.also { println("DiffUtil 작은 업데이트 $iteration : $it ms") }

            measureTimeMillis {
                normalAdapter.submitList(smallUpdate)
            }.also { println("일반 Adapter 작은 업데이트 $iteration : $it ms") }
        }
    }
}